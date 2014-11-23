package net.cherokeedictionary.lyx;

import org.apache.commons.lang3.StringUtils;

import net.cherokeedictionary.main.DbEntry;
import net.cherokeedictionary.main.JsonConverter;

public abstract class LyxEntry {

	protected int id;
	public String pos = null;
	public String definition = null;

	public abstract String getLyxCode();

	private static void makeBlankIfEmptyLine(DefinitionLine line) {
		boolean b = StringUtils.isEmpty(line.pronounce.replace("-", ""));
		b |= StringUtils.isEmpty(line.syllabary.replace("-", ""));
		if (!b) {
			return;
		}
		line.pronounce = "";
		line.syllabary = "";
	}

	public static LyxEntry getEntryFor(DbEntry dbentry) {
		if (dbentry.partofspeechc.startsWith("v")) {

			if (warnIfNonVerbData(dbentry)) {
				return null;
			}

			VerbEntry entry = new VerbEntry();
			entry.id = dbentry.id;
			entry.pos = dbentry.partofspeechc;
			entry.definition = dbentry.definitiond;

			entry.present3rd = new DefinitionLine();
			entry.present3rd.syllabary = dbentry.syllabaryb;
			entry.present3rd.pronounce = dbentry.entrytone;
			makeBlankIfEmptyLine(entry.present3rd);

			entry.present1st = new DefinitionLine();
			entry.present1st.syllabary = dbentry.vfirstpresh;
			entry.present1st.pronounce = dbentry.vfirstprestone;
			makeBlankIfEmptyLine(entry.present1st);

			entry.remotepast = new DefinitionLine();
			entry.remotepast.syllabary = dbentry.vthirdpastsyllj;
			entry.remotepast.pronounce = dbentry.vthirdpasttone;
			makeBlankIfEmptyLine(entry.remotepast);

			entry.habitual = new DefinitionLine();
			entry.habitual.syllabary = dbentry.vthirdpressylll;
			entry.habitual.pronounce = dbentry.vthirdprestone;
			makeBlankIfEmptyLine(entry.habitual);

			entry.imperative = new DefinitionLine();
			entry.imperative.syllabary = dbentry.vsecondimpersylln;
			entry.imperative.pronounce = dbentry.vsecondimpertone;
			makeBlankIfEmptyLine(entry.imperative);

			entry.infinitive = new DefinitionLine();
			entry.infinitive.syllabary = dbentry.vthirdinfsyllp;
			entry.infinitive.pronounce = dbentry.vthirdinftone;
			makeBlankIfEmptyLine(entry.infinitive);
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("n")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			NounEntry entry = new NounEntry();
			entry.id = dbentry.id;
			entry.pos = dbentry.partofspeechc;
			entry.definition = dbentry.definitiond;
			
			entry.single=new DefinitionLine();
			entry.single.pronounce=dbentry.entrytone;
			entry.single.syllabary=dbentry.syllabaryb;
			
			entry.plural=new DefinitionLine();
			entry.plural.pronounce=dbentry.nounadjpluraltone;
			entry.plural.syllabary=dbentry.nounadjpluralsyllf;
			return entry;
		}
		if (dbentry.partofspeechc.startsWith("ad")) {
			if (warnIfVerbData(dbentry)) {
				return null;
			}
			AdjectivialEntry entry = new AdjectivialEntry();
			entry.id = dbentry.id;
			entry.pos = dbentry.partofspeechc;
			entry.definition = dbentry.definitiond;
			
			entry.single_in=new DefinitionLine();
			entry.single_in.pronounce=dbentry.entrytone;
			entry.single_in.syllabary=dbentry.syllabaryb;
			
			entry.plural_in=new DefinitionLine();
			entry.plural_in.pronounce=dbentry.nounadjpluraltone;
			entry.plural_in.pronounce=dbentry.nounadjpluralsyllf;
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
			entry.definition = dbentry.definitiond;
			
			entry.interj=new DefinitionLine();
			entry.interj.pronounce=dbentry.entrytone;
			entry.interj.syllabary=dbentry.syllabaryb;
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
			entry.definition = dbentry.definitiond;
			
			entry.post=new DefinitionLine();
			entry.post.pronounce=dbentry.entrytone;
			entry.post.syllabary=dbentry.syllabaryb;
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
			entry.definition = dbentry.definitiond;
			
			entry.conjunction=new DefinitionLine();
			entry.conjunction.pronounce=dbentry.entrytone;
			entry.conjunction.syllabary=dbentry.syllabaryb;
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
			entry.definition = dbentry.definitiond;
			
			entry.pronoun=new DefinitionLine();
			entry.pronoun.pronounce=dbentry.entrytone;
			entry.pronoun.syllabary=dbentry.syllabaryb;
			return entry;
		}
		OtherEntry entry = new OtherEntry();
		if (warnIfVerbData(dbentry)) {
			return null;
		}
		entry.id = dbentry.id;
		entry.pos = dbentry.partofspeechc;
		entry.definition = dbentry.definitiond;
		return entry;
	}

	private static boolean warnIfNonVerbData(DbEntry dbentry) {
		boolean valid = true;
		valid &= StringUtils.isEmpty(dbentry.nounadjplurale);
		valid &= StringUtils.isEmpty(dbentry.nounadjpluralsyllf);
		valid &= StringUtils.isEmpty(dbentry.nounadjpluraltone);
		valid &= StringUtils.isEmpty(dbentry.nounadjpluraltranslit);
		if (!valid) {
			System.err
					.println("Warning - NON-VERB DATA FOUND IN VERB DB ENTRY: "
							+ dbentry.entrya + " (" + dbentry.partofspeechc
							+ ")" + " = " + dbentry.definitiond);
			String string = new JsonConverter().toJson(dbentry);
			string = string.replaceAll("\"[^\"]+\":\"\",?", "");
			System.err.println("\t" + string);
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
			System.err
					.println("Warning - VERB DATA FOUND IN NON-VERB DB ENTRY: "
							+ dbentry.entrya + " (" + dbentry.partofspeechc
							+ ")" + " = " + dbentry.definitiond);
			String string = new JsonConverter().toJson(dbentry);
			string = string.replaceAll("\"[^\"]+\":\"\",?", "");
			System.err.println("\t" + string);
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

	public static class VerbEntry extends LyxEntry {
		public DefinitionLine present3rd = null;
		public DefinitionLine present1st = null;
		public DefinitionLine remotepast = null;
		public DefinitionLine habitual = null;
		public DefinitionLine imperative = null;
		public DefinitionLine infinitive = null;
		public ExampleLine[] example = null;

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class InterjectionEntry extends LyxEntry {
		public DefinitionLine interj;
		public ExampleLine[] example = null;

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class ConjunctionEntry extends LyxEntry {
		public DefinitionLine conjunction;
		public ExampleLine[] example = null;

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class PronounEntry extends LyxEntry {
		public DefinitionLine pronoun;
		public ExampleLine[] example = null;

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class PostPositionEntry extends LyxEntry {
		public DefinitionLine post;
		public ExampleLine[] example = null;
		public PostPositionEntry() {
			super();
			this.pos="postp.";
		}
		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class NounEntry extends LyxEntry {
		public DefinitionLine single;
		public DefinitionLine plural;
		public ExampleLine[] example = null;

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class AdjectivialEntry extends LyxEntry {
		public DefinitionLine single_in;
		public DefinitionLine single_an;
		public DefinitionLine plural_in;
		public DefinitionLine plural_an;
		public ExampleLine[] example = null;

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class OtherEntry extends LyxEntry {

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static class BodyPart extends LyxEntry {

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
