package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WordForm implements Comparable<WordForm> {
	public String being_looked_up;
	public List<Reference> references = new ArrayList<Reference>();

	public WordForm() {
	}

	public WordForm(WordForm wf) {
		being_looked_up = wf.being_looked_up;
		references = wf.references;
		references.addAll(wf.references);
	}

	@Override
	public int compareTo(WordForm arg0) {
		int cmp = being_looked_up.compareTo(arg0.being_looked_up);
		if (cmp != 0) {
			return cmp;
		}
		if (Arrays.equals(references.toArray(), arg0.references.toArray())) {
			return 0;
		}
		return references.toString().compareTo(arg0.references.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WordForm)) {
			return false;
		}
		return compareTo((WordForm) obj) == 0;
	}

	public String getLyxCode() {
		boolean briefmode = false;
		if (references.size() == 1) {
			briefmode = references.get(0).syllabary.equals(being_looked_up);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin_layout Standard\n");
		sb.append("\\series bold\n");
		sb.append(LyxEntry.hyphenateSyllabary(being_looked_up));
		sb.append("\n");
		sb.append("\\series default\n");
		if (!briefmode) {
			sb.append(": ");
		}
		for (int ix = 0; ix < references.size(); ix++) {
			if (ix > 0) {
				sb.append(", ");
			}
			String ref = LyxEntry.hyphenateSyllabary(references.get(ix).syllabary);
			int id = references.get(ix).toLabel;
			if (!briefmode) {
				sb.append(ref);
			}
			sb.append(" (pg ".replace(" ",
					"\n\\begin_inset space ~\n\\end_inset\n"));
			sb.append("\\begin_inset CommandInset ref\n"
					+ "LatexCommand pageref\n" + "reference \"");
			sb.append("" + id);
			sb.append("\"\n" + "\\end_inset\n");
			sb.append(")\n");
		}
		sb.append("\\end_layout\n\n");
		return sb.toString();
	}

	public static void dedupeBySyllabary(List<Reference> references2) {
		Set<String> already = new HashSet<>();
		Iterator<Reference> iref = references2.iterator();
		while (iref.hasNext()) {
			String syllabary = iref.next().syllabary;
			if (already.contains(syllabary)) {
				iref.remove();
				continue;
			}
			already.add(syllabary);
		}

	}
}