package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;

import net.cherokeedictionary.model.DictionaryEntry.EntryExample;
import net.cherokeedictionary.model.LikeSpreadsheetsRecord;
import net.cherokeedictionary.shared.DictionaryEntryValidator;
import net.cherokeedictionary.util.DaoUtils;

public class AutoRepairExamples {
	private final DaoCherokeeDictionary dao = DaoCherokeeDictionary.dao;

	public AutoRepairExamples(String reportfile_html) {
		
		StringBuilder html_head=new StringBuilder();
		html_head.append("<html><head>");
		html_head.append("<meta charset=\"UTF-8\" lang=\"chr\" />");
		html_head.append("<title>Autorepaired entries for cherokeedictonary.net</title>");
		html_head.append("<style>\n");
		html_head.append("@import url(http://fonts.googleapis.com/earlyaccess/notosanscherokee.css);\n");
		html_head.append("body {font-family: 'Noto Sans Cherokee', sans-serif;}\n");
		html_head.append("</style>\n");
		html_head.append("</head>");
		html_head.append("<body>\n");
		StringBuilder html_body=new StringBuilder();
		
		System.out.println("Fetching CED records.");
		List<LikeSpreadsheetsRecord> oldRecords = dao.getLikespreadsheetRecords("CED");
		System.out.println("CED record count: "+oldRecords.size());
		List<LikeSpreadsheetsRecord> forUpdate = new ArrayList<>();
		oldRecords.forEach(old->{
			if (autoCorrectRecord(old)){
				forUpdate.add(old);
			}
		});
		System.out.println("===EXAMPLES FOR AUTO REPAIR: "+forUpdate.size());
		for (LikeSpreadsheetsRecord rec: forUpdate) {
			System.out.println("\t"+rec.id+") "+rec.syllabaryb);
			System.out.println("\t\t"+rec.sentencesyllr);
			System.out.println("\t\t"+rec.sentenceq);
			System.out.println("\t\t"+rec.sentencetranslit);
			System.out.println("\t\t"+rec.sentenceenglishs);
			
			html_body.append("<a id='_");
			html_body.append(rec.id);
			html_body.append("'></a>");
			html_body.append(String.format("[%04d]\n", rec.id));
			html_body.append("<pre>");
			
			html_body.append(rec.syllabaryb);
			html_body.append("\n");
			html_body.append("\t"+rec.sentencesyllr+"\n");
			html_body.append("\t"+rec.sentenceq+"\n");
			html_body.append("\t"+rec.sentenceenglishs+"\n");
			html_body.append("\t"+rec.sentencetranslit+"\n");
			html_body.append("\n");
			
			html_body.append("</pre>");
		}
		html_body.append("\n</body></html>\n");
		
		if (forUpdate.size()!=0) {
			dao.backupLikeSpreadsheets();
		}
		
		int[] counts=dao.updateLikespreadsheetSentences(forUpdate);
		int sum = Arrays.stream(counts).sum(); 
//		int sum=0;
		html_head.append("<p>=== A total of ");
		html_head.append(sum +" out of "+forUpdate.size());
		html_head.append(" entries with errors were corrected.</p>");
		
		DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
		sdf.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
		
		html_head.append("<p>Last updated: ");
		html_head.append(sdf.format(new Date()));
		html_head.append("</p>");
		
		
		try {
			System.out.println("\tWriting report to: "+reportfile_html);
			FileUtils.write(new File(reportfile_html), html_head.toString()+html_body.toString(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean autoCorrectRecord(LikeSpreadsheetsRecord record) {
		EntryExample old_example = new EntryExample(record.sentencesyllr, record.sentenceq, record.sentenceenglishs);
		EntryExample new_example = new EntryExample();
		boolean changed = false;
		new_example.english = DictionaryEntryValidator.repairUnderlinesAndClean(old_example.english);
		new_example.latin = DictionaryEntryValidator.repairUnderlinesAndClean(old_example.latin);
		new_example.syllabary = DictionaryEntryValidator.repairUnderlinesAndClean(old_example.syllabary);
		changed |= !new_example.english.equals(old_example.english);
		changed |= !new_example.latin.equals(old_example.latin);
		changed |= !new_example.syllabary.equals(old_example.syllabary);
		if (changed) {
			record.sentencesyllr=new_example.syllabary;
			record.sentenceq=new_example.latin;
			record.sentenceenglishs=new_example.english;
			record.sentencetranslit=DaoUtils.syllabaryTranslit(new_example.syllabary.replaceAll("[^Ꭰ-Ᏼ\\s]", ""));
		}
		return changed;
	}
}
