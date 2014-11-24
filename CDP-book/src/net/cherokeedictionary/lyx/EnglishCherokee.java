package net.cherokeedictionary.lyx;

import org.apache.commons.lang3.StringUtils;

public class EnglishCherokee implements Comparable<EnglishCherokee>{
	private String english;
	public String getEnglish() {
		return english;
	}
	public void setEnglish(String english) {
		this.english = english;
	}
	public String getDefinition() {
		return fixupDefinition(english);
	}
	public String syllabary;
	public String pronounce;
	public int toLabel;

	public String getLyxCode() {
		StringBuilder sb = new StringBuilder();
		String eng = StringUtils.strip(english.replace("\\n", " "));		
		eng = fixupDefinition(eng);		
		sb.append("\\begin_layout Description\n");
		sb.append(eng.replace(" ", "\n\\begin_inset space ~\n\\end_inset\n"));
		
		sb.append(": ");
		sb.append(syllabary);			
		sb.append(" [");
		sb.append(pronounce);
		sb.append("]");
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

	private String fixupDefinition(String eng) {
		eng = chopFront(eng);		
		if (eng.startsWith("(")) {
			String tmp = eng;
			String sub = StringUtils.substringBetween(eng, "(", ")");
			eng = StringUtils.substringAfter(eng, "("+sub+")");
			eng += " "+"("+sub+")";
			tmp += " -> "+eng;
			System.err.println(tmp);
		}
		return chopFront(eng);
	}

	public String chopFront(String eng) {
		eng = StringUtils.strip(eng);
		chopper: {
			String lc = eng.toLowerCase();
			if (lc.startsWith("is ")) {
				eng = StringUtils.substring(eng, 3);
				break chopper;
			}
			if (lc.startsWith("the ")) {
				eng = StringUtils.substring(eng, 4);
				break chopper;
			}
			if (lc.startsWith("a ")) {
				eng = StringUtils.substring(eng, 2);
				break chopper;
			}
			if (lc.startsWith("he's ")) {
				eng = StringUtils.substring(eng, 5);
				break chopper;
			}
			if (lc.startsWith("he is ")) {
				eng = StringUtils.substring(eng, 6);
				break chopper;
			}
			if (lc.startsWith("it's ")) {
				eng = StringUtils.substring(eng, 5);
				break chopper;
			}
			if (lc.startsWith("it is ")) {
				eng = StringUtils.substring(eng, 6);
				break chopper;
			}
			if (lc.startsWith("his ")) {
				eng = StringUtils.substring(eng, 4);
				break chopper;
			}
			if (lc.startsWith("her ")) {
				eng = StringUtils.substring(eng, 4);
				break chopper;
			}
			if (lc.startsWith("he ")) {
				eng = StringUtils.substring(eng, 3);
				break chopper;
			}
			if (lc.startsWith("she ")) {
				eng = StringUtils.substring(eng, 4);
				break chopper;
			}
			if (lc.startsWith("it ")) {
				eng = StringUtils.substring(eng, 3);
				break chopper;
			}
		}
		return eng;
	}

	@Override
	public int compareTo(EnglishCherokee arg0) {
		String e1 = getDefinition();
		String e2 = arg0.getDefinition();
		int cmp = e1.compareToIgnoreCase(e2); 
		if (cmp!=0) {
			return cmp;
		}
		cmp = e1.compareTo(e2); 
		if (cmp!=0) {
			return cmp;
		}
		cmp = syllabary.compareTo(arg0.syllabary);
		if (cmp!=0) {
			return cmp;
		}
		cmp = pronounce.compareTo(arg0.pronounce);
		if (cmp!=0) {
			return cmp;
		}
		return toLabel-arg0.toLabel;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EnglishCherokee)) {
			return false;
		}
		return compareTo((EnglishCherokee)obj)==0;
	}
}