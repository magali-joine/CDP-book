package net.cherokeedictionary.main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
	}

	private void importSqlFile(Db dbc) {
		StringBuilder create=new StringBuilder();
		
		try {
			List<String> lines = FileUtils.readLines(new File(infile), "UTF-8");
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
			StringBuilder statement = new StringBuilder();
			/*
			 * look for create table
			 */
			iline = lines.listIterator();
			while(iline.hasNext()) {
				String line=iline.next();
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
			
			iline = lines.listIterator();
			while (iline.hasNext()) {
				String line=iline.next();
				line=line.replace("ENGINE=InnoDB", "");
				line=line.replace("DEFAULT CHARSET=utf8", "");
				line=line.replaceAll("AUTO_INCREMENT=\\d+", "");
				create.append(line);
				if (line.endsWith(";")) {
					break;
				}
			}			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		
		try (Connection db = dbc.makeConnection()) {
			db.createStatement().execute(create.toString());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private Db initH2() {
		File h2file=new File("output/tmp-db");
		if (h2file.exists()) {
			h2file.delete();
		}
		return new H2Db(h2file);		
	}
}
