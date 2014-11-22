package net.cherokeedictionary.lyx;

import net.cherokeedictionary.db.Db;

public class LyxExportFile extends Thread {
	
	private final Db dbc;
	private final String lyxfile;

	public LyxExportFile(Db dbc, String lyxfile) {
		this.dbc=dbc;
		this.lyxfile=lyxfile;
	}

	@Override
	public void run() {
		
	}
}
