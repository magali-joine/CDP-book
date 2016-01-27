package net.cherokeedictionary.main;

import net.cherokeedictionary.db.Db;

public class ExportOrderedSpreadsheet {

	private static final String sql = "SELECT length(concat(syllabaryb, NOUNADJPLURALSYLLF, VFIRSTPRESH, VSECONDIMPERSYLLN, VTHIRDINFSYLLP, VTHIRDPASTSYLLJ, VTHIRDPRESSYLLL)) as l,\n" + 
			"SYLLABARYB, NOUNADJPLURALSYLLF, VFIRSTPRESH, VSECONDIMPERSYLLN,\n" + 
			"	VTHIRDINFSYLLP, VTHIRDPASTSYLLJ, VTHIRDPRESSYLLL,\n" + 
			"	ENTRYTONE, NOUNADJPLURALTONE, VFIRSTPRESTONE, VSECONDIMPERTONE, VTHIRDINFTONE, VTHIRDPASTTONE, VTHIRDPRESTONE, DEFINITIOND\n" + 
			"FROM \"PUBLIC\".LIKESPREADSHEETS\n" + 
			"order by length(concat(syllabaryb, NOUNADJPLURALSYLLF, VFIRSTPRESH, VSECONDIMPERSYLLN, VTHIRDINFSYLLP, VTHIRDPASTSYLLJ, VTHIRDPRESSYLLL)), syllabaryb";
	
	public ExportOrderedSpreadsheet(Db dbc, String orderedoutfile) {
		// TODO Auto-generated constructor stub
	}

}
