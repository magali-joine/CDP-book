package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;

import com.cherokeelessons.chr.Syllabary;

import net.cherokeedictionary.dao.DaoCherokeeDictionary;
import net.cherokeedictionary.model.DictionaryEntry;
import net.cherokeedictionary.model.DictionaryEntry.Crossreference;
import net.cherokeedictionary.model.DictionaryEntry.EntryExample;
import net.cherokeedictionary.model.DictionaryEntry.EntryForm;
import net.cherokeedictionary.model.DictionaryEntry.EntryFormType;
import net.cherokeedictionary.model.DictionaryEntry.Note;
import net.cherokeedictionary.model.LikeSpreadsheetsRecord;
import net.cherokeedictionary.shared.DictionaryEntryValidator;

public class ReportInvalidEntries {

	private final DaoCherokeeDictionary dao = DaoCherokeeDictionary.dao;
	
	public ReportInvalidEntries(String reportfile_html) {
		List<DictionaryEntry> entries = getRecords();
		
		StringBuilder html_head=new StringBuilder();
		
		DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
		sdf.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
		
		html_head.append("<html><head>");
		html_head.append("<meta charset=\"UTF-8\" lang=\"chr\" />");
		html_head.append("<title>Invalid entries for cherokeedictonary.net</title>");
		html_head.append("<style>\n");
		html_head.append("@import url(http://fonts.googleapis.com/earlyaccess/notosanscherokee.css);\n");
		html_head.append("body {font-family: 'Noto Sans Cherokee', sans-serif;}\n");
		html_head.append("</style>\n");
		html_head.append("</head>");
		html_head.append("<body>\n");
		StringBuilder html_body=new StringBuilder();
		
		Iterator<DictionaryEntry> ientry = entries.iterator();
		int invalid=0;
		System.out.println("\tScanning.");
		while (ientry.hasNext()) {
			DictionaryEntryValidator entry = new DictionaryEntryValidator(ientry.next());
			if (entry.definitions.get(0).contains("(see Gram")){
				continue;
			}
			if (entry.definitions.get(0).startsWith("(see ")){
				continue;
			}
			entry.validate();
			if (!entry.isValid()) {
				invalid++;
				html_body.append("<a id='_");
				html_body.append(invalid);
				html_body.append("'></a>");
				html_body.append(String.format("[%04d]\n", invalid));
				html_body.append("<pre>");
				html_body.append(entry.simpleFormatted());
				html_body.append("\n");
				for (EntryForm form: entry.forms) {
					if (form.pronunciation.isEmpty()) {
						continue;
					}
					if (form.syllabary.isEmpty()) {
						continue;
					}
					if (!form.pronunciation.startsWith("*")&&!form.syllabary.startsWith("*")){
						continue;
					}
					if (!form.syllabary.contains("ᎠᎾᏛᏁᎵᏍᎩ")){
						continue;
					}
					//ᎠᎳᏍᏛᏍᎦ
					html_body.append("'");
					html_body.append(form.syllabary);
					html_body.append("'\n");
					html_body.append("'");
					html_body.append(form.pronunciation);
					html_body.append("'\n");
					html_body.append("'");
					html_body.append(Syllabary.asLatinMatchPattern(form.syllabary));
					html_body.append("'\n");
					
				}
				html_body.append("</pre>");
				continue;
			}
		}
		
		html_head.append("<p>=== A total of ");
		html_head.append(invalid);
		html_head.append(" entries with errors were detected.</p>");
		
		html_head.append("<p>Last updated: ");
		html_head.append(sdf.format(new Date()));
		html_head.append("</p>");
		
		System.out.println("\tWriting report to: "+reportfile_html);
		
		html_body.append("<p>");
		html_body.append("<em>Just because an entry fails simple validation does not make it wrong.</em>");
		html_body.append("</p>");
		html_body.append("\n</body></html>\n");
		
		try {
			FileUtils.write(new File(reportfile_html), html_head.toString()+html_body.toString(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<DictionaryEntry> getRecords(){
		List<DictionaryEntry> newRecords = new ArrayList<>();
		List<LikeSpreadsheetsRecord> oldRecords = dao.getLikespreadsheetRecords("CED");
		System.out.println("Loaded "+oldRecords.size()+" records for scanning.");
		for (LikeSpreadsheetsRecord old: oldRecords) {
			DictionaryEntry entry = new DictionaryEntry();
			entry.id=old.id;
			entry.crossreferences.add(new Crossreference(old.crossreferencet));
			entry.definitions.add(old.definitiond);
			entry.examples.add(new EntryExample(old.sentencesyllr, old.sentenceq, old.sentenceenglishs));
			entry.forms.add(new EntryForm(EntryFormType.Verb3rdPrc,old.syllabaryb, old.entrytone, old.entrya));
			entry.forms.add(new EntryForm(EntryFormType.Verb1stPrc, old.vfirstpresh, old.vfirstprestone, old.vfirstpresg));
			entry.forms.add(new EntryForm(EntryFormType.Verb3rdPast,old.vthirdpastsyllj, old.vthirdpasttone, old.vthirdpasti));
			entry.forms.add(new EntryForm(EntryFormType.Verb3rdHab,old.vthirdpressylll, old.vthirdprestone, old.vthirdpresk));
			entry.forms.add(new EntryForm(EntryFormType.Verb2ndImp,old.vsecondimpersylln, old.vsecondimpertone, old.vsecondimperm));
			entry.forms.add(new EntryForm(EntryFormType.Verb3rdInf,old.vthirdinfsyllp, old.vthirdinftone, old.vthirdinfo));
			entry.forms.add(new EntryForm(EntryFormType.Other,old.nounadjpluralsyllf, old.nounadjpluraltone, old.nounadjplurale));
			entry.notes.add(new Note(old.notes));
			entry.pos=old.partofspeechc;
			entry.source=old.source;
			newRecords.add(entry);
		}
		Collections.sort(newRecords);
		System.out.println("\tReformatted.");
		return newRecords;
	}

}
