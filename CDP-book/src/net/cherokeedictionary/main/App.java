package net.cherokeedictionary.main;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.db.H2Db;
import net.cherokeedictionary.lyx.LyxExportFile;


public class App extends Thread {

	private static final String infile = "input/CherokeeDictionaryProject.sql";
	private static final String lyxfile = "output/CherokeeDictionary.lyx";

	@Override
	public void run() {
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
		System.out.println();
		System.out.println("--- STARTED AT: "+cal.getTime());
		System.out.println();
		
		Db dbc = initH2();
		new ImportSqlFile(dbc, infile).run();
		new LyxExportFile(dbc, lyxfile).run();
		
		cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
		System.out.println();
		System.out.println("--- FINISHED AT: "+cal.getTime());
		System.out.println();
	}

	private Db initH2() {
		File h2file = new File("output/tmp-db");
		return new H2Db(h2file);
	}
}
