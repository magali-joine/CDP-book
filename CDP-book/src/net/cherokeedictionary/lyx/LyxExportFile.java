package net.cherokeedictionary.lyx;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.main.DbEntry;
import net.cherokeedictionary.main.JsonConverter;

import org.apache.commons.io.IOUtils;

public class LyxExportFile extends Thread {
	
	private final Db dbc;
	private final String lyxfile;

	public LyxExportFile(Db dbc, String lyxfile) {
		this.dbc=dbc;
		this.lyxfile=lyxfile;
	}

	@Override
	public void run() {
		try {
			_run();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void _run() throws IOException {
		StringBuilder lyxdoc=new StringBuilder();		
		String start=IOUtils.toString(getClass().getResourceAsStream("/net/cherokeedictionary/lyx/LyxDocumentStart.txt"));
		lyxdoc.append(start);
		String end=IOUtils.toString(getClass().getResourceAsStream("/net/cherokeedictionary/lyx/LyxDocumentEnd.txt"));
		
		List<DbEntry> entries = getEntries();
		
	}

	private List<DbEntry> getEntries() {
		List<DbEntry> list = new ArrayList<>();
		try (Connection db = dbc.makeConnection()) {
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery("select * from likespreadsheets");
			while (rs.next()) {
				DbEntry entry = new DbEntry();
				for(Field f: DbEntry.class.getFields()) {
					String simpleName = f.getType().getSimpleName();
					if (simpleName.equals("String")) {
						String name = f.getName();
						f.set(entry, rs.getString(name));
					}
				}
				list.add(entry);
				System.out.println(new JsonConverter().toJson(entry));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return list;
	}
}
