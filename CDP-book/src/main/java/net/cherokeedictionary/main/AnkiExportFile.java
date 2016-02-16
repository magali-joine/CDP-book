package net.cherokeedictionary.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import net.cherokeedictionary.dao.DaoCherokeeDictionary;
import net.cherokeedictionary.lyx.EnglishCherokee;
import net.cherokeedictionary.lyx.LyxEntry;
import net.cherokeedictionary.lyx.LyxExportFile;
import net.cherokeedictionary.lyx.VerbEntry;
import net.cherokeedictionary.model.LikeSpreadsheetsRecord;

public class AnkiExportFile extends Thread {

	private static final String[] CSVHDR = {"CHEROKEE", "ENGLISH", "NOTES"};
	private final String ankifile;

	private static final DaoCherokeeDictionary dao = DaoCherokeeDictionary.dao;
	public AnkiExportFile(String ankifile) {
		this.ankifile=ankifile;
		System.out.println("\tAnkiExportFile");
		List<LikeSpreadsheetsRecord> entries = dao.getLikespreadsheetRecords("ced");
		DaoCherokeeDictionary.Util.removeUnwantedEntries(entries);
		DaoCherokeeDictionary.Util.removeEntriesWithMissingPronunciations(entries);
		DaoCherokeeDictionary.Util.removeEntriesWithInvalidSyllabary(entries);
		DaoCherokeeDictionary.Util.removeEntriesWithBogusDefinitions(entries);
		
		List<LyxEntry> lyxentries = LyxExportFile.processIntoEntries(entries);
		List<AnkiEntry> aklist = new ArrayList<>();
		for (LyxEntry entry: lyxentries) {
			String english = EnglishCherokee.getDefinition(entry.definition);
			if (entry instanceof VerbEntry) {
				List<String> s = entry.getSyllabary();
				List<String> p = entry.getPronunciations();
				String pres_3rd = s.get(0)+"<br/>"+p.get(0); 
				
				
			}
//			aklist.add(ak);
		}		
		Collections.sort(aklist);
		
		try {
			File file = new File(ankifile);
			FileUtils.touch(file);
			Appendable out;
			out = new FileWriter(file);
			CSVPrinter printer = CSVFormat.DEFAULT.withHeader(CSVHDR).print(out);
			Iterator<AnkiEntry> irec = aklist.iterator();
			while (irec.hasNext()) {
				printer.printRecord((Object[])irec.next().values());
			}
			printer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
