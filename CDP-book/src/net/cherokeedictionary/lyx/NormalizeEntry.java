package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.cherokeedictionary.main.App;

public class NormalizeEntry {
	public String pres3;
	public String pres1;
	public String past;
	public String habit;
	public String imp;
	public String inf;
	public List<String> getEntries(){
		List<String> list = new ArrayList<>();
		list.add(pres3);
		list.add(pres1);
		list.add(past);
		list.add(habit);
		list.add(imp);
		list.add(inf);
		return list;
	}
	public static void removeᏫprefix(NormalizeEntry normalizeEntry) {
		if (normalizeEntry.imp.startsWith("Ꮻ") && normalizeEntry.pres3.startsWith("Ꮻ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꭿ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮻ") && normalizeEntry.pres3.startsWith("Ꮽ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮻ") && normalizeEntry.pres3.startsWith("Ꮹ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꭿ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("ᏫᏕ") && normalizeEntry.pres3.startsWith("Ꮣ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("ᏫᏨ") && normalizeEntry.pres3.startsWith("Ꭴ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}				
		if (normalizeEntry.pres3.startsWith("Ꮹ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮹ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.habit);
		}
		if (normalizeEntry.pres3.startsWith("Ꮽ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮽ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.habit);
		}
		if (normalizeEntry.pres3.startsWith("Ꮻ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮻ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.habit);
		}
		if (normalizeEntry.pres1.startsWith("Ꮹ")){
			normalizeEntry.pres1=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.pres1);
		}
		if (normalizeEntry.pres1.startsWith("Ꮻ")){
			normalizeEntry.pres1=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres1);
		}
		if (normalizeEntry.past.startsWith("Ꮽ")){
			normalizeEntry.past=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.past);
		}
		if (normalizeEntry.inf.startsWith("Ꮽ")){
			normalizeEntry.inf=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.inf);
		}
		warnIfStartsWithAnyRange("Ꮻ", "Ꮾ", normalizeEntry);
	}
	
	public static void removeᏂprefix(NormalizeEntry normalizeEntry) {
		//they and I
		if (normalizeEntry.pres1.startsWith("ᏃᏥ")){
			normalizeEntry.pres1="Ꭳ"+LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres1);
		}
		//you all
		if (normalizeEntry.imp.startsWith("ᏂᏥ")){
			normalizeEntry.imp="Ꭲ"+LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		
		if (normalizeEntry.imp.startsWith("Ꮒ") && normalizeEntry.pres3.startsWith("Ꮒ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꭿ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮒ") && normalizeEntry.pres3.startsWith("Ꮔ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮒ") && normalizeEntry.pres3.startsWith("Ꮎ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꭿ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("ᏂᏗ") && normalizeEntry.pres3.startsWith("Ꮣ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("ᏂᏨ") && normalizeEntry.pres3.startsWith("Ꭴ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}				
		if (normalizeEntry.pres3.startsWith("Ꮎ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮎ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.habit);
		}
		if (normalizeEntry.pres3.startsWith("Ꮔ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮔ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.habit);
		}
		if (normalizeEntry.pres3.startsWith("Ꮒ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮒ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.habit);
		}
		if (normalizeEntry.pres1.startsWith("Ꮎ")){
			normalizeEntry.pres1=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.pres1);
		}
		if (normalizeEntry.pres1.startsWith("Ꮒ")){
			normalizeEntry.pres1=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres1);
		}
		if (normalizeEntry.past.startsWith("Ꮔ")){
			normalizeEntry.past=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.past);
		}
		if (normalizeEntry.inf.startsWith("Ꮔ")){
			normalizeEntry.inf=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.inf);
		}
		warnIfStartsWithAnyRange("Ꮒ", "Ꮕ", normalizeEntry);
	}
	
	private static void warnIfStartsWithAnyRange(String start,
			String end, NormalizeEntry normalizeEntry) {
		String regex="^["+Pattern.quote(start)+"-"+Pattern.quote(end)+"].*";
		Pattern pattern = Pattern.compile(regex);
		for (String element: normalizeEntry.getEntries()) {
			if (pattern.matcher(element).matches()){
				App.err("Need to add rule for: "+normalizeEntry.getEntries().toString());
			}
		}
	}
	public static void removeᏕprefix(NormalizeEntry normalizeEntry) {
		
		//you all
		if (normalizeEntry.imp.startsWith("ᏗᏥ")){
			normalizeEntry.imp="Ꭲ"+LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		//they and I
		if (normalizeEntry.pres1.startsWith("ᏙᏥ")){
			normalizeEntry.pres1="Ꭳ"+LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres1);
		}
		
		if (normalizeEntry.imp.startsWith("Ꮦ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꭾ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮩ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꮀ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮪ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꮁ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮫ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꮂ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮨ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꭿ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮤ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.newPrefix("Ꭽ", normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮧ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		if (normalizeEntry.imp.startsWith("Ꮥ")){
			normalizeEntry.imp=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.imp);
		}
		if (normalizeEntry.pres3.startsWith("Ꮣ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮣ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.habit);
		}
		if (normalizeEntry.pres3.startsWith("Ꮪ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮪ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.habit);
		}
		if (normalizeEntry.pres3.startsWith("Ꮥ")){
			normalizeEntry.pres3=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres3);
		}
		if (normalizeEntry.habit.startsWith("Ꮥ")){
			normalizeEntry.habit=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.habit);
		}
		if (normalizeEntry.pres1.startsWith("Ꮣ")){
			normalizeEntry.pres1=LyxEntry.VerbEntry.newPrefix("Ꭰ", normalizeEntry.pres1);
		}
		if (normalizeEntry.pres1.startsWith("Ꮥ")){
			normalizeEntry.pres1=LyxEntry.VerbEntry.chopPrefix(normalizeEntry.pres1);
		}
		if (normalizeEntry.past.startsWith("Ꮪ")){
			normalizeEntry.past=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.past);
		}				
		if (normalizeEntry.inf.startsWith("Ꮷ")){
			normalizeEntry.inf=LyxEntry.VerbEntry.newPrefix("Ꭴ", normalizeEntry.inf);
		}
		warnIfStartsWithAnyRange("Ꮣ", "Ꮫ", normalizeEntry);
	}
}