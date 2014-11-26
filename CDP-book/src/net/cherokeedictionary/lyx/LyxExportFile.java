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
	
	private static final String sloppy_begin = "\\begin_layout Standard\n" + 
			"\\begin_inset ERT\n" + 
			"status collapsed\n" + 
			"\n" + 
			"\\begin_layout Plain Layout\n" + 
			"\n" + 
			"\n" + 
			"\\backslash\n" + 
			"begin{sloppy}\n" + 
			"\\end_layout\n" + 
			"\n" + 
			"\\end_inset\n" + 
			"\n" + 
			"\n" + 
			"\\end_layout\n\n";
	
	private static final String sloppy_end = "\\begin_layout Standard\n" + 
			"\\begin_inset ERT\n" + 
			"status collapsed\n" + 
			"\n" + 
			"\\begin_layout Plain Layout\n" + 
			"\n" + 
			"\n" + 
			"\\backslash\n" + 
			"end{sloppy}\n" + 
			"\\end_layout\n" + 
			"\n" + 
			"\\end_inset\n" + 
			"\n" + 
			"\n" + 
			"\\end_layout\n\n";

	private static final String columnsep_large = "\\begin_layout Standard\n" + 
			"\\begin_inset ERT\n" + 
			"status open\n" + 
			"\n" + 
			"\\begin_layout Plain Layout\n" + 
			"\n" + 
			"\n" + 
			"\\backslash\n" + 
			"setlength{\n" + 
			"\\backslash\n" + 
			"columnsep}{20pt}\n" + 
			"\\end_layout\n" + 
			"\n" + 
			"\\end_inset\n" + 
			"\n" + 
			"\n" + 
			"\\end_layout\n" + 
			"\n";
	private static final String columnsep_normal = "\\begin_layout Standard\n" + 
			"\\begin_inset ERT\n" + 
			"status open\n" + 
			"\n" + 
			"\\begin_layout Plain Layout\n" + 
			"\n" + 
			"\n" + 
			"\\backslash\n" + 
			"setlength{\n" + 
			"\\backslash\n" + 
			"columnsep}{10pt}\n" + 
			"\\end_layout\n" + 
			"\n" + 
			"\\end_inset\n" + 
			"\n" + 
			"\n" + 
			"\\end_layout\n" + 
			"\n";
	private static final String seprule_on = "\\begin_layout Standard\n" + 
			"\\begin_inset ERT\n" + 
			"status open\n" + 
			"\n" + 
			"\\begin_layout Plain Layout\n" + 
			"\n" + 
			"\n" + 
			"\\backslash\n" + 
			"setlength{\n" + 
			"\\backslash\n" + 
			"columnseprule}{0.5pt}\n" + 
			"\\end_layout\n" + 
			"\n" + 
			"\\end_inset\n" + 
			"\n" + 
			"\n" + 
			"\\end_layout\n";
	private static final String seprule_off = "\\begin_layout Standard\n" + 
			"\\begin_inset ERT\n" + 
			"status open\n" + 
			"\n" + 
			"\\begin_layout Plain Layout\n" + 
			"\n" + 
			"\n" + 
			"\\backslash\n" + 
			"setlength{\n" + 
			"\\backslash\n" + 
			"columnseprule}{0pt}\n" + 
			"\\end_layout\n" + 
			"\n" + 
			"\\end_inset\n" + 
			"\n" + 
			"\n" + 
			"\\end_layout\n";
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

		removeEntriesWithMissingPronunciations(entries);
		removeEntriesWithInvalidSyllabary(entries);
		removeEntriesWithBogusDefinitions(entries);
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
		System.out.flush();
		System.err.flush();
		System.out.println("---");
		System.out.println("STATISTICS ON DBENTRIES THAT PASSED INITIAL SCREENING");
		System.out.println("---");
		System.out.println("\tFound "+nf.format(verbs)+" verb entries.");
		System.out.println("\tFound "+nf.format(nouns)+" noun entries.");
		System.out.println("\tFound "+nf.format(advadvs)+" adjectivial entries.");
		System.out.println("\tFound "+nf.format(interjects)+" interjection entries.");
		System.out.println("\tFound "+nf.format(posts)+" post-position entries.");
		System.out.println("\tFound "+nf.format(prons)+" pronoun entries.");
		System.out.println("\tFound "+nf.format(conjs)+" conjunction entries.");
		System.out.println("\tFound "+nf.format(other)+" other entries.");
		System.out.println("---");
		System.out.flush();
		
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
				if (wf.syllabary.contains(",")) {
					String[] wfs =wf.syllabary.split(" *, *");
					for (String s: wfs) {
						WordForm wf2 = new WordForm(wf);
						wf2.syllabary=s;
						wordforms.add(wf2);
					}
				} else {
					wordforms.add(wf);
				}
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
			/*
			 * is this a "splittable" definition with numbers?
			 */
			if (ec.getDefinition().startsWith("1")) {
				String defs[] = ec.getDefinition().split("\\d ?\\.? ?");
				for (String adef: defs) {
					if (StringUtils.isEmpty(adef)) {
						continue;
					}
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					english.add(ec_split);
				}
			} else {
				english.add(ec);
			}
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
		FileUtils.write(file, Chapter_Dictionary + columnsep_large + seprule_on + MULTICOLS_BEGIN + sloppy_begin, "UTF-8", true);
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
			if (entry.examples.size()!=0) {
				FileUtils.write(file,  "\\begin_deeper\n", "UTF-8", true);
				for (ExampleEntry ee: entry.examples) {
					FileUtils.write(file,  ee.getLyxCode(), "UTF-8", true);
				}
				FileUtils.write(file,  "\\end_deeper\n", "UTF-8", true);
			}
			Iterator<CrossReference> icross = entry.crossrefs.iterator();
			if (icross.hasNext()) {
				FileUtils.write(file,  "\\begin_deeper\n", "UTF-8", true);
				StringBuilder sb = new StringBuilder();
				sb.append("\\begin_layout Standard\n");
				sb.append("\\emph on\n");
				sb.append("cf: ");
				sb.append("\\emph default\n");
				
				FileUtils.write(file,sb.toString(), "UTF-8", true);				
				FileUtils.write(file,  icross.next().getLyxCode(true), "UTF-8", true);
				while (icross.hasNext()) {
					FileUtils.write(file,  ", "+icross.next().getLyxCode(true), "UTF-8", true);
				}
				sb.append("\\end_layout\n");
				FileUtils.write(file,  "\\end_deeper\n", "UTF-8", true);
			}
		}
		FileUtils.write(file, sloppy_end+MULTICOLS_END + seprule_off + columnsep_normal, "UTF-8", true);
		
		FileUtils.write(file, Chapter_WordForms + columnsep_large + seprule_on + MULTICOLS_BEGIN+sloppy_begin, "UTF-8", true);
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
		FileUtils.write(file,sloppy_end+ MULTICOLS_END + seprule_off + columnsep_normal, "UTF-8", true);
		
		FileUtils.write(file, Chapter_English + columnsep_large + seprule_on + MULTICOLS_BEGIN+sloppy_begin, "UTF-8", true);
		prevSection="";
		for (EnglishCherokee entry: english) {
			String eng = StringUtils.left(entry.getDefinition(), 1).toUpperCase();
			if (!eng.equals(prevSection)) {
				prevSection=eng;
				FileUtils.write(file, "\\begin_layout Section\n", "UTF-8", true);
				FileUtils.write(file, eng.toUpperCase(), "UTF-8", true);
				FileUtils.write(file, "\\end_layout\n", "UTF-8", true);
			}
			FileUtils.write(file, entry.getLyxCode(true), "UTF-8", true);
		}
		FileUtils.write(file,sloppy_end+ MULTICOLS_END + seprule_off + columnsep_normal, "UTF-8", true);
		
		FileUtils.write(file, end, "UTF-8", true);
		
	}

	private void removeEntriesWithBogusDefinitions(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (entry.definitiond.startsWith("(see")) {
				System.err.println("Bad definition: "+entry.entrya+": "+entry.definitiond);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.definitiond)) {
				System.err.println("Empty definition: "+entry.entrya+": "+entry.syllabaryb);
				ientry.remove();
				continue;
			}
		}		
	}

	private List<LyxEntry> processIntoEntries(List<DbEntry> entries) {
		Map<String, Integer> crossrefs_id=new HashMap<>();
		Map<Integer, String> crossrefs_syll=new HashMap<>();
		List<LyxEntry> definitions=new ArrayList<>();
		Iterator<DbEntry> ientries = entries.iterator();
		while (ientries.hasNext()) {
			DbEntry entry = ientries.next();
			LyxEntry entryFor = LyxEntry.getEntryFor(entry);
			if (entryFor!=null) {
				LyxEntry.fillinExampleSentences(entryFor, entry);				
				definitions.add(entryFor);
				crossrefs_id.put(entry.entrya.toLowerCase(), entryFor.id);
				crossrefs_syll.put(entryFor.id, entryFor.getSyllabary().get(0));
				entryFor.crossrefstxt = entry.crossreferencet;
			}
		}		
		Iterator<LyxEntry> idef = definitions.iterator();
		while (idef.hasNext()) {
			LyxEntry def = idef.next();
			if (StringUtils.isEmpty(def.crossrefstxt)) {
				continue;
			}
			String[] refs = def.crossrefstxt.split(", *");
			for (String ref: refs) {
				String xref=ref.toLowerCase();
				xref=StringUtils.substringBefore(ref, "(");
				xref=StringUtils.strip(xref);
				if (!crossrefs_id.containsKey(xref)) {
					System.err.println("UNABLE TO FIND CROSS-REFERENCE '"+ref+"' for entry "+def.getPronunciations().get(0));
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
			if (!StringUtils.isEmpty(entry.syllabaryb.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("Bad Syllabary: "+entry.entrya+", "+entry.syllabaryb);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.nounadjpluralsyllf.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("Bad Syllabary: "+entry.entrya+", "+entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vfirstpresh.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("Bad Syllabary: "+entry.entrya+", "+entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vsecondimpersylln.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("Bad Syllabary: "+entry.entrya+", "+entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdinfsyllp.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("Bad Syllabary: "+entry.entrya+", "+entry.vthirdinfsyllp);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdpastsyllj.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("Bad Syllabary: "+entry.entrya+", "+entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			if (!StringUtils.isEmpty(entry.vthirdpressylll.replaceAll("[Ꭰ-Ᏼ\\s,\\-]", ""))) {
				System.err.println("Bad Syllabary: "+entry.entrya+", "+entry.vthirdpressylll);
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
				System.err.println("Missing entrya: "+entry.entrya);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.nounadjpluraltone.replace("-", "")) != StringUtils.isEmpty(entry.nounadjpluralsyllf.replace("-", ""))) {
				System.err.println("Missing nounadjpluraltone or nounadjpluralsyllf: "+entry.entrya+", "+entry.nounadjpluraltone+"|"+entry.nounadjpluralsyllf);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vfirstprestone.replace("-", "")) != StringUtils.isEmpty(entry.vfirstpresh.replace("-", ""))) {
				System.err.println("Missing vfirstprestone or vfirstpresh: "+entry.entrya+", "+entry.vfirstprestone+"|"+entry.vfirstpresh);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vsecondimpertone.replace("-", "")) != StringUtils.isEmpty(entry.vsecondimpersylln.replace("-", ""))) {
				System.err.println("Missing vsecondimpertone or vsecondimpersylln: "+entry.entrya+", "+entry.vsecondimpertone+"|"+entry.vsecondimpersylln);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vthirdpasttone.replace("-", "")) != StringUtils.isEmpty(entry.vthirdpastsyllj.replace("-", ""))) {
				System.err.println("Missing vthirdpasttone or vthirdpastsyllj: "+entry.entrya+", "+entry.vthirdpasttone+"|"+entry.vthirdpastsyllj);
				ientry.remove();
				continue;
			}
			if (StringUtils.isEmpty(entry.vthirdprestone.replace("-", "")) != StringUtils.isEmpty(entry.vthirdpressylll.replace("-", ""))) {
				System.err.println("Missing vthirdprestone or vthirdpressylll: "+entry.entrya+", "+entry.vthirdprestone+"|"+entry.vthirdpressylll);
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
				System.err.println("No Syllabary: "+entry.entrya+" = "+entry.definitiond);
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
