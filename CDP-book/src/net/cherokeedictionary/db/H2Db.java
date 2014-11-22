package net.cherokeedictionary.db;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class H2Db extends Db {
	
	private File dbfile;
	private JdbcConnectionPool cp;

	public H2Db(String dbfile) {
		this.dbfile=new File("h2", dbfile);
		initConnectionPool();
	}

	public H2Db(File file) {
		this.dbfile=new File(file.getAbsolutePath());
		initConnectionPool();
	}

	@Override
	public String jdbcCatalog() {
		return "PUBLIC";
	}

	@Override
	public String jdbcHost() {
		return "localhost";
	}

	@Override
	public String jdbcPassword() {
		return "";
	}

	@Override
	public String jdbcUser() {
		return "";
	}

	@Override
	public Connection makeConnection() {		
		try {
			return cp.getConnection();
		} catch (SQLException e) {
			return null;
		}
	}	
	
	@Override
	protected ComboPooledDataSource initConnectionPool() {
		this.cp=JdbcConnectionPool.create(
				"jdbc:h2:"+dbfile.getAbsolutePath()+";AUTO_SERVER=TRUE;MODE=MYSQL", "", "");
		return null;
	}
}
