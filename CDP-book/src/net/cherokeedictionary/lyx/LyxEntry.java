package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.cherokeedictionary.main.App;
import net.cherokeedictionary.main.DbEntry;
import net.cherokeedictionary.main.JsonConverter;

import org.apache.commons.lang3.StringUtils;

public abstract class LyxEntry implements Comparable<LyxEntry> {
	public static boolean disable_hyphenation = true;
	private static final String LyxSoftHyphen = "\\SpecialChar \\-\n";

	public static String hyphenateSyllabary(String text) {
		if (disable_hyphenation) {
			return text.intern();
		}
		String quoteReplacement = Matcher.quoteReplacement(LyxSoftHyphen);
		for (String word : StringUtils.split(text)) {
			if (word.length() > 5) {
				text = text.replaceAll("([Ꭰ-Ᏼ]{3})([Ꭰ-Ᏼ]{3})", "$1"
						+ quoteReplacement + "$2");
			}
		}
		return text.intern();
	}

	public static interface HasNormalized {
		/**
		 * Additional entries "normalized" to help expose vowels on word roots.
		 * 
		 * @return
		 */
		public List<String> getNormalized();
	}

	protected abstract String sortKey();

	protected int id;
	public String pos = null;
	public String definition = null;
	public List<ExampleEntry> examples = new ArrayList<>();
	public String crossrefstxt = "";
	public List<CrossReference> crossrefs = new ArrayList<>();

	public abstract String getLyxCode();

	public abstract List<String> getSyllabary();

	public abstract List<String> getPronunciations();

	@Override
	final public int compareTo(LyxEntry arg0) {
		int cmp = sortKey().compareTo(arg0.sortKey());
		if (cmp == 0) {
			return definition.compareTo(arg0.definition);
		}
		return cmp;
	}

	private static void makeBlankIfEmptyLine(DefinitionLine line) {
		boolean b = StringUtils.isEmpty(line.pronounce.replace("-", ""));
		b |= StringUtils.isEmpty(line.syllabary.replace("-", ""));
		if (!b) {
			return;
		}
		line.pronounce = "";
		line.syllabary = "";
	}

	public static void fixCommas(DefinitionLine def) {
		def.pronounce = def.pronounce.replace(",", ", ")
				.replaceAll(", +", ", ");
		def.syllabary = def.syllabary.replace(",", ", ")
				.replaceAll(", +", ", ");
	}

	public static void fillinExampleSentences(LyxEntry entry, DbEntry dbentry) {
		String syllabary = dbentry.sentencesyllr;
		String pronounce = dbentry.sentenceq;
		String english = dbentry.sentenceenglishs;
		boolean s_blank = StringUtils.isBlank(syllabary);
		boolean p_blank = StringUtils.isBlank(pronounce);
		boolean e_blank = StringUtils.isBlank(english);
		if (s_blank && p_blank && e_blank) {
			return;
		}
		if (s_blank || p_blank || e_blank) {
			App.err("MISSING PART OF EXAMPLE SET FOR '" + dbentry.entrya + "'");
			App.err("\t" + (s_blank ? "SYLLABARY MISSING" : syllabary));
			App.err("\t" + (p_blank ? "PRONOUNCE MISSING" : pronounce));
			App.err("\t" + (e_blank ? "ENGLISH MISSING" : english));
			return;
		}

		syllabary = repairUnderlinesAndClean(syllabary);
		validateUnderlines(dbentry.entrya, syllabary);
		pronounce = repairUnderlinesAndClean(pronounce).replace("\\n", " ")
				.replace("\\\"", "\"").replace("\\", "");
		validateUnderlines(dbentry.entrya, pronounce);
		english = repairUnderlinesAndClean(english).replace("\\n", " ")
				.replace("\\\"", "\"").replace("\\", "");
		validateUnderlines(dbentry.entrya, english);

		String splitBy = "(\\? +|! +|\\. +)";
		String s[] = syllabary.split(splitBy);
		String p[] = pronounce.split(splitBy);
		String e[] = english.split(splitBy);
		if (s.length != p.length || p.length != e.length) {
			App.err("Unable to parse out examples for '" + dbentry.entrya + "'");
			s = new String[] { syllabary };
			p = new String[] { pronounce };
			e = new String[] { english };
		}
		if (s.length > 1) {
			for (int ix = 0; ix < s.length; ix++) {
				if (StringUtils.isEmpty(s[ix])) {
					continue;
				}
				if (!s[ix].contains("<u>")) {
					s = new String[] { syllabary };
					p = new String[] { pronounce };
					e = new String[] { english };
					break;
				}
				if (StringUtils.countMatches(s[ix], "<u>") != StringUtils
						.countMatches(s[ix], "</u>")) {
					s = new String[] { syllabary };
					p = new String[] { pronounce };
					e = new String[] { english };
					break;
				}
			}
		}
		if (s.length > 1) {
			for (int ix = 0; ix < s.length; ix++) {
				if (StringUtils.isEmpty(s[ix])) {
					continue;
				}
				syllabary = StringUtils.strip(StringUtils.substringAfter(
						syllabary, s[ix]));
				pronounce = StringUtils.strip(StringUtils.substringAfter(
						pronounce, p[ix]));
				english = StringUtils.strip(StringUtils.substringAfter(english,
						e[ix]));
				s[ix] += StringUtils.left(syllabary, 1);
				p[ix] += StringUtils.left(pronounce, 1);
				e[ix] += StringUtils.left(english, 1);
			}
		}
		for (int ix = 0; ix < s.length; ix++) {
			if (StringUtils.strip(e[ix]).matches("\\d+\\.?")) {
				continue;
			}
			ExampleEntry ee = new ExampleEntry();
			ee.english = StringUtils.strip(e[ix]);
			ee.pronounce = StringUtils.strip(p[ix]);
			ee.syllabary = StringUtils.strip(s[ix]);
			entry.examples.add(ee);
		}
	}

	private static String repairUnderlinesAndClean(String text) {
		text = text.replace(" </u>", "</u> ");
		text = text.replaceAll("u> +", "u> ");
		text = text.replace("<u> ", " <u>");
		text = text.replaceAll(" +<u>", " <u>");
		text = text.replace("\\n", " ");
		text = text.replace("\\\"", "\"");
		text = text.replace("\\", "");
		text = StringUtils.normalizeSpace(text);
		return text;
	}

	private static void validateUnderlines(String entry, String text) {
		if (!StringUtils.containsIgnoreCase(text, "<u>")
				&& !StringUtils.containsIgnoreCase(text, "</u>")) {
			App.err("Missing underline marking for '" + entry + "': " + text);
			return;
		}
		if (StringUtils.countMatches(text, "<u>") != StringUtils.countMatches(
				text, "</u>")) {
			App.err("Invalid underline marking (open vs close tag counts don't match) for '"
					+ entry + "': " + text);
			return;
		}
		if (text.matches(".*<u>[^a-zA-Z Ꭰ-Ᏼ]+</u>.*")) {
			App.err("Strange underline marking for '" + entry + "': " + text);
			return;
		}
	}

	public static LyxEntry getEntryFor(DbEntry dbentry) {

		String definitiond = dbentry.definitiond;
		definitiond = definitiond.replace(", (", " (");
		if (dbentry.partofspeechc.startsWith("v")) {

			if (warnIfNonVerbData(dbentry)) {
				return null;
			}

			VerbEntry entry = new VerbEntry();
			entry.id = dbentry.id;
			entry.pos = dbentry.partofspeechc;
			entry.definition = definitiond;

			entry.present3rd = new DefinitionLine();
			entry.present3rd.syllabary = dbentry.syllabaryb;
			entry.present3rd.pronounce = dbentry.entrytone;
			makeBlankIfEmptyLine(entry.present3rd);
			fixCommas(entry.present3rd);
			if (!fixPronunciation(dbentry, entry.present3rd)) {
				return null;
			}

			entry.present1st = new DefinitionLine();
			entry.present1st.syllabary = dbentry.vfirstpresh;
			entry.present1st.pronounce = dbentry.vfirstprestone;
			makeBlankIfEmptyLine(entry.present1st);
			fixCommas(entry.present1st);
			if (!fixPronunciation(dbentry, entry.present1st)) {
				return null;
			}

			entry.remotepast = new DefinitionLine();
			entry.remotepast.syllabary = dbentry.vthirdpastsyllj;
			entry.remotepast.pronounce = dbentry.vthirdpasttone;
			makeBlankIfEmptyLine(entry.remotepast);
			fixCommas(entry.remotepast);
			if (!fixPronunciation(dbentry, entry.remotepast)) {
				return null;
			}

			entry.habitual = new DefinitionLine();
			entry.habitual.syllabary = dbentry.vthirdpressylll;
			entry.habitual.pronounce = dbentry.vthirdprestone;
			makeBlankIfEmptyLine(entry.habitual);
			fixCommas(entry.habitual);
			if (!fixPronunciation(dbentry, entry.habitual)) {
				return null;
			}

			entry.imperative = new DefinitionLine();
			entry.imperative.syllabary = dbentry.vsecondimpersylln;
			entry.imperative.pronounce = dbentry.vsecondimpertone;
			makeBlankIfEmptyLine(entry.imperative);
			fixCommas(entry.imperative);
			if (!fixPronunciation(dbentry, entry.imperative)) {
				return null;
			}

			entry.infinitive = new DefinitionLine();
			entry.infinitive.syllabary = dbentry.vthirdinfsyllp;
			entry.infinitive.pronounce = dbentry.vthirdinftone;
			makeBlankIfEmptyLine(entry.infinitive);
			fixCommas(entry.infinitive);
			if (!fixPronunciation(dbentry, entry.infinitive)) {
				return null;
			}
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("n")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			NounEntry entry = new NounEntry();
			entry.id = dbentry.id;
			entry.pos = dbentry.partofspeechc;
			entry.definition = definitiond;

			entry.single = new DefinitionLine();
			entry.single.pronounce = dbentry.entrytone;
			entry.single.syllabary = dbentry.syllabaryb;
			fixCommas(entry.single);
			if (!fixPronunciation(dbentry, entry.single)) {
				return null;
			}

			entry.plural = new DefinitionLine();
			entry.plural.pronounce = dbentry.nounadjpluraltone;
			entry.plural.syllabary = dbentry.nounadjpluralsyllf;
			fixCommas(entry.plural);
			makeBlankIfEmptyLine(entry.plural);
			if (!fixPronunciation(dbentry, entry.plural)) {
				return null;
			}
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("ad")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			AdjectivialEntry entry = new AdjectivialEntry();
			entry.id = dbentry.id;
			entry.pos = dbentry.partofspeechc;
			entry.definition = definitiond;

			entry.single_in = new DefinitionLine();
			entry.single_in.pronounce = dbentry.entrytone;
			entry.single_in.syllabary = dbentry.syllabaryb;
			fixCommas(entry.single_in);
			if (!fixPronunciation(dbentry, entry.single_in)) {
				return null;
			}

			entry.single_an = new DefinitionLine();
			entry.single_an.pronounce = "";
			entry.single_an.syllabary = "";
			fixCommas(entry.single_an);
			makeBlankIfEmptyLine(entry.single_an);
			if (!fixPronunciation(dbentry, entry.single_an)) {
				return null;
			}

			entry.plural_in = new DefinitionLine();
			entry.plural_in.pronounce = dbentry.nounadjpluraltone;
			entry.plural_in.syllabary = dbentry.nounadjpluralsyllf;
			fixCommas(entry.plural_in);
			makeBlankIfEmptyLine(entry.plural_in);
			if (!fixPronunciation(dbentry, entry.plural_in)) {
				return null;
			}

			entry.plural_an = new DefinitionLine();
			entry.plural_an.pronounce = "";
			entry.plural_an.syllabary = "";
			fixCommas(entry.plural_an);
			makeBlankIfEmptyLine(entry.plural_an);
			if (!fixPronunciation(dbentry, entry.plural_an)) {
				return null;
			}
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("interj")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			if (warnIfNonVerbData(dbentry)) {
				return null;
			}
			InterjectionEntry entry = new InterjectionEntry();
			entry.id = dbentry.id;
			entry.pos = dbentry.partofspeechc;
			entry.definition = definitiond;

			entry.interj = new DefinitionLine();
			entry.interj.pronounce = dbentry.entrytone;
			entry.interj.syllabary = dbentry.syllabaryb;
			fixCommas(entry.interj);
			if (!fixPronunciation(dbentry, entry.interj)) {
				return null;
			}
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("prep")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			if (warnIfNonVerbData(dbentry)) {
				return null;
			}
			PostPositionEntry entry = new PostPositionEntry();
			entry.id = dbentry.id;
			entry.pos = "postp.";
			entry.definition = definitiond;

			entry.post = new DefinitionLine();
			entry.post.pronounce = dbentry.entrytone;
			entry.post.syllabary = dbentry.syllabaryb;
			fixCommas(entry.post);
			if (!fixPronunciation(dbentry, entry.post)) {
				return null;
			}
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("conj")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			if (warnIfNonVerbData(dbentry)) {
				return null;
			}
			ConjunctionEntry entry = new ConjunctionEntry();
			entry.id = dbentry.id;
			entry.pos = "postp.";
			entry.definition = definitiond;

			entry.conjunction = new DefinitionLine();
			entry.conjunction.pronounce = dbentry.entrytone;
			entry.conjunction.syllabary = dbentry.syllabaryb;
			fixCommas(entry.conjunction);
			if (!fixPronunciation(dbentry, entry.conjunction)) {
				return null;
			}
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("pron")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			if (warnIfNonVerbData(dbentry)) {
				return null;
			}
			PronounEntry entry = new PronounEntry();
			entry.id = dbentry.id;
			entry.pos = "postp.";
			entry.definition = definitiond;

			entry.pronoun = new DefinitionLine();
			entry.pronoun.pronounce = dbentry.entrytone;
			entry.pronoun.syllabary = dbentry.syllabaryb;
			fixCommas(entry.pronoun);
			if (!fixPronunciation(dbentry, entry.pronoun)) {
				return null;
			}
			return entry;
		}
		OtherEntry entry = new OtherEntry();
		if (warnIfVerbData(dbentry)) {
			return null;
		}
		entry.id = dbentry.id;
		entry.pos = dbentry.partofspeechc;
		entry.definition = definitiond;
		entry.other = new DefinitionLine();
		entry.other.pronounce = dbentry.entrytone;
		entry.other.syllabary = dbentry.syllabaryb;

		return entry;
	}

	public static boolean fixPronunciation(DbEntry dbentry, DefinitionLine def) {
		if (!fixToneCadenceMarks(def)) {
			App.err("Bad Pronunciation Entry: " + dbentry.entrya + " - "
					+ def.pronounce);
			return false;
		}
		return true;
	}

	private static boolean warnIfNonVerbData(DbEntry dbentry) {
		boolean valid = true;
		valid &= StringUtils.isEmpty(dbentry.nounadjplurale);
		valid &= StringUtils.isEmpty(dbentry.nounadjpluralsyllf);
		valid &= StringUtils.isEmpty(dbentry.nounadjpluraltone);
		valid &= StringUtils.isEmpty(dbentry.nounadjpluraltranslit);
		if (!valid) {
			App.err("NON-VERB DATA FOUND IN VERB DB ENTRY: " + dbentry.entrya
					+ " (" + dbentry.partofspeechc + ")" + " = "
					+ dbentry.definitiond);
			String string = new JsonConverter().toJson(dbentry);
			string = string.replaceAll("\"[^\"]+\":\"\",?", "");
			App.err("\t" + string);
		}
		return !valid;
	}

	private static boolean warnIfVerbData(DbEntry dbentry) {
		boolean valid = true;
		valid &= StringUtils.isEmpty(dbentry.vfirstpresg);
		valid &= StringUtils.isEmpty(dbentry.vfirstpresh);
		valid &= StringUtils.isEmpty(dbentry.vfirstprestone);
		valid &= StringUtils.isEmpty(dbentry.vfirstprestranslit);
		valid &= StringUtils.isEmpty(dbentry.vsecondimperm);
		valid &= StringUtils.isEmpty(dbentry.vsecondimpersylln);
		valid &= StringUtils.isEmpty(dbentry.vsecondimpertone);
		valid &= StringUtils.isEmpty(dbentry.vsecondimpertranslit);
		valid &= StringUtils.isEmpty(dbentry.vthirdinfo);
		valid &= StringUtils.isEmpty(dbentry.vthirdinfsyllp);
		valid &= StringUtils.isEmpty(dbentry.vthirdinftone);
		valid &= StringUtils.isEmpty(dbentry.vthirdinftranslit);
		valid &= StringUtils.isEmpty(dbentry.vthirdpasti);
		valid &= StringUtils.isEmpty(dbentry.vthirdpastsyllj);
		valid &= StringUtils.isEmpty(dbentry.vthirdpasttone);
		valid &= StringUtils.isEmpty(dbentry.vthirdpasttranslit);
		valid &= StringUtils.isEmpty(dbentry.vthirdpresk);
		valid &= StringUtils.isEmpty(dbentry.vthirdpressylll);
		valid &= StringUtils.isEmpty(dbentry.vthirdprestone);
		valid &= StringUtils.isEmpty(dbentry.vthirdprestranslit);
		if (!valid) {
			App.err("VERB DATA FOUND IN NON-VERB DB ENTRY: " + dbentry.entrya
					+ " (" + dbentry.partofspeechc + ")" + " = "
					+ dbentry.definitiond);
			String string = new JsonConverter().toJson(dbentry);
			string = string.replaceAll("\"[^\"]+\":\"\",?", "");
			App.err("\t" + string);
		}
		return !valid;
	}

	protected LyxEntry() {
	}

	public static class DefinitionLine {
		public String cf;
		public String label;
		public String definition;
		public String pos;
		public String pronounce;
		public String syllabary;
	}

	public static class ExampleLine {

	}

	public static class VerbEntry extends LyxEntry implements HasNormalized {
		public DefinitionLine present3rd = null;
		public DefinitionLine present1st = null;
		public DefinitionLine remotepast = null;
		public DefinitionLine habitual = null;
		public DefinitionLine imperative = null;
		public DefinitionLine infinitive = null;
		public ExampleLine[] example = null;

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, present3rd,
					pos, definition));
			sb.append("\\begin_deeper\n");
			sb.append(lyxSyllabaryPronounce(present1st));
			sb.append(lyxSyllabaryPronounce(remotepast));
			sb.append(lyxSyllabaryPronounce(habitual));
			sb.append(lyxSyllabaryPronounce(imperative));
			sb.append(lyxSyllabaryPronounce(infinitive));
			sb.append("\\end_deeper\n");
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(present3rd.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(present1st.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(remotepast.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(habitual.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(imperative.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(infinitive.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(present3rd.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(present1st.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(remotepast.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(habitual.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(imperative.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(infinitive.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(present3rd.syllabary);
			list.add(present1st.syllabary);
			list.add(remotepast.syllabary);
			list.add(habitual.syllabary);
			list.add(imperative.syllabary);
			list.add(infinitive.syllabary);
			return list;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(present3rd.pronounce);
			list.add(present1st.pronounce);
			list.add(remotepast.pronounce);
			list.add(habitual.pronounce);
			list.add(imperative.pronounce);
			list.add(infinitive.pronounce);
			return list;
		}

		@Override
		public List<String> getNormalized() {
			List<String> list = new ArrayList<>();
			String pres3 = StringUtils.strip(present3rd.syllabary);
			if (pres3.contains(",")) {
				pres3 = StringUtils.substringBefore(pres3, ",");
				pres3 = StringUtils.strip(pres3);
			}
			String pres1 = StringUtils.strip(present1st.syllabary);
			if (pres1.contains(",")) {
				pres1 = StringUtils.substringAfterLast(pres1, ",");
				pres1 = StringUtils.strip(pres1);
			}
			String past = StringUtils.strip(remotepast.syllabary);
			if (past.contains(",")) {
				past = StringUtils.substringBefore(past, ",");
				past = StringUtils.strip(past);
			}
			String habit = StringUtils.strip(habitual.syllabary);
			if (habit.contains(",")) {
				habit = StringUtils.substringBefore(habit, ",");
				habit = StringUtils.strip(habit);
			}
			String imp = StringUtils.strip(imperative.syllabary);
			if (imp.contains(",")) {
				imp = StringUtils.substringAfterLast(imp, ",");
				imp = StringUtils.strip(imp);
			}
			imp = fixImperativeSuffix(imp);
			String inf = StringUtils.strip(infinitive.syllabary);
			if (inf.contains(",")) {
				inf = StringUtils.substringBefore(inf, ",");
				inf = StringUtils.strip(inf);
			}
			if (pres3.startsWith("Ꭰ") && pres1.startsWith("Ꮵ")) {
				list.add(chopPrefix(pres3));
				list.add(chopPrefix(past));
				list.add(chopPrefix(habit));
				list.add(chopPrefix(imp));
				list.add(chopPrefix(inf));
				return list;
			}
			if (pres3.startsWith("Ꭶ") && pres1.startsWith("Ꮵ")) {
				list.add(chopPrefix(pres3));
				list.add(chopPrefix(past));
				list.add(chopPrefix(habit));
				list.add(chopPrefix(imp));
				list.add(chopPrefix(inf));
				return list;
			}
			if (pres3.startsWith("Ꭷ") && pres1.startsWith("Ꮵ")) {
				list.add(chopPrefix(pres3));
				list.add(chopPrefix(past));
				list.add(chopPrefix(habit));
				list.add(chopPrefix(imp));
				list.add(chopPrefix(inf));
				return list;
			}
			if (pres3.startsWith("Ꭰ") && pres1.startsWith("Ꭶ")) {
				list.add(newPrefix("Ꭰ", pres3));
				list.add(newPrefix("Ꭰ", past));
				list.add(newPrefix("Ꭰ", habit));
				list.add(newPrefix("Ꭰ", imp));
				list.add(newPrefix("Ꭰ", inf));
				return list;
			}
			if (pres3.startsWith("Ꭶ") && pres1.startsWith("Ꭶ")) {
				list.add(newPrefix("Ꭰ", pres3));
				list.add(newPrefix("Ꭰ", past));
				list.add(newPrefix("Ꭰ", habit));
				list.add(newPrefix("Ꭰ", imp));
				list.add(newPrefix("Ꭰ", inf));
				return list;
			}
			if (pres3.startsWith("Ꭷ") && pres1.startsWith("Ꭶ")) {
				list.add(newPrefix("Ꭰ", pres3));
				list.add(newPrefix("Ꭰ", past));
				list.add(newPrefix("Ꭰ", habit));
				list.add(newPrefix("Ꭰ", imp));
				list.add(newPrefix("Ꭰ", inf));
				return list;
			}
			return list;
		}

		private String fixImperativeSuffix(String imp) {
			if (StringUtils.isBlank(imp)) {
				return "";
			}
			String suffix = StringUtils.right(imp, 1);
			/*
			 * matches -Ꭷ !
			 */
			if (StringUtils.containsAny("ᎠᎦᎧᎭᎳᎹᎾᎿᏆᏌᏓᏔᏜᏝᏣᏩᏯ", suffix)){
				return imp;
			}
			/*
			 * does not match -Ꭷ !
			 */
			while (!StringUtils.containsAny("ᎠᎦᎭᎳᎹᎾᎿᏆᏌᏓᏔᏜᏝᏣᏩᏯ", suffix)) {
				char c = suffix.charAt(0);
				if (c < 'Ꭰ') {
					return imp;
				}
				c--;
				suffix = String.valueOf(c);
			}
			String recent_past_form = StringUtils.left(imp, imp.length() - 1)
					+ suffix;			
			return recent_past_form;
		}

		private String chopPrefix(String text) {
			return newPrefix("", text);
		}

		private String newPrefix(String prefix, String text) {
			if (StringUtils.isBlank(text)) {
				return "";
			}
			return prefix + StringUtils.substring(text, 1);
		}	
	}

	public static class InterjectionEntry extends LyxEntry {
		public DefinitionLine interj;
		public ExampleLine[] example = null;

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(interj.syllabary);
			return list;
		}

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, interj, pos, definition));
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(interj.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(interj.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(interj.pronounce);
			return list;
		}
	}

	public static class ConjunctionEntry extends LyxEntry {
		public DefinitionLine conjunction;
		public ExampleLine[] example = null;

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(conjunction.syllabary);
			return list;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(conjunction.pronounce);
			return list;
		}

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, conjunction,
					pos, definition));
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(conjunction.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(conjunction.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}
	}

	public static class PronounEntry extends LyxEntry {
		public DefinitionLine pronoun;
		public ExampleLine[] example = null;

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(pronoun.syllabary);
			return list;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(pronoun.pronounce);
			return list;
		}

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, pronoun, pos, definition));
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(pronoun.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(pronoun.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}
	}

	public static class PostPositionEntry extends LyxEntry {
		public DefinitionLine post;
		public ExampleLine[] example = null;

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(post.syllabary);
			return list;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(post.pronounce);
			return list;
		}

		public PostPositionEntry() {
			super();
			this.pos = "postp.";
		}

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, post, pos, definition));
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(post.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(post.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}
	}

	public static class NounEntry extends LyxEntry {
		public DefinitionLine single;
		public DefinitionLine plural;
		public ExampleLine[] example = null;

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(single.syllabary);
			list.add(plural.syllabary);
			return list;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(single.pronounce);
			list.add(plural.pronounce);
			return list;
		}

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, single, pos, definition));
			if (isOnlySyllabary(plural.syllabary)) {
				sb.append("\\begin_deeper\n");
				sb.append(lyxSyllabaryPronounce(plural));
				sb.append("\\end_deeper\n");
			}
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(single.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(plural.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(single.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(plural.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}
	}

	public static class AdjectivialEntry extends LyxEntry {
		public DefinitionLine single_in;
		public DefinitionLine single_an;
		public DefinitionLine plural_in;
		public DefinitionLine plural_an;
		public ExampleLine[] example = null;

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(single_in.syllabary);
			list.add(single_an.syllabary);
			list.add(plural_in.syllabary);
			list.add(plural_an.syllabary);
			return list;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(single_in.pronounce);
			list.add(single_an.pronounce);
			list.add(plural_in.pronounce);
			list.add(plural_an.pronounce);
			return list;
		}

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, single_in, pos, definition));
			boolean addit = isOnlySyllabary(single_an.syllabary);
			addit |= isOnlySyllabary(plural_in.syllabary);
			addit |= isOnlySyllabary(plural_an.syllabary);
			if (addit) {
				sb.append("\\begin_deeper\n");
				if (isOnlySyllabary(single_an.syllabary)) {
					sb.append(lyxSyllabaryPronounce(single_an));
				}
				if (isOnlySyllabary(plural_in.syllabary)) {
					sb.append(lyxSyllabaryPronounce(plural_in));
				}
				if (isOnlySyllabary(plural_an.syllabary)) {
					sb.append(lyxSyllabaryPronounce(plural_an));
				}
				sb.append("\\end_deeper\n");
			}
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(single_in.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(single_an.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(plural_in.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(plural_an.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(single_in.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(single_an.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(plural_in.pronounce.replace("-", ""));
				sb.append(" ");
				sb.append(plural_an.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}
	}

	public static class OtherEntry extends LyxEntry {

		public DefinitionLine other;
		public ExampleLine[] example = null;

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			list.add(other.syllabary);
			return list;
		}

		@Override
		public List<String> getPronunciations() {
			List<String> list = new ArrayList<>();
			list.add(other.pronounce);
			return list;
		}

		public OtherEntry() {
			super();
			this.pos = "other";
		}

		@Override
		public String getLyxCode() {
			StringBuilder sb = new StringBuilder();
			sb.append(lyxSyllabaryPronounceDefinition(id, other, pos, definition));
			return sb.toString();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(other.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""));
				sb.append(" ");
				sb.append(other.pronounce.replace("-", ""));
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}
	}

	public static class BodyPart extends LyxEntry {

		@Override
		public List<String> getSyllabary() {
			List<String> list = new ArrayList<>();
			return list;
		}

		@Override
		public String getLyxCode() {
			return sortKey();
		}

		private String _sortKey = null;

		@Override
		protected String sortKey() {
			if (StringUtils.isEmpty(_sortKey)) {
				StringBuilder sb = new StringBuilder();
				sb.append(" ");
				_sortKey = sb.toString();
				_sortKey = _sortKey.replaceAll(" +", " ");
				_sortKey = StringUtils.strip(_sortKey);
			}
			return _sortKey;
		}

		@Override
		public List<String> getPronunciations() {
			return null;
		}
	}

	private static String lyxSyllabaryPronounce(DefinitionLine def) {
		return lyxSyllabaryPronounce(def.syllabary, def.pronounce);
	}

	public boolean isOnlySyllabary(String syllabary) {
		if (StringUtils.isEmpty(syllabary.replaceAll("[ \\-]", ""))) {
			return false;
		}
		return StringUtils.isEmpty(syllabary.replaceAll("[Ꭰ-Ᏼ ]", ""));
	}

	private static String lyxSyllabaryPronounce(String syllabary,
			String pronounce) {
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin_layout Description\n");
		if (StringUtils.isEmpty(syllabary.replace("-", ""))) {
			syllabary = "-----";
			pronounce = "";
		}
		syllabary = syllabary.replace(" ",
				"\n\\begin_inset space ~\n\\end_inset\n");
		sb.append(syllabary);
		if (!StringUtils.isEmpty(pronounce)) {
			sb.append(" [");
			sb.append(pronounce);
			sb.append("]");
		}
		sb.append("\n");
		sb.append("\\end_layout\n");
		return sb.toString();
	}

	private static String lyxSyllabaryPronounceDefinition(int label,
			DefinitionLine def, String pos, String definition) {
		return lyxSyllabaryPronounceDefinition(label, def.syllabary,
				def.pronounce, pos, definition);
	}

	private static String lyxSyllabaryPronounceDefinition(int label,
			String syllabary, String pronounce, String pos, String definition) {
		if (StringUtils.isBlank(syllabary.replace("-", ""))) {
			syllabary = "-----";
			pronounce = "";
		}
		syllabary = syllabary.replace(" ",
				"\n\\begin_inset space ~\n\\end_inset\n");
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin_layout Description\n");
		sb.append(syllabary);
		if (!StringUtils.isEmpty(pronounce)) {
			sb.append(" [");
			sb.append(pronounce);
			sb.append("]");
		}
		sb.append("\n");
		if (!StringUtils.isBlank(pos)) {
			sb.append(" (");
			sb.append(pos);
			sb.append(") ");
			sb.append("\n");
		}
		sb.append(" \n");
		sb.append("\\begin_inset Quotes eld\n\\end_inset\n");
		sb.append(definition);
		sb.append("\n");
		sb.append("\\begin_inset Quotes erd\n\\end_inset\n");
		sb.append(lyxLabel(label + ""));
		sb.append("\\end_layout\n");
		return sb.toString();
	}

	private static String lyxLabel(String label) {
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin_inset CommandInset label\nLatexCommand label\nname \"");
		sb.append(label);
		sb.append("\"\n\\end_inset\n");
		return sb.toString();
	}

	private static final String[] searchList = { "?", "A.", "E.", "I.", "O.",
			"U.", "V.", "a.", "e.", "i.", "o.", "u.", "v.", "1", "2", "3", "4" };
	private static final String[] replacementList = { "ɂ", "̣A", "̣E", "Ị",
			"Ọ", "Ụ", "Ṿ", "ạ", "ẹ", "ị", "ọ", "ụ", "ṿ", "¹", "²", "³", "⁴" };

	private static boolean fixToneCadenceMarks(DefinitionLine def) {
		def.pronounce = StringUtils.replaceEach(def.pronounce, searchList,
				replacementList);
		if (def.pronounce.matches(".*" + Pattern.quote(".") + ".*")) {
			return false;
		}
		if (def.pronounce.matches(".*\\d.*")) {
			return false;
		}
		if (def.pronounce.matches(".*¹(¹²³⁴).*")) {
			return false;
		}
		if (def.pronounce.matches(".*⁴(¹²³⁴).*")) {
			return false;
		}
		if (def.pronounce.matches(".*²(¹²⁴).*")) {
			return false;
		}
		if (def.pronounce.matches(".*³(¹³⁴).*")) {
			return false;
		}
		return true;
	}

}
