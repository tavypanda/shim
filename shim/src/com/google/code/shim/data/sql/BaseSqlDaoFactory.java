package com.google.code.shim.data.sql;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.code.shim.data.DataAccessException;

/**
 * Constructs DAOs that are common to all apps. Generally, you extend this DAO
 * factory and include getters to your own additional DAOs.
 * 
 * @author dgau
 * 
 */
public abstract class BaseSqlDaoFactory {

	private static Logger logger = LogManager.getLogger(BaseSqlDaoFactory.class);

	/**
	 * An injected reference to the datasource to use for DAOs.
	 */
	protected DataSource ds = null;

	/**
	 * Creates a factory by injecting a datasource that will be used for the
	 * DAOs created by this factory.
	 * 
	 * @param source
	 * @throws DataAccessException
	 */
	public BaseSqlDaoFactory(DataSource source) throws DataAccessException {
		this.ds = source;
	}

	/**
	 * Creates a factory that will lookup datasources for the DAOs by using the
	 * specified JNDI datasource name.
	 * 
	 * @param dataSourceName
	 *            should be the JNDI datasource name
	 */
	public BaseSqlDaoFactory(String dataSourceName) throws DataAccessException {
		this.ds = lookupDataSource(dataSourceName);
		if (ds == null) {
			throw new DataAccessException("No data source for name: " + dataSourceName);
		}
	}

	/**
	 * JNDI lookup takes place in this method
	 * 
	 * @param name
	 * @return the DataSource
	 * @throws a
	 *             DataAccessException if the source cannot be found.
	 */
	public static DataSource lookupDataSource(String name) throws DataAccessException {
		try {
			Context ctx = new InitialContext();
			Context envCtx = (Context) ctx.lookup("java:comp/env");
			return (DataSource) envCtx.lookup(name);
		} catch (NamingException e) {
			e.printStackTrace();
			logger.error(e);
			throw new DataAccessException(e);
		}
	}

}
