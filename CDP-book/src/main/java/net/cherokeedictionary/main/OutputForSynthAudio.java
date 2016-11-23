package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import net.cherokeedictionary.dao.DaoCherokeeDictionary;
import net.cherokeedictionary.model.DictionaryEntry;
import net.cherokeedictionary.model.DictionaryEntry.EntryForm;

public class OutputForSynthAudio implements Runnable {
	private final DaoCherokeeDictionary dao = DaoCherokeeDictionary.dao;
	private File dest = new File("output", "synth-audio");
	@Override
	public void run() {
		FileUtils.deleteQuietly(dest);
		dest.mkdirs();
		List<DictionaryEntry> entries = dao.getRecordsForSource("ced");
		
		List<SynthEntry> sEntries = new ArrayList<SynthEntry>();
		for (DictionaryEntry e: entries) {
			for (EntryForm f: e.forms) {
				if (StringUtils.isBlank(f.syllabary)) {
					continue;
				}
				SynthEntry se = new SynthEntry();
				se.definition=e.definitions.get(0);
				se.id=e.id;
				se.pronounce=f.pronunciation;
				se.slength=f.syllabary.length();
				se.stem=f.formType.name();
				se.syllabary=f.syllabary;
				sEntries.add(se);
			}
		}
		Collections.sort(sEntries);
		int counter=1;
		for (int ix=0; ix<sEntries.size(); ix+=11) {
			List<SynthEntry> sublist = sEntries.subList(ix, Math.min(ix+100, sEntries.size()));
			File txtfile = new File(dest, String.format("CED-%03d", counter++));
			try {
				FileUtils.writeLines(txtfile, "UTF-8", sublist);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
	}
	
}

class SynthEntry implements Comparable<SynthEntry>{
	public int id;
	public int slength;
	public String syllabary;
	public String pronounce;
	public String stem;
	public String definition;
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(syllabary);
		sb.append(" ");
		sb.append("[");
		sb.append(pronounce);
		sb.append("] ");
		sb.append("(");
		sb.append(stem);
		sb.append(") ");
		sb.append("\u201c");
		sb.append(definition);
		sb.append("\u201d");
		sb.append(" {");
		sb.append(id);
		sb.append(", ");
		sb.append(slength);
		sb.append("}");
		return sb.toString();
	}
	@Override
	public int compareTo(SynthEntry o) {
		if (o==null) {
			return 1;
		}
		if (slength!=o.slength) {
			return Integer.compare(slength, o.slength);
		}
		if (syllabary==null && syllabary!=o.syllabary) {
			return -1;
		}
		if (o.syllabary==null) {
			return 1;
		}
		if (!syllabary.equals(o.syllabary)){
			return syllabary.compareTo(o.syllabary);
		}
		if (pronounce==null && pronounce!=o.pronounce) {
			return -1;
		}
		if (o.pronounce==null) {
			return 1;
		}
		if (!pronounce.equals(o.pronounce)){
			return pronounce.compareTo(o.pronounce);
		}
		return 0;
	}
}
