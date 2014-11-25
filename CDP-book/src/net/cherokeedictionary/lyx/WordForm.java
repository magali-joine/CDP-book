package net.cherokeedictionary.lyx;


public class WordForm implements Comparable<WordForm> {
	public String syllabary;
	public String references;
	public int toLabel;
	public WordForm() {
	}
	public WordForm(WordForm wf) {
		syllabary=wf.syllabary;
		references=wf.references;
		toLabel=wf.toLabel;
	}
	@Override
	public int compareTo(WordForm arg0) {
		int cmp = syllabary.compareTo(arg0.syllabary); 
		if (cmp!=0) {
			return cmp;
		}
		cmp = references.compareTo(arg0.references);
		if (cmp!=0) {
			return cmp;
		}
		return toLabel-arg0.toLabel;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WordForm)) {
			return false;
		}
		return compareTo((WordForm)obj)==0;
	}
	
	public String getLyxCode() {
		boolean brief=syllabary.equals(references);
		StringBuilder sb = new StringBuilder();
		sb.append("\\begin_layout Standard\n");
		sb.append("\\series bold\n");
//		sb.append(syllabary.replace(" ", "\n\\begin_inset space ~\n\\end_inset\n"));
		sb.append(syllabary);
		sb.append("\n");
		sb.append("\\series default\n");
		if (!brief) {
			sb.append(": ");
			sb.append(references);			
		}
		sb.append(" (page ");
		sb.append("\\begin_inset CommandInset ref\n" + 
				"LatexCommand pageref\n" + 
				"reference \"");
		sb.append(""+toLabel);
		sb.append("\"\n" + 
				"\\end_inset\n"); 
		sb.append(")\n");
		sb.append("\\end_layout\n\n");		
		return sb.toString();
	}
}