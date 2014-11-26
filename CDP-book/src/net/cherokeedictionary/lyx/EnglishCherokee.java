package net.cherokeedictionary.lyx;

import net.cherokeedictionary.main.App;

import org.apache.commons.lang3.StringUtils;

public class EnglishCherokee implements Comparable<EnglishCherokee>{
	private String english;
	public EnglishCherokee() {
	}
	public EnglishCherokee(EnglishCherokee ec) {
		this.english=ec.english;
		this.pronounce=ec.pronounce;
		this.syllabary=ec.syllabary;
		this.toLabel=ec.toLabel;
	}
	public void setEnglish(String english) {
		this.english = english;
	}
	public String getDefinition() {
		String eng=english;
		if (eng.endsWith(".")) {
			eng=StringUtils.left(eng, eng.length()-1);
		}
		eng = transform(eng);		
		if (eng.startsWith("(")) {
			String sub = StringUtils.substringBetween(eng, "(", ")");
			eng = StringUtils.substringAfter(eng, "("+sub+")");
			eng += " "+"("+sub+")";
		}
		eng=transform(eng);
		if (eng.endsWith(",")) {
			eng=StringUtils.left(eng, eng.length()-1);
		}		
		return eng;
	}
	public String syllabary;
	public String pronounce;
	public int toLabel;

	public String getLyxCode(boolean bold) {
		StringBuilder sb = new StringBuilder();
		String eng = StringUtils.strip(getDefinition().replace("\\n", " "));		
		if (bold) {
			sb.append("\\begin_layout Standard\n");
			sb.append("\\series bold\n");
			sb.append(eng);
			sb.append("\n");
			sb.append("\\series default\n");
		} else {
			sb.append("\\begin_layout Standard\n");
			sb.append(eng);
		}		
		
		sb.append(": ");
		sb.append(syllabary);			
//		sb.append(" [");
//		sb.append(pronounce);
//		sb.append("]");
		sb.append(" (pg ");
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

	private String transform(String eng) {		
		eng = StringUtils.strip(eng);
		String lc = eng.toLowerCase();
		if (lc.startsWith("n.")) {
			eng = StringUtils.substring(eng, 2);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v. t.")) {
			eng = StringUtils.substring(eng, 5);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v.t.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v. i.")) {
			eng = StringUtils.substring(eng, 5);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("v.i.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("adv.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.startsWith("adj.")) {
			eng = StringUtils.substring(eng, 4);
			eng = StringUtils.strip(eng);
			lc = eng.toLowerCase();
		}
		if (lc.contains(".") && lc.indexOf(".")<4 && !lc.startsWith("1")) {
			App.err("WARNING: BAD DEFINITION! => "+eng);
		}
		if (lc.startsWith("becoming ")) {
			eng = StringUtils.substring(eng, 9)+" (becoming)";
			lc = eng.toLowerCase();
		}
		chopper: {
			if (lc.startsWith("they're ")) {
				eng = StringUtils.substring(eng, 8)+" (they are)";
				break chopper;
			}
			if (lc.startsWith("they are ")) {
				eng = StringUtils.substring(eng, 9)+" (they are)";
				break chopper;
			}
			if (lc.startsWith("at the ")) {
				eng = StringUtils.substring(eng, 7)+" (at the)";
				break chopper;
			}
			if (lc.startsWith("at a ")) {
				eng = StringUtils.substring(eng, 5)+" (at a)";
				break chopper;
			}
			if (lc.startsWith("at ")) {
				eng = StringUtils.substring(eng, 3)+" (at)";
				break chopper;
			}
			if (lc.startsWith("in the ")) {
				eng = StringUtils.substring(eng, 7)+" (in the)";
				break chopper;
			}
			if (lc.startsWith("in a ")) {
				eng = StringUtils.substring(eng, 5)+" (in a)";
				break chopper;
			}
			if (lc.startsWith("in ")) {
				eng = StringUtils.substring(eng, 3)+" (in)";
				break chopper;
			}
			if (lc.startsWith("on the ")) {
				eng = StringUtils.substring(eng, 7)+" (on the)";
				break chopper;
			}
			if (lc.startsWith("on a ")) {
				eng = StringUtils.substring(eng, 5)+" (on a)";
				break chopper;
			}
			if (lc.startsWith("on ")) {
				eng = StringUtils.substring(eng, 3)+" (on)";
				break chopper;
			}
			if (lc.startsWith("she's ")) {
				eng = StringUtils.substring(eng, 6);
				break chopper;
			}
			if (lc.startsWith("he/it is ")) {
				eng = StringUtils.substring(eng, 9);
				break chopper;
			}
			if (lc.startsWith("he, it's ")) {
				eng = StringUtils.substring(eng, 9);
				break chopper;
			}
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
			if (lc.startsWith("an ")) {
				eng = StringUtils.substring(eng, 3);
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
			if (lc.startsWith("his, her")) {
				if (eng.length()<9) {
					break chopper;
				}
				eng = StringUtils.substring(eng, 8)+" (his/her)";
			}
			if (lc.startsWith("its ")) {
				eng = StringUtils.substring(eng, 4)+" (its)";
				break chopper;
			}
			if (lc.startsWith("his ")) {
				eng = StringUtils.substring(eng, 4)+" (his)";
				break chopper;
			}
			if (lc.startsWith("her ")) {
				eng = StringUtils.substring(eng, 4)+" (her)";
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