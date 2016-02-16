package net.cherokeedictionary.main;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.db.H2Db;
import net.cherokeedictionary.lyx.LyxExportFile;


public class App {

	private static final String infile = "input/CherokeeDictionaryProject.sql";
	private static final String orderedOutfile = "input/orderedOutfile.csv";
	private static final String lyxfile = "output/CherokeeDictionary.lyx";
	private static final String formsfile = "output/WordForms.txt";
	private static final String ankiFile = "output/anki.txt";
	private static final Logger logger;

	static {
		logger=Logger.getGlobal();
		logger.setLevel(Level.INFO);
	}
	
	public static void info() {
		info("\n");
	}
	public static void info(String info) {
//		logger.log(Level.INFO, info);
		System.out.println(info);
		System.out.flush();
	}
	
	public static void err(String err) {
//		logger.log(Level.SEVERE, err);
		System.err.println(err);
		System.err.flush();
	}
	
	public static void err() {
		err("\n");
	}
	
	public App() {
		try {
			_run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void _run() throws Exception {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
		info();
		info("--- STARTED AT: "+cal.getTime());
		info();
		
//		Db dbc = initH2();
		//new ImportSqlFile(dbc, infile);
//		new ExportOrderedSpreadsheet(orderedOutfile);
		new LyxExportFile(lyxfile, formsfile);
		//new AnkiExportFile(dbc, ankiFile).run();
		
		cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("EST5EDT"));
		info();
		info("--- FINISHED AT: "+cal.getTime());
		info();
	}

	private Db initH2() {
		File h2file = new File("output/tmp-db");
		return new H2Db(h2file);
	}
}
