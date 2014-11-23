package net.cherokeedictionary.lyx;

import net.cherokeedictionary.main.DbEntry;

import org.apache.commons.lang3.StringUtils;


public abstract class LyxEntry {
	
	protected int id;
	public String pos;
	public String definition;
	
//	private DbEntry dbentry;
	
	public abstract String getLyxCode();

	public LyxEntry parse(DbEntry dbentry) {
//		this.dbentry = dbentry;
		entrymaker: {
			if (dbentry.partofspeechc.startsWith("v")) {
				VerbEntry entry = new VerbEntry();
				entry.id=dbentry.id;
				entry.pos=dbentry.partofspeechc;
				entry.definition=dbentry.definitiond;
				
				entry.present3rd.syllabary=dbentry.syllabaryb;
				entry.present3rd.pronounce=dbentry.entrytone;
				
				entry.present1st.syllabary=dbentry.vfirstpresh;
				entry.present1st.pronounce=dbentry.vfirstprestone;
				
				entry.remotepast.syllabary=dbentry.vthirdpastsyllj;
				entry.remotepast.pronounce=dbentry.vthirdpasttone;
				
				entry.habitual.syllabary=dbentry.vthirdpressylll;
				entry.habitual.pronounce=dbentry.vthirdprestone;
				
				entry.imperative.syllabary=dbentry.vsecondimpersylln;
				entry.imperative.pronounce=dbentry.vsecondimpertone;
				
				entry.infinitive.syllabary=dbentry.vthirdinfsyllp;
				entry.infinitive.pronounce=dbentry.vthirdinftone;				
				break entrymaker;
			}
			
		}
		return null;
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
		public DefinitionLine present3rd;
		public DefinitionLine present1st;
		public DefinitionLine remotepast;
		public DefinitionLine habitual;
		public DefinitionLine imperative;
		public DefinitionLine infinitive;
		public ExampleLine example;
		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class NounEntry extends LyxEntry {

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class AdjEntry extends LyxEntry {

		@Override
		public String getLyxCode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static class AdvEntry extends LyxEntry {

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
