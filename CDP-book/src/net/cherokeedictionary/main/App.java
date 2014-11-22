package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.cherokeedictionary.db.Db;
import net.cherokeedictionary.db.H2Db;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class App extends Thread {

	private static final String infile = "input/CherokeeDictionaryProject.sql";

	@Override
	public void run() {
		Db dbc = initH2();
		importSqlFile(dbc);
		cleanupDb(dbc);
	}

	private void cleanupDb(Db dbc) {
		List<String> fields_to_trim=new ArrayList<>();
		try (Connection db = dbc.makeConnection()) {
			Statement s = db.createStatement();
			ResultSet rs = s.executeQuery("show columns from likespreadsheets");
			while (rs.next()) {
				String field = rs.getString("FIELD");
				String type = rs.getString("TYPE");
				if (!type.startsWith("varchar")&&!type.startsWith("clob")) {
					continue;
				}
				fields_to_trim.add(field);
			}
			rs.close();
			for(String field: fields_to_trim) {
				s.execute("update likespreadsheets set "+field+"=TRIM("+field+")");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
	}

	private void importSqlFile(Db dbc) {
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
			iline.set(line);
		}
		/*
		 * simplisticly assumes mysql exported statments end with ";" as the
		 * last char on the line
		 */
		// List Iterator
		iline = lines.listIterator();
		/*
		 * look for drop table
		 */
		while (iline.hasNext()) {
			String line = iline.next();
			if (line.startsWith("/*")) {
				continue;
			}
			if (line.startsWith("--")) {
				continue;
			}
			if (!line.startsWith("DROP TABLE ")) {
				continue;
			}
			iline.previous();
			break;
		}
		/*
		 * read in drop table
		 */
		sql.setLength(0);
		while (iline.hasNext()) {
			String line = iline.next();
			sql.append(line);
			if (line.endsWith(";")) {
				break;
			}
		}
		doSql(dbc, sql.toString());

		/*
		 * look for create table
		 */
		sql.setLength(0);
		while (iline.hasNext()) {
			String line = iline.next();
			if (line.startsWith("/*")) {
				continue;
			}
			if (line.startsWith("--")) {
				continue;
			}
			if (!line.startsWith("CREATE TABLE ")) {
				continue;
			}
			iline.previous();
			break;
		}
		/*
		 * read in create table
		 */
		while (iline.hasNext()) {
			String line = iline.next();
			line = line.replace("ENGINE=InnoDB", "");
			line = line.replace("DEFAULT CHARSET=utf8", "");
			line = line.replaceAll("AUTO_INCREMENT=\\d+", "");
			sql.append(line);
			if (line.endsWith(";")) {
				break;
			}
		}
		doSql(dbc, sql.toString());

		/*
		 * scan for and process "inserts" in the remaining export, assumes no
		 * other statements of interest exist.
		 */
		while (iline.hasNext()) {
			String line = iline.next();
			if (!line.startsWith("INSERT ")) {
				continue;
			}
			sql.setLength(0);
			sql.append(line);
			sql.append("\n");
			while (iline.hasNext()) {
				line = iline.next();
				sql.append(line.replace("\\'", "''"));
				sql.append("\n");
				if (line.endsWith(";")) {
					doSql(dbc, sql.toString());
					break;
				}
			}
		}

	}

	public void doSql(Db dbc, String sql) {
		try (Connection db = dbc.makeConnection()) {
			Statement s = db.createStatement();
			s.execute(sql);
		} catch (SQLException e) {			
			throw new RuntimeException(e);
		}
	}

	private Db initH2() {
		File h2file = new File("output/tmp-db");
		return new H2Db(h2file);
	}
}
