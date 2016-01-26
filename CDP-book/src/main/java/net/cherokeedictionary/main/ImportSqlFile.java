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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.jdbc.JdbcSQLException;

public class ImportSqlFile {

	private final Db dbc;
	private final String infile;

	public ImportSqlFile(Db dbc, String infile) {
		this.dbc=dbc;
		this.infile=infile;
		System.out.println("\tImporting SQL");
		importSqlFile();
		cleanupDb();
	}
	
	private void cleanupDb() {
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
			line=line.replace("\\'", "''");
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
			if (line.startsWith("--")){
				continue;
			}
			if (line.startsWith("/*")){
				continue;
			}
			line = line.replace("ENGINE=InnoDB", "");
			line = line.replace("DEFAULT CHARSET=utf8", "");
			line = line.replaceAll("AUTO_INCREMENT=\\d+", "");
			sql.append(line);
			if (line.endsWith(";")) {
				String sql_string = sql.toString();
				sql.setLength(0);
				if (sql_string.toLowerCase().startsWith("lock")){
					continue;
				}
				if (sql_string.toLowerCase().startsWith("unlock")){
					continue;
				}
				if (sql_string.startsWith("INSERT INTO `user_search`")){
					continue;
				}
				doSql(sql_string);
			}
		}
	}

	private void doSql(String sql) {
		try (Connection db = dbc.makeConnection()) {
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
