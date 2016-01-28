package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import net.cherokeedictionary.db.Db;

public class ImportSqlFile {

	private final Db dbc;
	private final String infile;

	public ImportSqlFile(Db dbc, String infile) throws SQLException {
		this.dbc = dbc;
		this.infile = infile;
		System.out.println("\tImporting SQL");
		importSqlFile();
		cleanupDb();
		keepOnlyCed();
		removeNoSyllabaryEntries();
		removeGrammarEntries();
		removeInvalidSyllabaryEntries();
		fixPronunciations();
		dehyphenSyllabary();
	}

	private static final String[] sfields={"SYLLABARYB", "NOUNADJPLURALSYLLF", "VFIRSTPRESH", "VSECONDIMPERSYLLN",
			"VTHIRDINFSYLLP", "VTHIRDPASTSYLLJ", "VTHIRDPRESSYLLL"};
	private void dehyphenSyllabary() throws SQLException {
		try (Connection db = dbc.openConnection()) {
			for (String field : sfields) {
				PreparedStatement ps = db.prepareStatement(
						"update likespreadsheets " + "set " + field + "=replace(" + field + ", '-', '')");
				ps.execute();
			}
		}		
	}

	private static final String[] searchList = { "?", "A.", "E.", "I.", "O.", "U.", "V.", "a.", "e.", "i.", "o.", "u.",
			"v.", "1", "2", "3", "4" };
	private static final String[] replacementList = { "ɂ", "̣A", "̣E", "Ị", "Ọ", "Ụ", "Ṿ", "ạ", "ẹ", "ị", "ọ", "ụ", "ṿ",
			"¹", "²", "³", "⁴" };
	private static final String[] pfields = { "entrytone", "nounadjpluraltone", "vfirstprestone", "vsecondimpertone",
			"vthirdinftone", "vthirdpasttone", "vthirdprestone" };

	private void fixPronunciations() throws SQLException {
		try (Connection db = dbc.openConnection()) {
			for (String field : pfields) {
				PreparedStatement ps = db.prepareStatement(
						"update likespreadsheets " + "set " + field + "=replace(" + field + ", ?, ?)");
				for (int ix = 0; ix < searchList.length; ix++) {
					ps.setString(1, searchList[ix]);
					ps.setString(2, replacementList[ix]);
					ps.addBatch();
				}
				ps.executeBatch();
			}
		}
	}

	private void removeInvalidSyllabaryEntries() throws SQLException {
		try (Connection db = dbc.openConnection()) {
			Statement s = db.createStatement();
			s.execute("delete from likespreadsheets where regexp_replace(syllabaryb, '[Ꭰ-Ᏼ ,]+', '') != ''");
			int removeCount = s.getUpdateCount();
			if (removeCount > 0) {
				System.out.println("Removed " + removeCount + " with invalid syllabaryb values.");
			}
		}
	}

	private void removeGrammarEntries() throws SQLException {
		try (Connection db = dbc.openConnection()) {
			Statement s = db.createStatement();
			s.execute("delete from likespreadsheets where definitiond like '%(see %'");
			int removeCount = s.getUpdateCount();
			if (removeCount > 0) {
				System.out.println("Removed " + removeCount + " see ... records.");
			}
			s.execute("delete from likespreadsheets where entrya like '-%'");
			removeCount = s.getUpdateCount();
			if (removeCount > 0) {
				System.out.println("Removed " + removeCount + " suffix records.");
			}
			s.execute("delete from likespreadsheets where entrya like '%-'");
			removeCount = s.getUpdateCount();
			if (removeCount > 0) {
				System.out.println("Removed " + removeCount + " prefix records.");
			}
		}
	}

	private void removeNoSyllabaryEntries() throws SQLException {
		try (Connection db = dbc.openConnection()) {
			Statement s = db.createStatement();
			s.execute("delete from likespreadsheets where length(syllabaryb)=0");
			int removeCount = s.getUpdateCount();
			if (removeCount > 0) {
				System.out.println("Removed " + removeCount + " no syllabary records.");
			}
		}
	}

	private void keepOnlyCed() throws SQLException {
		try (Connection db = dbc.openConnection()) {
			Statement s = db.createStatement();
			s.execute("delete from likespreadsheets where source != 'ced'");
			int removeCount = s.getUpdateCount();
			if (removeCount > 0) {
				System.out.println("Removed " + removeCount + " nonCED records.");
			}
		}
	}

	private void cleanupDb() {
		List<String> fields_to_trim = new ArrayList<>();
		try (Connection db = dbc.openConnection()) {
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery("show columns from likespreadsheets");
			while (rs.next()) {
				String field = rs.getString("FIELD");
				String type = rs.getString("TYPE").toLowerCase();
				if (!type.startsWith("varchar") && !type.startsWith("clob")) {
					continue;
				}
				fields_to_trim.add(field);
			}
			rs.close();
			for (String field : fields_to_trim) {
				s.execute("update likespreadsheets set " + field + "='' where " + field + " is null");
				int nullUpdateCount = s.getUpdateCount();
				if (nullUpdateCount > 0) {
					// System.out.println("Updated "+nullUpdateCount+" records
					// where "+field+" was null");
				}
				s.execute("update likespreadsheets set " + field + "=TRIM(" + field + ") where " + field + " != TRIM("
						+ field + ")");
				int trimUpdateCount = s.getUpdateCount();
				if (trimUpdateCount > 0) {
					System.out.println("Updated " + trimUpdateCount + " records where " + field + " needed trimming");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void importSqlFile() {
		StringBuilder sql = new StringBuilder();
		List<String> lines;
		try {
			lines = FileUtils.readLines(new File(infile), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		/*
		 * Strip all lines of extra whitespace to simplify parsing
		 */
		ListIterator<String> iline = lines.listIterator();
		while (iline.hasNext()) {
			String line = StringUtils.strip(iline.next());
			line = line.replace("\\'", "''");
			iline.set(line);
		}
		/*
		 * simplisticly assumes mysql exported statments end with ";" as the
		 * last char on the line
		 */
		// List Iterator
		iline = lines.listIterator();
		while (iline.hasNext()) {
			String line = iline.next();
			if (line.startsWith("--")) {
				continue;
			}
			if (line.startsWith("/*")) {
				continue;
			}
			line = line.replace("ENGINE=InnoDB", "");
			line = line.replace("DEFAULT CHARSET=utf8", "");
			line = line.replaceAll("AUTO_INCREMENT=\\d+", "");
			sql.append(line);
			if (line.endsWith(";")) {
				String sql_string = sql.toString();
				sql.setLength(0);
				if (sql_string.toLowerCase().startsWith("lock")) {
					continue;
				}
				if (sql_string.toLowerCase().startsWith("unlock")) {
					continue;
				}
				if (sql_string.startsWith("INSERT INTO `user_search`")) {
					continue;
				}
				doSql(sql_string);
			}
		}
	}

	private void doSql(String sql) {
		try (Connection db = dbc.openConnection()) {
			Statement s = db.createStatement();
			s.execute(sql);
		} catch (SQLException e) {
			try {
				FileUtils.write(new File("output/bad.sql"), sql, "UTF-8");
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			throw new RuntimeException("Sql Error");
		}
	}

}
