package net.cherokeedictionary.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class SimpleDictionaryEntry {
	public int id = 0;
	public int size = 0;
	public List<String> pronunciations = new ArrayList<>();
	public List<String> syllabary = new ArrayList<>();
	public String definition = "";
	private boolean valid = true;
	public EntryType type;
	public List<String> errors = new ArrayList<>();

	private void clean() {
		valid = true;
		for (int ix=0; ix<pronunciations.size(); ix++) {
			String p = pronunciations.get(ix);
			String s = syllabary.get(ix);
			if (p.replaceAll("[ -]+", "").isEmpty()) {
				p="";
				pronunciations.set(ix, "");
			}
			if (s.replaceAll("[ -]+", "").isEmpty()){
				s="";
				syllabary.set(ix, "");
			}
		}
		if (syllabary.size() == pronunciations.size()) {
			size = syllabary.size();
		}
	}

	public boolean isValid() {
		if (!valid) {
			return false;
		}
		for (int ix = 0; ix < size; ix++) {
			String s = syllabary.get(ix);
			String p = pronunciations.get(ix);
			if (s.isEmpty() != p.isEmpty()) {
				valid=false;
				if (s.isEmpty()) {
					syllabary.set(ix, "***");
					errors.add("Missing Syllabary Entry");
				}
				if (p.isEmpty()) {
					pronunciations.set(ix, "***");
					errors.add("Missing Pronunciation Entry");
				}
			}
			if (s.isEmpty()) {
				continue;
			}
			if (!s.replaceAll("[Ꭰ-Ᏼ ,]+", "").isEmpty()) {
				syllabary.set(ix, "*** "+s);
				valid=false;
			}
			if (s.length()>1 && !p.matches(".*?[¹²³⁴].*?")) {
				valid=false;
				pronunciations.set(ix, "*** "+p);
				errors.add("Invalid Pronunciation Entry");
			}
		}
		return valid;
	}

	public String simpleFormatted() {
		StringBuilder sb = new StringBuilder();
		sb.append("id: " + id);
		if (errors.size()>0) {
			sb.append(" ");
			sb.append(errors);
		}
		sb.append("\n");
		sb.append(syllabary.get(0));
		sb.append(" [");
		sb.append(pronunciations.get(0));
		sb.append("] ");
		sb.append("\u201C");
		sb.append(definition);
		sb.append("\u201D");
		sb.append("\n");
		for (int ix = 1; ix < size; ix++) {
			String s = syllabary.get(ix);
			String p = pronunciations.get(ix);
			if (s.isEmpty() && p.isEmpty()) {
				continue;
			}
			sb.append("___ ");
			sb.append(s);
			sb.append(" [");
			sb.append(p);
			sb.append("] ");
			sb.append("\n");
		}
		return sb.toString();
	}

	private void splitCombinations() {
		ListIterator<String> ls = syllabary.listIterator();
		ListIterator<String> lp = pronunciations.listIterator();
		while (ls.hasNext()) {
			String s = ls.next();
			String p = lp.next();
			if (s.contains(",") != p.contains(",")) {
				valid = false;
			}
		}
	}

	public void validate() {
		splitCombinations();
		clean();
	}

}
