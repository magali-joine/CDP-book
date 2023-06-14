package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.cherokeelessons.log.Log;

import net.cherokeedictionary.model.DictionaryEntry;
import net.cherokeedictionary.model.DictionaryEntry.EntryForm;

public class OutputForSynthAudio implements Runnable {
	private final DaoCherokeeDictionary dao = DaoCherokeeDictionary.dao;
	private File dest = new File("output", "synth-audio");
	private final Logger log = Log.getLogger(this);
	@Override
	public void run() {
		log.info("start");
		FileUtils.deleteQuietly(dest);
		dest.mkdirs();
		List<DictionaryEntry> entries = dao.getRecordsForSource("ced");
		
		List<SynthEntry> sEntries = new ArrayList<SynthEntry>();
		for (DictionaryEntry e : entries) {
			List<EntryForm> tmp = new ArrayList<>();
			//look for pronounce entries to split up
			for (EntryForm f : e.forms) {
				if (StringUtils.isBlank(f.syllabary)) {
					continue;
				}
				if (StringUtils.isBlank(f.pronunciation)) {
					continue;
				}
				if (!f.syllabary.contains(",")&&!f.pronunciation.contains(",")){
					tmp.add(f);
					continue;
				}
				String[] p=f.pronunciation.split(",\\s*");
				String[] s=f.syllabary.split(",\\s*");
				if (p.length!=s.length) {
					String[] s2=new String[p.length];
					for (int is=0; is<p.length; is++) {
						if (is<s.length) {
							s2[is]=s[is];
							continue;
						}
						s2[is]=s[0];
					}
					s=s2;
					System.err.println("Splits don't match! "+f.pronunciation+"|"+f.syllabary);
				}
				for (int i=0; i<p.length; i++) {
					EntryForm s1 = new EntryForm();
					s1.formType=f.formType;
					s1.latin=f.latin;
					s1.pronunciation=p[i];
					s1.syllabary=s[i];
					tmp.add(s1);
				}
			}
			for (EntryForm f : tmp) {
				if (f.syllabary.startsWith("-") || f.syllabary.endsWith("-")) {
					continue;
				}
				if (f.pronunciation.startsWith("-") || f.pronunciation.endsWith("-")) {
					continue;
				}
				SynthEntry se = new SynthEntry();
				se.definition="";
				for (String d: e.definitions) {
					if (se.definition.length()!=0) {
						se.definition+=", ";
					}
					se.definition += d.trim();
				}
				se.id = e.id;
				se.pronounce = Util.fixToneCadenceMarks(f.pronunciation).toLowerCase().trim();
				se.slength = f.syllabary.replaceAll("[^Ꭰ-Ᏼ]", "").length();
				se.stem = f.formType.name();
				se.syllabary = f.syllabary.trim();
				sEntries.add(se);
			}
		}
		
		/*
		 * merge sound alikes together
		 */
		Map<String, SynthEntry> byPronounce = new HashMap<>();
		Iterator<SynthEntry> ise = sEntries.iterator();
		while (ise.hasNext()) {
			SynthEntry next=ise.next();
			if (byPronounce.containsKey(next.pronounce)){
				byPronounce.get(next.pronounce).definition+=", "+next.definition;
				ise.remove();
				continue;
			}
			byPronounce.put(next.pronounce, next);
		}
		
		Collections.sort(sEntries);
		
		int partSize = 16;
		int counter = 1;
		for (int ix = 0; ix < sEntries.size(); ix += partSize) {
			List<SynthEntry> sublist = sEntries.subList(ix, Math.min(ix + partSize, sEntries.size()));
			File txtfile = new File(dest, String.format("CED-%03d.txt", counter++));
			try {
				FileUtils.writeLines(txtfile, "UTF-8", sublist);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
		log.info("done");
	}

}

class Util {
	private static final String[] searchList = { "?", "A.", "E.", "I.", "O.", "U.", "V.", "a.", "e.", "i.", "o.", "u.",
			"v.", "1", "2", "3", "4" };
	private static final String[] replacementList = { "ɂ", "̣A", "̣E", "Ị", "Ọ", "Ụ", "Ṿ", "ạ", "ẹ", "ị", "ọ", "ụ", "ṿ",
			"¹", "²", "³", "⁴" };

	public static String fixToneCadenceMarks(String pronounce) {
		return StringUtils.replaceEach(pronounce, searchList, replacementList);
	}
}

class SynthEntry implements Comparable<SynthEntry> {
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
		if (o == null) {
			return 1;
		}
		if (slength != o.slength) {
			return Integer.compare(slength, o.slength);
		}
		if (syllabary == null && syllabary != o.syllabary) {
			return -1;
		}
		if (o.syllabary == null) {
			return 1;
		}
		if (!syllabary.equals(o.syllabary)) {
			if (syllabary.length()>1 && o.syllabary.length()>1){
				String s1=syllabary.substring(1);
				String s2=o.syllabary.substring(1);
				if (!s1.equals(s2)){
					return s1.compareTo(s2);
				}
			}
			return syllabary.compareTo(o.syllabary);
		}
		if (pronounce == null && pronounce != o.pronounce) {
			return -1;
		}
		if (o.pronounce == null) {
			return 1;
		}
		if (!pronounce.equals(o.pronounce)) {
			return pronounce.compareTo(o.pronounce);
		}
		return 0;
	}
}
