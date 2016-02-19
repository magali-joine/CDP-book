package net.cherokeedictionary.shared;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.cherokeelessons.chr.Syllabary;

import net.cherokeedictionary.model.DictionaryEntry;

public class DictionaryEntryValidator extends DictionaryEntry {

	public DictionaryEntryValidator(DictionaryEntry entry) {
		super(entry);
	}

	public DictionaryEntryValidator() {
	}

	private boolean valid = true;
	private Set<String> errors = new LinkedHashSet<>();

	private void clean() {
		valid = true;
		int size_syllabary = 0;
		int size_pronunciation = 0;
		for (int ix = 0; ix < forms.size(); ix++) {
			EntryForm form = forms.get(ix);
			String p = form.pronunciation;// pronunciations.get(ix);
			String s = form.pronunciation;// syllabary.get(ix);
			if (p.replaceAll("[ -]+", "").isEmpty()) {
				p = "";
				form.pronunciation = "";// pronunciations.set(ix, "");
			} else {
				size_pronunciation++;
			}
			if (s.replaceAll("[ -]+", "").isEmpty()) {
				s = "";
				form.syllabary = "";// syllabary.set(ix, "");
			} else {
				size_syllabary++;
			}
		}
	}

	public boolean isValid() {
		if (!valid) {
			return false;
		}
		for (int ix = 0; ix < forms.size(); ix++) {
			EntryForm form = forms.get(ix);
			String s = form.syllabary;
			String p = form.pronunciation.toLowerCase();
			if (s.isEmpty() != p.isEmpty()) {
				if (s.isEmpty() && (p.startsWith("-") || p.endsWith("-"))) {
					continue;
				}
				valid = false;
				if (s.isEmpty()) {
					form.syllabary = "*";
					errors.add("Missing Syllabary Entry");
					continue;
				}
				if (p.isEmpty()) {
					form.pronunciation = "*";
					errors.add("Missing Pronunciation Entry");
					continue;
				}
			}
			if (s.isEmpty()) {
				continue;
			}
			if (!StringUtils.normalizeSpace(s).equals(s)||!StringUtils.strip(s).equals(s)) {
				form.syllabary = "* '"+s.replaceAll("\\s", "\u2423")+"'";
				errors.add("Bad/Extra spaces in Syllabary Entry");
				continue;
			}
			if (!StringUtils.normalizeSpace(p).equals(p)||!StringUtils.strip(p).equals(p)) {
				form.pronunciation = "* '"+p.replace(" ", "\u2423")+"'";
				errors.add("Bad/Extra spaces in Pronunciation Entry");
				continue;
			}
			if (p.matches(".*[¹²³⁴]{1,2}[^AEIOUVẠẸỊỌỤṾaeiouvạẹịọụṿ¹²³⁴]+[¹²³⁴]{1,2}.*")) {
				valid = false;
				form.pronunciation = "* " + p;
				errors.add("Badly Placed Tone Mark/Missing a Vowel");
				continue;
			}
			if (!s.replaceAll("[Ꭰ-Ᏼ ,\\-]+", "").isEmpty()) {
				form.syllabary = "* " + s;
				valid = false;
				errors.add("Bad Syllabary Entry");
				continue;
			}
			if (s.length() > 1 && !p.matches(".*?[¹²³⁴].*?")) {
				System.err.println("\t"+s+" ["+p+"]");
			}
			if (s.startsWith("-") && !p.startsWith("-")){
				valid = false;
				form.pronunciation = "* " + p;
				errors.add("Missing hyphen?");
				continue;
			}
			if (s.endsWith("-") && !p.endsWith("-")){
				valid = false;
				form.pronunciation = "* " + p;
				errors.add("Missing hyphen?");
				continue;
			}
			if (p.startsWith("-") && !s.startsWith("-")){
				valid = false;
				form.syllabary = "* " + s;
				errors.add("Missing hyphen?");
				continue;
			}
			if (p.endsWith("-") && !s.endsWith("-")){
				valid = false;
				form.syllabary = "* " + s;
				errors.add("Missing hyphen?");
				continue;
			}
//			if (!s.startsWith("-") && !s.endsWith("-")){
				if (s.replaceAll("[^Ꭰ-Ᏼ]", "").length() > 1 && !p.matches(".*?[¹²³⁴].*?")) {
					valid = false;
					form.pronunciation = "* " + p;
					errors.add("Invalid Pronunciation Entry");
					continue;
				}
//			}
			if (s.contains(",") || p.contains(",")) {
				int scount = 0;
				int is = -1;
				while ((is = s.indexOf(",", is + 1)) >= 0) {
					scount++;
				}
				;
				int ip = -1;
				int pcount = 0;
				while ((ip = p.indexOf(",", ip + 1)) >= 0) {
					pcount++;
				}
				;
				if (scount != pcount) {
					valid = false;
					errors.add("Commas Mismatch");
					form.syllabary = "* [" + scount + "] " + s;
					form.pronunciation = "* [" + pcount + "] " + p;
					continue;
				}
			}
			if (!p.matches(Syllabary.asLatinMatchPattern(s))) {
				errors.add("Syllabary and Pronunciation Disagree");
				valid = false;
				form.syllabary = "* " + s.replaceAll("\\s", "\u2423");
				form.pronunciation = "* " + p.replaceAll("\\s", "\u2423");
				continue;
			}
		}

		return valid;
	}

	public String simpleFormatted() {
		EntryForm form = forms.get(0);
		StringBuilder sb = new StringBuilder();
		sb.append("id: " + id);
		if (errors.size() > 0) {
			sb.append(" ");
			sb.append(errors);
		}
		sb.append("\n");
		sb.append(form.syllabary);
		sb.append(" [");
		sb.append(form.pronunciation);
		sb.append("] ");
		sb.append("\u201C");
		sb.append(this.definitions.get(0));
		sb.append("\u201D");
		sb.append("\n");
		for (int ix = 1; ix < forms.size(); ix++) {
			EntryForm subform = forms.get(ix);
			String s = subform.syllabary;
			String p = subform.pronunciation;
			if (s.isEmpty() && p.isEmpty()) {
				continue;
			}
			sb.append("    ");
			sb.append(s);
			sb.append(" [");
			sb.append(p);
			sb.append("] ");
			sb.append("\n");
		}
		return sb.toString();
	}

	private void splitCombinations() {
		Iterator<EntryForm> iform = forms.iterator();
		while (iform.hasNext()) {
			EntryForm form = iform.next();
			if (form.syllabary.contains(",") != form.pronunciation.contains(",")) {
				valid = false;
			}
		}
	}

	public void validate() {
		noNullsOnValidatedFields();
		fixPronunciations();
		splitCombinations();
		clean();
	}

	private void fixPronunciations() {
		forms.forEach(f -> f.pronunciation = fixPronunciation(f.pronunciation));
	}

	private static final String[] searchList = { "?", "A.", "E.", "I.", "O.", "U.", "V.", "a.", "e.", "i.", "o.", "u.",
			"v.", "1", "2", "3", "4" };
	private static final String[] replacementList = { "ɂ","Ạ", "̣E", "Ị", "Ọ", "Ụ", "Ṿ", "ạ", "ẹ", "ị", "ọ", "ụ", "ṿ",
			"¹", "²", "³", "⁴" };

	private static String fixPronunciation(String pronounce) {
		pronounce = StringUtils.replaceEach(pronounce, searchList, replacementList);
		return pronounce;
	}

	private void noNullsOnValidatedFields() {
		this.crossreferences.removeIf(cr -> cr == null);
		this.definitions.removeIf(d -> d == null);
		this.examples.removeIf(e -> e == null);
		this.forms.removeIf(f -> f == null);
		this.notes.removeIf(n -> n == null);
		this.crossreferences.forEach(c -> {
			c.crossReference = StringUtils.defaultString(c.crossReference);
		});
		this.forms.forEach(f -> {
			f.formType = f.formType == null ? EntryFormType.Other : f.formType;
			f.latin = f.latin == null ? "" : f.latin;
			f.pronunciation = f.pronunciation == null ? "" : f.pronunciation;
			f.syllabary = f.syllabary == null ? "" : f.syllabary;
		});
		this.examples.forEach(e -> {
			e.english = e.english == null ? "" : e.english;
			e.latin = e.latin == null ? "" : e.latin;
			e.syllabary = e.syllabary == null ? "" : e.syllabary;
		});
		this.notes.forEach(n -> {
			n.note = n.note == null ? "" : n.note;
		});
	}
}
