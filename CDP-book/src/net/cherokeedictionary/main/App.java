package net.cherokeedictionary.main;

import java.io.File;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.db.H2Db;
import net.cherokeedictionary.lyx.LyxExportFile;

public class App extends Thread {

	private static final String infile = "input/CherokeeDictionaryProject.sql";
	private static final String lyxfile = "output/CherokeeDictionary.lyx";

	@Override
	public void run() {
		Db dbc = initH2();
		new ImportSqlFile(dbc, infile).run();
		new LyxExportFile(dbc, lyxfile).run();
	}

	private Db initH2() {
		File h2file = new File("output/tmp-db");
		return new H2Db(h2file);
	}
}
