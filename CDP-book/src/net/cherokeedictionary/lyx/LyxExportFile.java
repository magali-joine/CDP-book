package net.cherokeedictionary.lyx;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.main.DbEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class LyxExportFile extends Thread {

	private final Db dbc;
	private final String lyxfile;

	public LyxExportFile(Db dbc, String lyxfile) {
		this.dbc = dbc;
		this.lyxfile = lyxfile;
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
		StringBuilder lyxdoc = new StringBuilder();
		String start = IOUtils.toString(getClass().getResourceAsStream(
				"/net/cherokeedictionary/lyx/LyxDocumentStart.txt"));
		lyxdoc.append(start);
		String end = IOUtils.toString(getClass().getResourceAsStream(
				"/net/cherokeedictionary/lyx/LyxDocumentEnd.txt"));
		List<DbEntry> entries = getEntries();
		removeUnwantedEntries(entries);
		fixupPronunciations(entries);
	}

	private void fixupPronunciations(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			System.out.println("Fixing Tones: "+entry.entrya+"="+entry.definitiond);
			entry.entrytone=fixToneCadenceMarks(entry.entrytone);
			entry.nounadjpluraltone=fixToneCadenceMarks(entry.nounadjpluraltone);
			entry.vfirstprestone=fixToneCadenceMarks(entry.vfirstprestone);
			entry.vsecondimpertone=fixToneCadenceMarks(entry.vsecondimpertone);
			entry.vthirdinftone=fixToneCadenceMarks(entry.vthirdinftone);
			entry.vthirdpasttone=fixToneCadenceMarks(entry.vthirdpasttone);
			entry.vthirdprestone=fixToneCadenceMarks(entry.vthirdprestone);
		}
	}

	private final String[] searchList={"?", "A.", "E.", "I.", "O.", "U.", "V.", "a.", "e.", "i.", "o.", "u.", "v.", "1", "2", "3", "4"};
	private final String[] replacementList={"ɂ", "̣A", "̣E", "Ị", "Ọ", "Ụ", "Ṿ", "ạ", "ẹ", "ị", "ọ", "ụ", "ṿ", "¹", "²", "³", "⁴"};
	private String fixToneCadenceMarks(String entrytone) {
		String result=StringUtils.replaceEach(entrytone, searchList, replacementList);
		if (result.matches(".*"+Pattern.quote(".")+".*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*\\d.*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*¹(¹²³⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*⁴(¹²³⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*²(¹²⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		if (result.matches(".*³(¹³⁴).*")) {
			System.err.println("\tBAD PRONUNCIATION ENTRY: "+entrytone+" => "+result);
		}
		return result;
	}

	/**
	 * We don't want "empty", "word parts", or
	 * "Cross references to the not-included Grammar"
	 * 
	 * @param entries
	 */
	private void removeUnwantedEntries(List<DbEntry> entries) {
		Iterator<DbEntry> ientry = entries.iterator();
		while (ientry.hasNext()) {
			DbEntry entry = ientry.next();
			if (StringUtils.isEmpty(entry.syllabaryb)) {
				ientry.remove();
				continue;
			}
			if (entry.syllabaryb.contains("-")) {
				ientry.remove();
				continue;
			}
			if (entry.definitiond.contains("(see Gram. ")) {
				ientry.remove();
				continue;
			}
		}

	}

	private List<DbEntry> getEntries() {
		List<DbEntry> list = new ArrayList<>();
		try (Connection db = dbc.makeConnection()) {
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery("select * from likespreadsheets");
			while (rs.next()) {
				DbEntry entry = new DbEntry();
				for (Field f : DbEntry.class.getFields()) {
					String simpleName = f.getType().getSimpleName();
					if (simpleName.equals("String")) {
						String name = f.getName();
						f.set(entry, rs.getString(name));
					}
				}
				list.add(entry);
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
