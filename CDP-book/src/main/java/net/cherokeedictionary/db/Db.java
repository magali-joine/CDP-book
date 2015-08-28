package net.cherokeedictionary.db;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.ConnectionCustomizer;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

public abstract class Db implements ConnectionCustomizer {

	final static protected Object lock = new Object();

	final private static Map<String, ComboPooledDataSource> pools;

	static {
		pools = new HashMap<String, ComboPooledDataSource>();
	}

	public static void destroy() {
		synchronized (lock) {
			try {
				AbandonedConnectionCleanupThread.shutdown();
			} catch (InterruptedException e) {
			}
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements()) {
				try {
					DriverManager.deregisterDriver(drivers.nextElement());
				} catch (SQLException e) {
				}
			}
			Iterator<ComboPooledDataSource> iter = pools.values().iterator();
			while (iter.hasNext()) {
				ComboPooledDataSource p = iter.next();
				p.setUnreturnedConnectionTimeout(1);
				p.resetPoolManager(true);
				p.hardReset();
				p.close();				
			}			
		}
	}

	protected final String init_lookup_key;

	private boolean initDone = false;

	private ComboPooledDataSource pool;

	protected Db() {
		init_lookup_key = this.getClass().getCanonicalName()+"|"+jdbcUser()+"|"+jdbcCatalog();
	}

	public ConnectionCustomizer connectionConfigClass() {
		return this;
	}

	public String getInitLookupKey() {
		return init_lookup_key;
	}

	private void init() {
		synchronized (lock) {
			if (initDone) {
				return;
			}
			pool = pools.get(getInitLookupKey());
			if (pool == null) {
				pool = initConnectionPool();
				pools.put(getInitLookupKey(), pool);
			}
			initDone = true;
		}
	}

	protected ComboPooledDataSource initConnectionPool() {

		String jdbcString = "jdbc:mysql://" + jdbcHost();
		jdbcString += "?useCompression=false"
				// + "&useUnicode=yes" + "&characterEncoding=utf8mb4"
				+ "&cacheServerConfiguration=true" + "&autoDeserialize=true"
				+ "&cachePrepStmts=true" + "&cacheCallableStmts=true"
				+ "&useLocalSessionState=true" + "&elideSetAutoCommits=true"
				+ "&alwaysSendSetIsolation=false"
				+ "&enableQueryTimeouts=false"
				+ "&zeroDateTimeBehavior=convertToNull"
				+ "&rewriteBatchedStatements=true";

		System.setProperty("com.mchange.v2.log.MLog",
				"com.mchange.v2.log.FallbackMLog");
		System.setProperty(
				"com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL",
				"WARNING");
		System.setProperty(
				"com.mchange.v2.c3p0.management.ManagementCoordinator",
				"com.mchange.v2.c3p0.management.NullManagementCoordinator");

		ComboPooledDataSource pool = new ComboPooledDataSource();
		try {
			/*
			 * TomCat memory control.
			 */
			pool.setContextClassLoaderSource("library");
			pool.setDriverClass(com.mysql.jdbc.Driver.class.getCanonicalName());
			pool.setPrivilegeSpawnedThreads(true);
		} catch (PropertyVetoException e) {
		}
		pool.setUser(jdbcUser());
		pool.setPassword(jdbcPassword());
		pool.setJdbcUrl(jdbcString);
		pool.setAcquireIncrement(1);
		pool.setAcquireRetryAttempts(99);
		pool.setAutoCommitOnClose(false);
		pool.setInitialPoolSize(0);
		pool.setMinPoolSize(0);
		pool.setMaxPoolSize(9999);
		pool.setMaxIdleTime(300);
		pool.setMaxIdleTimeExcessConnections(99);
		pool.setPreferredTestQuery("/* ping */ SELECT 1");
		pool.setTestConnectionOnCheckout(true);
		pool.setUnreturnedConnectionTimeout(900);
		pool.setDebugUnreturnedConnectionStackTraces(true);
		pool.setMaxConnectionAge(3600);
		ConnectionCustomizer connectionConfigClass = connectionConfigClass();
		if (connectionConfigClass != null) {
			pool.setConnectionCustomizerClassName(connectionConfigClass
					.getClass().getName());
		}
		return pool;
	}

	public abstract String jdbcCatalog();

	public abstract String jdbcHost();

	public abstract String jdbcPassword();

	public abstract String jdbcUser();

	/**
	 * Returns a new pooled SQL Connection object.
	 * 
	 * @return
	 */
	public Connection makeConnection() {
		if (!initDone) {
			init();
		}
		try {
			return pool.getConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onAcquire(Connection arg0, String arg1) throws Exception {
	}

	@Override
	public void onCheckIn(Connection arg0, String arg1) throws Exception {
	}

	@Override
	public void onCheckOut(Connection arg0, String arg1) throws Exception {
		arg0.setCatalog(jdbcCatalog());
		arg0.setAutoCommit(true);
		arg0.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
	}

	@Override
	public void onDestroy(Connection arg0, String arg1) throws Exception {
	}
}
