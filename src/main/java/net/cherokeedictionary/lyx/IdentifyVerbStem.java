package net.cherokeedictionary.lyx;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.cherokeedictionary.model.entries.LyxEntry;

public class IdentifyVerbStem {

	public static void mark(LyxEntry entryFor) {
		List<String> syll = entryFor.getSyllabary();
		if (syll.size()<3){
			return;
		}
		if (!syll.get(2).startsWith("Ꭴ")){
			return;
		}
		String _1st_presc = syll.get(1);
		if (_1st_presc.contains(",")){
			_1st_presc=StringUtils.strip(StringUtils.substringAfterLast(_1st_presc, ","));
		}
		if (!_1st_presc.startsWith("Ꮵ")&&!_1st_presc.startsWith("ᎠᎩ")) {
			entryFor.stemRootType=StemRootType.Vowel;
			return;
		}
		String _3rd_rpast = syll.get(2);
		if (_1st_presc.matches("Ꮵ[ᏯᏰᏱᏲᏳᏴ].*") && !_3rd_rpast.matches("Ꭴ[ᏯᏰᏱᏲᏳᏴ].*")) {
			entryFor.stemRootType=StemRootType.Vowel;
			return;
		}
		if (!_3rd_rpast.matches("Ꭴ[ᏩᏪᏫᏬᏭᏮ].*")) {
			entryFor.stemRootType=StemRootType.Consonent;
			return;
		}
		if (_1st_presc.matches("ᎠᎩ[ᏩᏪᏫᏬᏭᏮ].*")){
			entryFor.stemRootType=StemRootType.Consonent;
			return;
		}
		if (_1st_presc.matches("Ꮵ[ᏯᏰᏱᏲᏳᏴ].*")) {
			entryFor.stemRootType=StemRootType.Vowel;
			return;
		}
		if (_1st_presc.matches("Ꮵ[ᏩᏪᏫᏬᏭᏮ].*")) {
			entryFor.stemRootType=StemRootType.Consonent;
			return;
		}
		entryFor.stemRootType=StemRootType.GlottalStop;
	}

	public static enum StemRootType {
		Vowel, Consonent, GlottalStop, Unknown
	}
}
