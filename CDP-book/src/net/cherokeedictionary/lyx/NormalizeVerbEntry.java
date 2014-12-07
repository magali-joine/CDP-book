package net.cherokeedictionary.lyx;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import net.cherokeedictionary.lyx.LyxEntry.VerbEntry;
import net.cherokeedictionary.main.App;

public class NormalizeVerbEntry {
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
	public static void removeDirectObject(NormalizeVerbEntry e) {
		if (!e.pres3.contains(" ")){
			return;
		}
		String object = StringUtils.getCommonPrefix(e.pres3, e.past);
		e.habit=StringUtils.removeStart(e.habit, object);
		e.pres1=StringUtils.removeStart(e.pres1, object);
		e.imp=StringUtils.removeStart(e.imp, object);
		e.pres3=StringUtils.removeStart(e.pres3, object);
		e.past=StringUtils.removeStart(e.past, object);
	}
	public static void removeᎢprefix(NormalizeVerbEntry e) {
		if (!e.pres3.startsWith("Ꭲ")){
			return;
		}
		e.pres3=VerbEntry.chopPrefix(e.pres3);
		if (e.pres1.startsWith("Ꭲ")){
			e.pres1=VerbEntry.chopPrefix(e.pres1);
		}
		if (e.past.startsWith("Ꭵ")){
			e.past=VerbEntry.chopPrefix(e.past);
		}
		if (e.habit.startsWith("Ꭲ")){
			e.habit=VerbEntry.chopPrefix(e.habit);
		}
		if (e.imp.startsWith("Ꭲ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}
		if (e.inf.startsWith("Ꭵ")){
			e.inf=VerbEntry.chopPrefix(e.inf);
		}
	}
	public static void removeᏫprefix(NormalizeVerbEntry e) {
		if (e.imp.startsWith("Ꮻ") && e.pres3.startsWith("Ꮻ")){
			e.imp=VerbEntry.newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("Ꮻ") && e.pres3.startsWith("Ꮽ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}
		if (e.imp.startsWith("Ꮻ") && e.pres3.startsWith("Ꮹ")){
			e.imp=VerbEntry.newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("ᏫᏕ") && e.pres3.startsWith("Ꮣ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}
		if (e.imp.startsWith("ᏫᏨ") && e.pres3.startsWith("Ꭴ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}				
		if (e.pres3.startsWith("Ꮹ")){
			e.pres3=VerbEntry.newPrefix("Ꭰ", e.pres3);
		}
		if (e.habit.startsWith("Ꮹ")){
			e.habit=VerbEntry.newPrefix("Ꭰ", e.habit);
		}
		if (e.pres3.startsWith("Ꮽ")){
			e.pres3=VerbEntry.newPrefix("Ꭴ", e.pres3);
		}
		if (e.habit.startsWith("Ꮽ")){
			e.habit=VerbEntry.newPrefix("Ꭴ", e.habit);
		}
		if (e.pres3.startsWith("Ꮻ")){
			e.pres3=VerbEntry.chopPrefix(e.pres3);
		}
		if (e.habit.startsWith("Ꮻ")){
			e.habit=VerbEntry.chopPrefix(e.habit);
		}
		if (e.pres1.startsWith("Ꮹ")){
			e.pres1=VerbEntry.newPrefix("Ꭰ", e.pres1);
		}
		if (e.pres1.startsWith("Ꮻ")){
			e.pres1=VerbEntry.chopPrefix(e.pres1);
		}
		if (e.past.startsWith("Ꮽ")){
			e.past=VerbEntry.newPrefix("Ꭴ", e.past);
		}
		if (e.inf.startsWith("Ꮽ")){
			e.inf=VerbEntry.newPrefix("Ꭴ", e.inf);
		}
		warnIfStartsWithAnyRange("Ꮻ", "Ꮾ", e);
	}
	
	public static void removeᏂprefix(NormalizeVerbEntry e) {
		//they and I
		if (e.pres1.startsWith("ᏃᏥ")){
			e.pres1="Ꭳ"+VerbEntry.chopPrefix(e.pres1);
		}
		//you all
		if (e.imp.startsWith("ᏂᏥ")){
			e.imp="Ꭲ"+VerbEntry.chopPrefix(e.imp);
		}
		
		if (e.imp.startsWith("Ꮒ") && e.pres3.startsWith("Ꮒ")){
			e.imp=VerbEntry.newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("Ꮒ") && e.pres3.startsWith("Ꮔ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}
		if (e.imp.startsWith("Ꮒ") && e.pres3.startsWith("Ꮎ")){
			e.imp=VerbEntry.newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("ᏂᏗ") && e.pres3.startsWith("Ꮣ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}
		if (e.imp.startsWith("ᏂᏨ") && e.pres3.startsWith("Ꭴ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}				
		if (e.pres3.startsWith("Ꮎ")){
			e.pres3=VerbEntry.newPrefix("Ꭰ", e.pres3);
		}
		if (e.habit.startsWith("Ꮎ")){
			e.habit=VerbEntry.newPrefix("Ꭰ", e.habit);
		}
		if (e.pres3.startsWith("Ꮔ")){
			e.pres3=VerbEntry.newPrefix("Ꭴ", e.pres3);
		}
		if (e.habit.startsWith("Ꮔ")){
			e.habit=VerbEntry.newPrefix("Ꭴ", e.habit);
		}
		if (e.pres3.startsWith("Ꮒ")){
			e.pres3=VerbEntry.chopPrefix(e.pres3);
		}
		if (e.habit.startsWith("Ꮒ")){
			e.habit=VerbEntry.chopPrefix(e.habit);
		}
		if (e.pres1.startsWith("Ꮎ")){
			e.pres1=VerbEntry.newPrefix("Ꭰ", e.pres1);
		}
		if (e.pres1.startsWith("Ꮒ")){
			e.pres1=VerbEntry.chopPrefix(e.pres1);
		}
		if (e.past.startsWith("Ꮔ")){
			e.past=VerbEntry.newPrefix("Ꭴ", e.past);
		}
		if (e.inf.startsWith("Ꮔ")){
			e.inf=VerbEntry.newPrefix("Ꭴ", e.inf);
		}
		warnIfStartsWithAnyRange("Ꮒ", "Ꮕ", e);
	}
	
	private static void warnIfStartsWithAnyRange(String start,
			String end, NormalizeVerbEntry normalizeVerbEntry) {
		String regex="^["+Pattern.quote(start)+"-"+Pattern.quote(end)+"].*";
		Pattern pattern = Pattern.compile(regex);
		for (String element: normalizeVerbEntry.getEntries()) {
			if (pattern.matcher(element).matches()){
				App.err("Need to add rule for: "+normalizeVerbEntry.getEntries().toString());
			}
		}
	}
	public static void removeᏕprefix(NormalizeVerbEntry e) {
		
		//you all
		if (e.imp.startsWith("ᏗᏥ")){
			e.imp="Ꭲ"+VerbEntry.chopPrefix(e.imp);
		}
		//they and I
		if (e.pres1.startsWith("ᏙᏥ")){
			e.pres1="Ꭳ"+VerbEntry.chopPrefix(e.pres1);
		}
		
		if (e.imp.startsWith("Ꮦ")){
			e.imp=VerbEntry.newPrefix("Ꭾ", e.imp);
		}
		if (e.imp.startsWith("Ꮩ")){
			e.imp=VerbEntry.newPrefix("Ꮀ", e.imp);
		}
		if (e.imp.startsWith("Ꮪ")){
			e.imp=VerbEntry.newPrefix("Ꮁ", e.imp);
		}
		if (e.imp.startsWith("Ꮫ")){
			e.imp=VerbEntry.newPrefix("Ꮂ", e.imp);
		}
		if (e.imp.startsWith("Ꮨ")){
			e.imp=VerbEntry.newPrefix("Ꭿ", e.imp);
		}
		if (e.imp.startsWith("Ꮤ")){
			e.imp=VerbEntry.newPrefix("Ꭽ", e.imp);
		}
		if (e.imp.startsWith("Ꮧ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}
		if (e.imp.startsWith("Ꮥ")){
			e.imp=VerbEntry.chopPrefix(e.imp);
		}
		if (e.pres3.startsWith("Ꮣ")){
			e.pres3=VerbEntry.newPrefix("Ꭰ", e.pres3);
		}
		if (e.habit.startsWith("Ꮣ")){
			e.habit=VerbEntry.newPrefix("Ꭰ", e.habit);
		}
		if (e.pres3.startsWith("Ꮪ")){
			e.pres3=VerbEntry.newPrefix("Ꭴ", e.pres3);
		}
		if (e.habit.startsWith("Ꮪ")){
			e.habit=VerbEntry.newPrefix("Ꭴ", e.habit);
		}
		if (e.pres3.startsWith("Ꮥ")){
			e.pres3=VerbEntry.chopPrefix(e.pres3);
		}
		if (e.habit.startsWith("Ꮥ")){
			e.habit=VerbEntry.chopPrefix(e.habit);
		}
		if (e.pres1.startsWith("Ꮣ")){
			e.pres1=VerbEntry.newPrefix("Ꭰ", e.pres1);
		}
		if (e.pres1.startsWith("Ꮥ")){
			e.pres1=VerbEntry.chopPrefix(e.pres1);
		}
		if (e.past.startsWith("Ꮪ")){
			e.past=VerbEntry.newPrefix("Ꭴ", e.past);
		}				
		if (e.inf.startsWith("Ꮷ")){
			e.inf=VerbEntry.newPrefix("Ꭴ", e.inf);
		}
		warnIfStartsWithAnyRange("Ꮣ", "Ꮫ", e);
	}
}