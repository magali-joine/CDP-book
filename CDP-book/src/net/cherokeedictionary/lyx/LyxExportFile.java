package net.cherokeedictionary.lyx;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.lyx.LyxEntry.AdjectivialEntry;
import net.cherokeedictionary.lyx.LyxEntry.ConjunctionEntry;
import net.cherokeedictionary.lyx.LyxEntry.DefinitionLine;
import net.cherokeedictionary.lyx.LyxEntry.HasNormalized;
import net.cherokeedictionary.lyx.LyxEntry.InterjectionEntry;
import net.cherokeedictionary.lyx.LyxEntry.NounEntry;
import net.cherokeedictionary.lyx.LyxEntry.OtherEntry;
import net.cherokeedictionary.lyx.LyxEntry.PostPositionEntry;
import net.cherokeedictionary.lyx.LyxEntry.PronounEntry;
import net.cherokeedictionary.lyx.LyxEntry.VerbEntry;
import net.cherokeedictionary.main.App;
import net.cherokeedictionary.main.DbEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class LyxExportFile extends Thread {

	private static final String sloppy_begin = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status collapsed\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "begin{sloppy}\n" + "\\end_layout\n" + "\n" + "\\end_inset\n"
			+ "\n" + "\n" + "\\end_layout\n\n";

	private static final String sloppy_end = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status collapsed\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "end{sloppy}\n" + "\\end_layout\n" + "\n" + "\\end_inset\n"
			+ "\n" + "\n" + "\\end_layout\n\n";

	private static final String columnsep_large = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnsep}{20pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n" + "\n";
	private static final String columnsep_normal = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnsep}{10pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n" + "\n";
	private static final String seprule_on = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnseprule}{0.5pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n";
	private static final String seprule_off = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnseprule}{0pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n";
	private static final String MULTICOLS_END = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status collapsed\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "end{multicols}\n" + "\\end_layout\n" + "\n" + "\\end_inset\n"
			+ "\n" + "\n" + "\\end_layout\n";
	private static final String MULTICOLS_BEGIN = "\\begin_layout Standard\n"
			+ "\n" + "\\lang english\n" + "\\begin_inset ERT\n"
			+ "status collapsed\n" + "\n" + "\\begin_layout Plain Layout\n"
			+ "\n" + "\n" + "\\backslash\n" + "begin{multicols}{2}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n";
	private static final String Chapter_Dictionary = "\\begin_layout Chapter\n"
			+ "Dictionary\n" + "\\end_layout\n";
	private static final String Chapter_WordForms = "\\begin_layout Chapter\n"
			+ "Word Form Lookup\n" + "\\end_layout\n";
	private static final String Chapter_English = "\\begin_layout Chapter\n"
			+ "English to Cherokee Lookup\n" + "\\end_layout\n";
	private final Db dbc;
	private final String lyxfile;
	private final String formsfile;

	public LyxExportFile(Db dbc, String lyxfile, String formsfile) {
		this.dbc = dbc;
		this.lyxfile = lyxfile;
		this.formsfile=formsfile;
	}

	@Override
	public void run() {
		try {
			_run();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void _run() throws IOException {

		StringBuilder lyxdoc = new StringBuilder();
		String start = IOUtils.toString(getClass().getResourceAsStream(
				"/net/cherokeedictionary/lyx/LyxDocumentStart.txt"));
		lyxdoc.append(start);
		String end = IOUtils.toString(getClass().getResourceAsStream(
				"/net/cherokeedictionary/lyx/LyxDocumentEnd.txt"));
		List<DbEntry> entries = getEntries();
		removeUnwantedEntries(entries);

		removeEntriesWithMissingPronunciations(entries);
		removeEntriesWithInvalidSyllabary(entries);
		removeEntriesWithBogusDefinitions(entries);
		List<LyxEntry> definitions = processIntoEntries(entries);

		NumberFormat nf = NumberFormat.getInstance();
		App.info("Loaded " + nf.format(definitions.size()) + " definitions.");
		Iterator<LyxEntry> ilyx = definitions.iterator();
		int verbs = 0;
		int nouns = 0;
		int advadvs = 0;
		int interjects = 0;
		int posts = 0;
		int prons = 0;
		int other = 0;
		int conjs = 0;
		while (ilyx.hasNext()) {
			LyxEntry entry = ilyx.next();
			if (entry instanceof ConjunctionEntry) {
				conjs++;
				continue;
			}
			if (entry instanceof PronounEntry) {
				prons++;
				continue;
			}
			if (entry instanceof PostPositionEntry) {
				posts++;
				continue;
			}
			if (entry instanceof InterjectionEntry) {
				interjects++;
				continue;
			}
			if (entry instanceof VerbEntry) {
				verbs++;
				continue;
			}
			if (entry instanceof NounEntry) {
				nouns++;
				continue;
			}
			if (entry instanceof AdjectivialEntry) {
				advadvs++;
				continue;
			}
			if (entry instanceof OtherEntry) {
				App.info("OTHER: " + entry.pos);
				other++;
				continue;
			}
			App.err("\t" + entry.getClass().getSimpleName());
		}
		App.info("---");
		App.info("STATISTICS ON DBENTRIES THAT PASSED INITIAL SCREENING");
		App.info("---");
		App.info("\tFound " + nf.format(verbs) + " verb entries.");
		App.info("\tFound " + nf.format(nouns) + " noun entries.");
		App.info("\tFound " + nf.format(advadvs) + " adjectivial entries.");
		App.info("\tFound " + nf.format(interjects) + " interjection entries.");
		App.info("\tFound " + nf.format(posts) + " post-position entries.");
		App.info("\tFound " + nf.format(prons) + " pronoun entries.");
		App.info("\tFound " + nf.format(conjs) + " conjunction entries.");
		App.info("\tFound " + nf.format(other) + " other entries.");
		App.info("---");

		// addSeeAlsoEntries(definitions);

		Collections.sort(definitions);

		/*
		 * Build up word forms reference
		 */
		List<WordForm> wordforms = new ArrayList<>();
		Iterator<LyxEntry> idef = definitions.iterator();
		while (idef.hasNext()) {
			LyxEntry next = idef.next();
			List<String> list = next.getSyllabary();
			String primary_entry = list.get(0);
			if (primary_entry.contains(",")) {
				primary_entry = StringUtils.substringBefore(primary_entry, ",");
			}
			primary_entry = StringUtils.strip(primary_entry);
			if (next instanceof HasNormalized) {
				List<String> normal = (((HasNormalized) next).getNormalized());
				if (normal.size()!=0) {
					list.clear();
				}
				//normal.removeAll(list);
				list.addAll(normal);
			}
			Iterator<String> isyl = list.iterator();
			while (isyl.hasNext()) {
				for (String syllabary : StringUtils.split(isyl.next(), ",")) {
					syllabary=StringUtils.strip(syllabary);
					if (StringUtils.isBlank(syllabary)) {
						continue;
					}
					if (StringUtils.isBlank(syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""))) {
						continue;
					}
					if (syllabary.length() < 2) {
//						continue;
					}					
					WordForm wf = new WordForm();
					wf.being_looked_up = syllabary;
					wf.references.add(new Reference(primary_entry, "", next.id));
					wordforms.add(wf);
				}
			}
		}
		Collections.sort(wordforms);
		App.info("Pre-combined and pre-deduped Wordform entries: "
				+ nf.format(wordforms.size()));
		for (int ix = 1; ix < wordforms.size(); ix++) {
			WordForm e1 = wordforms.get(ix - 1);
			WordForm e2 = wordforms.get(ix);
			if (e1.being_looked_up.equals(e2.being_looked_up)) {
				e2.references.removeAll(e1.references);
				e1.references.addAll(e2.references);
				WordForm.dedupeBySyllabary(e1.references);
				wordforms.remove(ix);
				ix--;
				continue;
			}
		}
		App.info("Post-combined Wordform entries: "
				+ nf.format(wordforms.size()));
		/*
		 * Save out wordforms into a special lookup file for use by other softwares.
		 */
		StringBuilder sbwf = new StringBuilder();
		for(WordForm wordform: wordforms) {
			if (wordform.being_looked_up.contains(" ")){
				continue;
			}
			sbwf.append(wordform.being_looked_up);
			for (Reference ref: wordform.references) {
				sbwf.append("\t");
				sbwf.append(ref.syllabary);
			}
			sbwf.append("\n");
		}
		FileUtils.writeStringToFile(new File(formsfile), sbwf.toString(), "UTF-8");

		/*
		 * Build up english to cherokee reference
		 */
		List<EnglishCherokee> english = new ArrayList<>();
		idef = definitions.iterator();
		while (idef.hasNext()) {
			LyxEntry next = idef.next();
			String syllabary = next.getSyllabary().get(0);
			String pronounce = next.getPronunciations().get(0);
			if (syllabary.contains(",")) {
				syllabary = StringUtils.substringBefore(syllabary, ",");
				syllabary = StringUtils.strip(syllabary);
			}
			if (pronounce.contains(",")) {
				pronounce = StringUtils.substringBefore(pronounce, ",");
				pronounce = StringUtils.strip(pronounce);
			}
			String def = next.definition;
			int forLabel = next.id;
			EnglishCherokee ec = new EnglishCherokee();
			ec.setEnglish(def);
			ec.refs.add(new Reference(syllabary, pronounce, forLabel));
			english.addAll(getSplitsFor(ec));
		}
		Collections.sort(english);
		App.info("Pre-combined English to Cherokee entries: "
				+ nf.format(english.size()));
		for (int ix = 1; ix < english.size(); ix++) {
			if (english.get(ix - 1).equals(english.get(ix))) {
				english.remove(ix);
				ix--;
			}
		}
		App.info("Deduped English to Cherokee entries: "
				+ nf.format(english.size()));
		for (int ix = 1; ix < english.size(); ix++) {
			EnglishCherokee e1 = english.get(ix - 1);
			EnglishCherokee e2 = english.get(ix);
			if (e1.getDefinition().equals(e2.getDefinition())) {
				e1.refs.addAll(e2.refs);
				english.remove(ix);
				ix--;
				continue;
			}
		}
		App.info("Post-combined English to Cherokee entries: "
				+ nf.format(english.size()));

		StringBuilder sb=new StringBuilder();
		
		/*
		 * Start of Book including all front matter
		 */
		sb.append(start);
		/*
		 * Cherokee Dictionary
		 */
		sb.append(Chapter_Dictionary + columnsep_large + seprule_on
				+ MULTICOLS_BEGIN + sloppy_begin);
		String prevSection = "";
		for (LyxEntry entry : definitions) {
			String syll = StringUtils.left(
					entry.getLyxCode().replaceAll("[^Ꭰ-Ᏼ]", ""), 1);
			if (!syll.equals(prevSection)) {
				prevSection = syll;
				sb.append("\\begin_layout Section\n");
				sb.append(syll);
				sb.append("\\end_layout\n");
			}
			sb.append(entry.getLyxCode().replace("\\n", " "));
			if (entry.examples.size() != 0) {
				sb.append("\\begin_deeper\n");
				for (ExampleEntry ee : entry.examples) {
					sb.append(ee.getLyxCode());
				}
				sb.append("\\end_deeper\n");
			}
			Iterator<CrossReference> icross = entry.crossrefs.iterator();
			if (icross.hasNext()) {
				sb.append("\\begin_deeper\n");
				StringBuilder sb1 = new StringBuilder();
				sb1.append("\\begin_layout Standard\n");
				sb1.append("\\noindent\n");
				sb1.append("\\align left\n");
				sb1.append("\\emph on\n");
				sb1.append("cf: ");
				sb1.append("\\emph default\n");
//				sb1.append("\\end_layout\n");
				sb.append(sb1.toString());
				sb.append(icross.next().getLyxCode(true));
				while (icross.hasNext()) {
					sb.append(", " + icross.next().getLyxCode(true));
				}
				sb.append("\\end_deeper\n");
			}
		}
		sb.append(sloppy_end + MULTICOLS_END + seprule_off
				+ columnsep_normal);

		/*
		 * Wordform Lookup
		 */
		sb.append(Chapter_WordForms + columnsep_large + seprule_on
				+ MULTICOLS_BEGIN + sloppy_begin);
		prevSection = "";
		for (WordForm entry : wordforms) {
			if (entry.being_looked_up.contains(" ")){
				continue;
			}
			String syll = StringUtils.left(entry.being_looked_up, 1);
			if (!syll.equals(prevSection)) {
				prevSection = syll;
				sb.append("\\begin_layout Section\n");
				sb.append(syll);
				sb.append("\\end_layout\n");
			}
			sb.append(entry.getLyxCode());
		}
		sb.append(sloppy_end + MULTICOLS_END + seprule_off
				+ columnsep_normal);
		
		/*
		 * English to Cherokee
		 */
		sb.append(Chapter_English + columnsep_large + seprule_on
				+ MULTICOLS_BEGIN + sloppy_begin);
		prevSection = "";
		for (EnglishCherokee entry : english) {
			String eng = StringUtils.left(entry.getDefinition(), 1)
					.toUpperCase();
			if (!eng.equals(prevSection)) {
				prevSection = eng;
				sb.append("\\begin_layout Section\n");
				sb.append(eng.toUpperCase());
				sb.append("\\end_layout\n");
			}
			sb.append(entry.getLyxCode(true));
		}
		sb.append(sloppy_end + MULTICOLS_END + seprule_off
				+ columnsep_normal);
		sb.append(end);
		FileUtils.writeStringToFile(new File(lyxfile), sb.toString(), "UTF-8", false);
	}

	/*
	 * is this a "splittable" definition?
	 */
	private List<EnglishCherokee> getSplitsFor(EnglishCherokee ec) {
		List<EnglishCherokee> list = new ArrayList<>();
		splitAndAdd: {
			// keep this one first
			String definition = ec.getDefinition();
			if (definition.startsWith("1")) {
				String defs[] = definition.split("\\d ?\\.? ?");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}

			if (definition.contains("\\/")) {
				String defs[] = definition.split("\\/");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			// he has
			if (definition.contains(", he has")
					|| definition.contains(",he has")) {
				String defs[] = definition.split(", ?he ");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			if (definition.contains(", she has")
					|| definition.contains(",she has")) {
				String defs[] = definition.split(", ?she ");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			if (definition.contains(" him,")
					&& !definition.contains(" him, it")
					&& !definition.contains(" him, her")) {
				String defs[] = definition.split(" him,");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef + " him");
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			if (definition.contains(", his ") || definition.contains(",his ")) {
				String defs[] = definition.split(", ?his ");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			if (definition.contains(", her ") || definition.contains(",her ")) {
				String defs[] = definition.split(", ?her ");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			if (definition.contains(", it's") || definition.contains(",it's")) {
				String defs[] = definition.split(", ?it's");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			if (definition.contains(", he's") || definition.contains(",he's")) {
				String defs[] = definition.split(", ?he's");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			if (definition.contains(", she's") || definition.contains(",she's")) {
				String defs[] = definition.split(", ?she's");
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			list.add(ec);
		}
		return list;
	}

	@SuppressWarnings("unused")
	private void addSeeAlsoEntries(List<LyxEntry> definitions) {
		int size = definitions.size();
		for (int ia = 0; ia < size; ia++) {
			LyxEntry next = definitions.get(ia);
			String syll = next.getSyllabary().get(0);
			if (!syll.contains(",")) {
				continue;
			}
			String pron = next.getPronunciations().get(0);
			if (StringUtils.countMatches(syll, ",") != StringUtils
					.countMatches(pron, ",")) {
				App.err("Mismatched SYLLABARY vs PRONOUNCE: '" + pron + "', '"
						+ syll + "'");
				continue;
			}
			String[] s = syll.split(",");
			String[] p = pron.split(",");
			for (int ix = 1; ix < s.length; ix++) {
				OtherEntry seealso = new OtherEntry();
				seealso.id = next.id;
				seealso.pos = next.pos;
				seealso.definition = next.definition;
				seealso.other = new DefinitionLine();
				seealso.other.pronounce = StringUtils.strip(p[ix]);
				seealso.other.syllabary = StringUtils.strip(s[ix]);
				seealso.crossrefs.add(new CrossReference(next.id, s[0], s[0]));
				definitions.add(seealso);
			}
		}
	}

	private void removeEntriesWithBogusDefinitions(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (entry.definitiond.startsWith("(see")) {
				App.err("Bad definition: " + entry.entrya + ": "
						+ entry.definitiond);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.definitiond)) {
				App.err("Empty definition: " + entry.entrya + ": "
						+ entry.syllabaryb);
				ientry.remove();
				continue;
			}
		}
	}

	private List<LyxEntry> processIntoEntries(List<DbEntry> entries) {
		Map<String, Integer> crossrefs_id = new HashMap<>();
		Map<Integer, String> crossrefs_syll = new HashMap<>();
		List<LyxEntry> definitions = new ArrayList<>();
		Iterator<DbEntry> ientries = entries.iterator();
		while (ientries.hasNext()) {
			DbEntry entry = ientries.next();
			LyxEntry entryFor = LyxEntry.getEntryFor(entry);
			if (entryFor != null) {
				LyxEntry.fillinExampleSentences(entryFor, entry);
				definitions.add(entryFor);
				for (String entrya : entry.entrya.split(",")) {
					entrya = entrya.replace("\\n", " ");
					entrya = StringUtils.strip(entrya).toLowerCase();
					crossrefs_id.put(entrya, entryFor.id);
					String cfsyll = entryFor.getSyllabary().get(0);
					if (cfsyll.contains(",")) {
						cfsyll = StringUtils.substringBefore(cfsyll, ",");
						cfsyll = StringUtils.strip(cfsyll);
					}
					crossrefs_syll.put(entryFor.id, cfsyll);
				}
				entryFor.crossrefstxt = entry.crossreferencet;
			}
		}
		Iterator<LyxEntry> idef = definitions.iterator();
		while (idef.hasNext()) {
			LyxEntry def = idef.next();
			if (StringUtils.isEmpty(def.crossrefstxt)) {
				continue;
			}
			String[] refs = def.crossrefstxt.replace("\\n", " ").split(",");
			for (String ref : refs) {
				String pronounce = def.getPronunciations().get(0);
				ref = StringUtils.strip(ref);
				String xref = ref.toLowerCase();
				xref = StringUtils.substringBefore(xref, "(");
				xref = StringUtils.strip(xref);
				if (xref.startsWith("cf ")) {
					App.err("BAD CROSS-REFERENCE: '" + pronounce + "' cf '"
							+ xref + "' from '" + def.crossrefstxt + "'");
					xref = StringUtils.substring(xref, 3);
				}
				if (!crossrefs_id.containsKey(xref)) {
					App.err("MISSING CROSS-REFERENCE '" + pronounce + "' cf '"
							+ xref + "' from '" + def.crossrefstxt + "'");
					continue;
				}
				Integer xid = crossrefs_id.get(xref);
				String syllabary = crossrefs_syll.get(xid);
				def.crossrefs.add(new CrossReference(xid, ref, syllabary));
			}
		}
		return definitions;
	}

	private void removeEntriesWithInvalidSyllabary(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (!StringUtils.isEmpty(entry.syllabaryb.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.syllabaryb);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.nounadjpluralsyllf.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vfirstpresh.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vsecondimpersylln.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdinfsyllp.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vthirdinfsyllp);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdpastsyllj.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdpressylll.replaceAll(
					"[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				App.err("Bad Syllabary: " + entry.entrya + ", "
						+ entry.vthirdpressylll);
				ientry.remove();
				continue;
			}
		}
	}

	private void removeEntriesWithMissingPronunciations(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (StringUtils.isEmpty(entry.entrytone)) {
				App.err("Missing entrya: " + entry.entrya);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.nounadjpluraltone.replace("-", "")) != StringUtils
					.isEmpty(entry.nounadjpluralsyllf.replace("-", ""))) {
				App.err("Missing nounadjpluraltone or nounadjpluralsyllf: "
						+ entry.entrya + ", " + entry.nounadjpluraltone + "|"
						+ entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vfirstprestone.replace("-", "")) != StringUtils
					.isEmpty(entry.vfirstpresh.replace("-", ""))) {
				App.err("Missing vfirstprestone or vfirstpresh: "
						+ entry.entrya + ", " + entry.vfirstprestone + "|"
						+ entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vsecondimpertone.replace("-", "")) != StringUtils
					.isEmpty(entry.vsecondimpersylln.replace("-", ""))) {
				App.err("Missing vsecondimpertone or vsecondimpersylln: "
						+ entry.entrya + ", " + entry.vsecondimpertone + "|"
						+ entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vthirdpasttone.replace("-", "")) != StringUtils
					.isEmpty(entry.vthirdpastsyllj.replace("-", ""))) {
				App.err("Missing vthirdpasttone or vthirdpastsyllj: "
						+ entry.entrya + ", " + entry.vthirdpasttone + "|"
						+ entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vthirdprestone.replace("-", "")) != StringUtils
					.isEmpty(entry.vthirdpressylll.replace("-", ""))) {
				App.err("Missing vthirdprestone or vthirdpressylll: "
						+ entry.entrya + ", " + entry.vthirdprestone + "|"
						+ entry.vthirdpressylll);
				ientry.remove();
				continue;
			}
		}
	}

	/**
	 * We don't want "empty", "word parts", or
	 * "Cross references to the not-included Grammar"
	 * 
	 * @param entries
	 */
	private void removeUnwantedEntries(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (entry.syllabaryb.contains("-")) {
				ientry.remove();
				continue;
			}
			if (entry.entrya.startsWith("-")) {
				ientry.remove();
				continue;
			}
			if (entry.entrya.endsWith("-")) {
				ientry.remove();
				continue;
			}
			if (entry.definitiond.contains("see Gram")) {
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.syllabaryb)) {
				App.err("No Syllabary: " + entry.entrya + " = "
						+ entry.definitiond);
				ientry.remove();
				continue;
			}
		}

	}

	private List<DbEntry> getEntries() {
		int counter = 0;
		List<DbEntry> list = new ArrayList<>();
		try (Connection db = dbc.makeConnection()) {
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery("select * from likespreadsheets");
			while (rs.next()) {
				DbEntry entry = new DbEntry();
				for (Field f : DbEntry.class.getFields()) {
					String simpleName = f.getType().getSimpleName();
					if (simpleName.equals("String")) {
						String name = f.getName();
						f.set(entry, rs.getString(name));
					}
				}
				entry.id = counter++;
				list.add(entry);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return list;
	}
}
