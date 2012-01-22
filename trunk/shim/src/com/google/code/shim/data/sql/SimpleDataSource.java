package com.google.code.shim.data.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.code.shim.data.DataAccessException;

/**
 * DataSource implementation, wrapping a single reusable connection. This implementation does not actually pool
 * connections, however it will attempt to reuse an already initialized connection if it is has not yet been closed.
 * 
 * @author dgau
 * 
 */
public class SimpleDataSource implements DataSource {

	Logger logger = LogManager.getLogger(SimpleDataSource.class);

	private int loginTimeout = 30;// seconds

	private String dbUrl;
	private String username;
	private volatile String password;

	private Connection conn;

	public SimpleDataSource(String driverClass, String dbUrl, String username, String password)
		throws DataAccessException {
		try {
			this.dbUrl = dbUrl;
			this.username = username;
			this.password = password;

			Class.forName(driverClass).newInstance();
			// As long as it's a JDBC 4.0 driver, it should register itself and the following line is not needed,
			// as discussed at http://docs.oracle.com/javase/6/docs/api/java/sql/DriverManager.html
			// DriverManager.registerDriver(dbDriver);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new DataAccessException(e);
		}

	}

	/**
	 * Gets a connection. If {@link #getConnection()} has already been called once for the data source and the
	 * connection created was never closed, this method will simply return that connection. If the connection is closed
	 * or invalid, a new connection will be created.
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(username, password);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		if (conn != null && !conn.isValid(loginTimeout)) {
			
			 if(logger.isDebugEnabled()){
				 logger.debug("Reusing existing connection.");
			 }
			return conn;
		} else {
			logger.debug("Creating new connection.");
			this.username = username;
			this.password = password;
			// if(logger.isDebugEnabled()){
			// logger.debug("Registered JDBC drivers: ");
			// Enumeration<Driver> e = DriverManager.getDrivers();
			//
			// while(e.hasMoreElements()){
			// logger.debug(e.nextElement().getClass().getName());
			// }
			// }
			return DriverManager.getConnection(dbUrl, username, password);
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("SimpleDataSource is not a wrapper.");
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// Do nothing
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		loginTimeout = seconds;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return loginTimeout;
	}
}
