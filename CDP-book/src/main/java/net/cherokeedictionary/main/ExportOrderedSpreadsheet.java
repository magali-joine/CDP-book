package net.cherokeedictionary.main;

import java.util.Iterator;
import java.util.List;

import net.cherokeedictionary.db.DaoDictionary;
import net.cherokeedictionary.model.SimpleDictionaryEntry;

public class ExportOrderedSpreadsheet {

	private final DaoDictionary dao = DaoDictionary.dao;
	
	public ExportOrderedSpreadsheet(String orderedoutfile) {
		List<SimpleDictionaryEntry> entries = dao.getSimpleEntries();
		Iterator<SimpleDictionaryEntry> ientry = entries.iterator();
		int invalid=0;
		while (ientry.hasNext()) {
			SimpleDictionaryEntry entry = ientry.next();
			entry.validate();
			if (!entry.isValid()) {
				invalid++;
				System.err.println(entry.simpleFormatted());
				System.err.println();
				ientry.remove();
				continue;
			}
		}
		if (invalid>0) {
			System.err.println("=== A total of "+invalid+" entries with errors were detected.");
		}
	}

}
