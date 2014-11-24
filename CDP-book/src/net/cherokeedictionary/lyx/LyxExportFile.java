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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.lyx.LyxEntry.AdjectivialEntry;
import net.cherokeedictionary.lyx.LyxEntry.ConjunctionEntry;
import net.cherokeedictionary.lyx.LyxEntry.InterjectionEntry;
import net.cherokeedictionary.lyx.LyxEntry.NounEntry;
import net.cherokeedictionary.lyx.LyxEntry.OtherEntry;
import net.cherokeedictionary.lyx.LyxEntry.PostPositionEntry;
import net.cherokeedictionary.lyx.LyxEntry.PronounEntry;
import net.cherokeedictionary.lyx.LyxEntry.VerbEntry;
import net.cherokeedictionary.main.DbEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class LyxExportFile extends Thread {

	private static final String MULTICOLS_END = "\\begin_layout Standard\n" + 
			"\\begin_inset ERT\n" + 
			"status collapsed\n" + 
			"\n" + 
			"\\begin_layout Plain Layout\n" + 
			"\n" + 
			"\n" + 
			"\\backslash\n" + 
			"end{multicols}\n" + 
			"\\end_layout\n" + 
			"\n" + 
			"\\end_inset\n" + 
			"\n" + 
			"\n" + 
			"\\end_layout\n";
	private static final String MULTICOLS_BEGIN = "\\begin_layout Standard\n"
					+ "\n" + "\\lang english\n" + "\\begin_inset ERT\n"
					+ "status collapsed\n" + "\n" + "\\begin_layout Plain Layout\n"
					+ "\n" + "\n" + "\\backslash\n" + "begin{multicols}{2}\n"
					+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
					+ "\\end_layout\n";
	private static final String Chapter_Dictionary = "\\begin_layout Chapter\n" + 
					"Dictionary\n" + 
					"\\end_layout\n";
	private static final String Chapter_WordForms = "\\begin_layout Chapter\n" + 
			"Word Form Lookup\n" + 
			"\\end_layout\n";
	private static final String Chapter_English = "\\begin_layout Chapter\n" + 
			"English to Cherokee Lookup\n" + 
			"\\end_layout\n";
	private final Db dbc;
	private final String lyxfile;

	public LyxExportFile(Db dbc, String lyxfile) {
		this.dbc = dbc;
		this.lyxfile = lyxfile;
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
		fixupPronunciations(entries);
		removeEntriesWithMissingPronunciations(entries);
		removeEntriesWithInvalidSyllabary(entries);
		List<LyxEntry> definitions=processIntoEntries(entries);
		
		NumberFormat nf = NumberFormat.getInstance();
		System.out.println("Loaded "+nf.format(definitions.size())+" definitions.");
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
				System.out.println("OTHER: "+entry.pos);
				other++;
				continue;
			}
			System.err.println("\t"+entry.getClass().getSimpleName());
		}
		System.out.println("\tFound "+nf.format(verbs)+" verb entries.");
		System.out.println("\tFound "+nf.format(nouns)+" noun entries.");
		System.out.println("\tFound "+nf.format(advadvs)+" adjectivial entries.");
		System.out.println("\tFound "+nf.format(interjects)+" interjection entries.");
		System.out.println("\tFound "+nf.format(posts)+" post-position entries.");
		System.out.println("\tFound "+nf.format(prons)+" pronoun entries.");
		System.out.println("\tFound "+nf.format(conjs)+" conjunction entries.");
		System.out.println("\tFound "+nf.format(other)+" other entries.");
		
		Collections.sort(definitions);
		
		/*
		 * Build up word forms reference
		 */
		List<WordForm> wordforms = new ArrayList<>();
		Iterator<LyxEntry> idef = definitions.iterator();
		while (idef.hasNext()) {
			LyxEntry next = idef.next();
			Iterator<String> isyl = next.getSyllabary().iterator();
			while (isyl.hasNext()) {
				String syllabary = isyl.next();
				if (StringUtils.isEmpty(syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""))){
					continue;
				}
				WordForm wf = new WordForm();
				wf.syllabary=syllabary;
				wf.references=next.getSyllabary().get(0);
				wf.toLabel=next.id;
				wordforms.add(wf);
			}
		}
		Collections.sort(wordforms);
		for (int ix=1; ix<wordforms.size(); ix++) {
			if (wordforms.get(ix-1).equals(wordforms.get(ix))) {
				wordforms.remove(ix);
				ix--;
			}
		}
		
		/*
		 * Build up english to cherokee reference
		 */
		List<EnglishCherokee> english = new ArrayList<>();
		idef = definitions.iterator();
		while (idef.hasNext()) {
			LyxEntry next = idef.next();
			String syllabary = next.getSyllabary().get(0);
			String def = next.definition;
			int forLabel = next.id;
			EnglishCherokee ec = new EnglishCherokee();
			ec.setEnglish(def);
			ec.syllabary=syllabary;
			ec.toLabel=forLabel;
			ec.pronounce=next.getPronunciations().get(0);
			english.add(ec);
		}
		Collections.sort(english);
		for (int ix=1; ix<english.size(); ix++) {
			if (english.get(ix-1).equals(english.get(ix))) {
				english.remove(ix);
				ix--;
			}
		}
		
		
		File file = new File(lyxfile);
		if (file.exists()) {
			file.delete();
		}
		FileUtils.write(file, start, "UTF-8", true);
		FileUtils.write(file, Chapter_Dictionary + MULTICOLS_BEGIN, "UTF-8", true);
		String prevSection="";
		for (LyxEntry entry: definitions) {
			String syll = StringUtils.left(entry.getLyxCode().replaceAll("[^Ꭰ-Ᏼ]", ""),1);
			if (!syll.equals(prevSection)) {
				prevSection=syll;
				FileUtils.write(file, "\\begin_layout Section\n", "UTF-8", true);
				FileUtils.write(file, syll, "UTF-8", true);
				FileUtils.write(file, "\\end_layout\n", "UTF-8", true);
			}
			FileUtils.write(file, entry.getLyxCode().replace("\\n", " "), "UTF-8", true);
		}
		FileUtils.write(file, MULTICOLS_END, "UTF-8", true);
		
		FileUtils.write(file, Chapter_WordForms + MULTICOLS_BEGIN, "UTF-8", true);
		prevSection="";
		for (WordForm entry: wordforms) {
			String syll = StringUtils.left(entry.syllabary, 1);
			if (!syll.equals(prevSection)) {
				prevSection=syll;
				FileUtils.write(file, "\\begin_layout Section\n", "UTF-8", true);
				FileUtils.write(file, syll, "UTF-8", true);
				FileUtils.write(file, "\\end_layout\n", "UTF-8", true);
			}
			FileUtils.write(file, entry.getLyxCode(), "UTF-8", true);
		}
		FileUtils.write(file, MULTICOLS_END, "UTF-8", true);
		
		FileUtils.write(file, Chapter_English + MULTICOLS_BEGIN, "UTF-8", true);
		prevSection="";
		for (EnglishCherokee entry: english) {
			String eng = StringUtils.left(entry.getDefinition(), 1).toUpperCase();
			if (!eng.equals(prevSection)) {
				prevSection=eng;
				FileUtils.write(file, "\\begin_layout Section\n", "UTF-8", true);
				FileUtils.write(file, eng.toUpperCase(), "UTF-8", true);
				FileUtils.write(file, "\\end_layout\n", "UTF-8", true);
			}
			FileUtils.write(file, entry.getLyxCode(), "UTF-8", true);
		}
		FileUtils.write(file, MULTICOLS_END, "UTF-8", true);
		
		FileUtils.write(file, end, "UTF-8", true);
	}

	private List<LyxEntry> processIntoEntries(List<DbEntry> entries) {
		List<LyxEntry> definitions=new ArrayList<>();
		Iterator<DbEntry> ientries = entries.iterator();
		while (ientries.hasNext()) {
			DbEntry entry = ientries.next();
			LyxEntry entryFor = LyxEntry.getEntryFor(entry);
			if (entryFor!=null) {
				definitions.add(entryFor);
			}
		}
		return definitions;
	}

	private void removeEntriesWithInvalidSyllabary(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (!StringUtils.isEmpty(entry.syllabaryb.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("(Removed Entry) Bad Syllabary: "+entry.entrya+", "+entry.syllabaryb);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.nounadjpluralsyllf.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("(Removed Entry) Bad Syllabary: "+entry.entrya+", "+entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vfirstpresh.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("(Removed Entry) Bad Syllabary: "+entry.entrya+", "+entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vsecondimpersylln.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("(Removed Entry) Bad Syllabary: "+entry.entrya+", "+entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdinfsyllp.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("(Removed Entry) Bad Syllabary: "+entry.entrya+", "+entry.vthirdinfsyllp);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdpastsyllj.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("(Removed Entry) Bad Syllabary: "+entry.entrya+", "+entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdpressylll.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("(Removed Entry) Bad Syllabary: "+entry.entrya+", "+entry.vthirdpressylll);
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
				System.err.println("(Removed Entry) Missing entrya: "+entry.entrya);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.nounadjpluraltone.replace("-", "")) != StringUtils.isEmpty(entry.nounadjpluralsyllf.replace("-", ""))) {
				System.err.println("(Removed Entry) Missing nounadjpluraltone or nounadjpluralsyllf: "+entry.entrya+", "+entry.nounadjpluraltone+"|"+entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vfirstprestone.replace("-", "")) != StringUtils.isEmpty(entry.vfirstpresh.replace("-", ""))) {
				System.err.println("(Removed Entry) Missing vfirstprestone or vfirstpresh: "+entry.entrya+", "+entry.vfirstprestone+"|"+entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vsecondimpertone.replace("-", "")) != StringUtils.isEmpty(entry.vsecondimpersylln.replace("-", ""))) {
				System.err.println("(Removed Entry) Missing vsecondimpertone or vsecondimpersylln: "+entry.entrya+", "+entry.vsecondimpertone+"|"+entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vthirdpasttone.replace("-", "")) != StringUtils.isEmpty(entry.vthirdpastsyllj.replace("-", ""))) {
				System.err.println("(Removed Entry) Missing vthirdpasttone or vthirdpastsyllj: "+entry.entrya+", "+entry.vthirdpasttone+"|"+entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vthirdprestone.replace("-", "")) != StringUtils.isEmpty(entry.vthirdpressylll.replace("-", ""))) {
				System.err.println("(Removed Entry) Missing vthirdprestone or vthirdpressylll: "+entry.entrya+", "+entry.vthirdprestone+"|"+entry.vthirdpressylll);
				ientry.remove();
				continue;
			}
		}		
	}

	private void fixupPronunciations(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			System.out.println("Fixing Tones: "+entry.entrya+"="+entry.definitiond);
			System.out.flush();
			entry.entrytone=fixToneCadenceMarks(entry.entrytone);
			entry.nounadjpluraltone=fixToneCadenceMarks(entry.nounadjpluraltone);
			entry.vfirstprestone=fixToneCadenceMarks(entry.vfirstprestone);
			entry.vsecondimpertone=fixToneCadenceMarks(entry.vsecondimpertone);
			entry.vthirdinftone=fixToneCadenceMarks(entry.vthirdinftone);
			entry.vthirdpasttone=fixToneCadenceMarks(entry.vthirdpasttone);
			entry.vthirdprestone=fixToneCadenceMarks(entry.vthirdprestone);
			System.err.flush();
		}
	}

	private final String[] searchList={"?", "A.", "E.", "I.", "O.", "U.", "V.", "a.", "e.", "i.", "o.", "u.", "v.", "1", "2", "3", "4"};
	private final String[] replacementList={"ɂ", "̣A", "̣E", "Ị", "Ọ", "Ụ", "Ṿ", "ạ", "ẹ", "ị", "ọ", "ụ", "ṿ", "¹", "²", "³", "⁴"};
	private String fixToneCadenceMarks(String entrytone) {
		String result=StringUtils.replaceEach(entrytone, searchList, replacementList);
		if (result.matches(".*"+Pattern.quote(".")+".*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*\\d.*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*¹(¹²³⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*⁴(¹²³⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*²(¹²⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*³(¹³⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		return result;
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
				System.err.println("(Removed Entry) No Syllabary: "+entry.entrya+" = "+entry.definitiond);
				ientry.remove();
				continue;
			}
		}

	}

	private List<DbEntry> getEntries() {
		int counter=0;
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
				entry.id=counter++;
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
