package com.google.code.shim.data.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.code.shim.collections.StringKeyMap;
import com.google.code.shim.data.BaseDao;
import com.google.code.shim.data.DataAccessException;
import com.google.code.shim.data.UnavailableException;
import com.google.code.shim.data.sql.handler.JsonObjectHandler;
import com.google.code.shim.data.sql.handler.RowHandler;
import com.google.code.shim.data.sql.handler.RowListHandler;

/**
 * Provides basic capabilities for all DAOs. Implementing method naming conventions and behaviors will help save you
 * time implementing and maintaining your data access layer. The following conventions are recommended:
 * <p>
 * General conventions:
 * </p>
 * <ul>
 * <li>{@link DataAccessException}s (for example, wrapping a SQLException) may be thrown from any method.</li>
 * </ul>
 * 
 * <p>
 * Query method (SELECT) conventions:
 * </p>
 * <ul>
 * <li>Method names should begin with 'get' or 'find'.</li>
 * <li>Methods returning a single {@link StringKeyMap} of data should either return the {@link Map<String,
 * Object>} or null if the query returned no results.</li>
 * <li>Methods returning multiple {@link StringKeyMap}s of data should either return a List<{@link Map<String,
 * Object> }> or Collections.EMPTY_LIST if the query returned no results.</li>
 * <li>Methods returning scalar values should return the scalar or the corresponding empty/null type depending on the
 * type of scalar being returned.</li>
 * </ul>
 * 
 * <p>
 * Delete method conventions:
 * </p>
 * <ul>
 * <li>The method name should begin with 'delete'.</li>
 * <li>Methods may have void return signature, however a row count of affected rows is sometimes also helpful.</li>
 * </ul>
 * 
 * 
 * 
 * <p>
 * Update method conventions:
 * </p>
 * <ul>
 * <li>The method name should begin with the name 'update'.</li>
 * <li>Methods may have void return signature, however a row count of affected rows is sometimes also helpful.</li>
 * </ul>
 * 
 * <p>
 * Insert method conventions:
 * </p>
 * <ul>
 * <li>The method name should begin with the name 'insert' or 'add'.</li>
 * <li>Methods may have void return signature, however a row count of affected rows is sometimes also helpful..</li>
 * </ul>
 * 
 * 
 * <p>
 * Insert-Update (e.g. "Upsert") method conventions:
 * </p>
 * <ul>
 * <li>The method name should begin with the name 'save'.</li>
 * <li>Methods may have void return signature, however a row count of affected rows is sometimes also helpful.</li>
 * </ul>
 * 
 * @author dgau
 * 
 */
public abstract class BaseSqlDao extends BaseDao {
	protected static final Logger logger = LogManager.getLogger(BaseSqlDao.class);
	private final DataSource ds;
	private final DialectInfo dialect;
	private boolean parameterMetadataSupport = true;
	/**
	 * Every DAO must be instantiated with a reference to a JNDI data source.
	 * 
	 * @param injectedDs
	 * @throws DataAccessException
	 */
	public BaseSqlDao(DataSource injectedDs) throws DataAccessException {
		super();
		this.ds = injectedDs;
		if(logger.isDebugEnabled())
			logger.debug("Assuming generic database dialect.");
		try {
			dialect = new DialectInfo();
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
	}

	/**
	 * You can also create a DAO with a particular SQL dialect. Currently only a "generic" dialect is supported.
	 * 
	 * @param injectedDs
	 * @param dialectName
	 * @throws DataAccessException
	 */
	public BaseSqlDao(DataSource injectedDs, String dialectName) throws DataAccessException {
		super();
		this.ds = injectedDs;
		if (dialectName == null || "".equals(dialectName.trim())) {
			throw new DataAccessException("Dialect was not specified.");
		}
		try {
			dialect = new DialectInfo(dialectName);
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
	}
	/**
	 * Adjust whether the driver supports parameter metadata (some don't).  Defaults to true.
	 * @param driverSupportsParameterMetadata
	 */
	public void setParameterMetadataSupport(boolean driverSupportsParameterMetadata){
		parameterMetadataSupport = driverSupportsParameterMetadata;
	}

	/**
	 * Every DAO must be instantiated with a reference to a JNDI data source.
	 * 
	 * <p>
	 * This constructor accepts a string that specifies the JNDI datasource name which will be used to lookup the
	 * datasource.
	 * </p>
	 * 
	 * 
	 * @param dsName
	 * @throws DataAccessException
	 *             when a datasource cannot be returned
	 */
	public BaseSqlDao(String dsName) throws DataAccessException {
		super();
		try {
			Context ctx = new InitialContext();
			this.ds = (DataSource) ctx.lookup(dsName);
			logger.info("Assuming generic database dialect.");
			try {
				dialect = new DialectInfo();
			} catch (IOException e) {
				throw new DataAccessException(e);
			}
		} catch (NamingException e) {
			throw handleException(e);
		}
	}

	/**
	 * Every DAO must be instantiated with a reference to a JNDI data source.
	 * 
	 * <p>
	 * This constructor accepts a string that specifies the JNDI datasource name which will be used to lookup the
	 * datasource, as well as the sql dialect name if you are using a dialect-specific extension.
	 * </p>
	 * 
	 * 
	 * @param dsName
	 * @param dialectName
	 * @throws DataAccessException
	 */
	public BaseSqlDao(String dsName, String dialectName) throws DataAccessException {
		super();
		try {
			Context ctx = new InitialContext();
			this.ds = (DataSource) ctx.lookup(dsName);
			if (dialectName == null || "".equals(dialectName.trim())) {
				throw new DataAccessException("Dialect was not specified.");
			}
			try {
				this.dialect = new DialectInfo(dialectName);
			} catch (IOException e) {
				throw new DataAccessException(e);
			}
		} catch (NamingException e) {
			throw handleException(e);
		}
	}

	/**
	 * Gets the data source.
	 * 
	 * @return the SQL DataSource
	 */
	protected DataSource getDataSource() {
		return this.ds;
	}

	/**
	 * Executes a query that returns a single scalar value. The query is specified by a property that is derived by
	 * concatenating "sql." with the calling method name. Thus if you invoke this method from inside a method named
	 * getMyValue, it will look for the sql statement in the properties file under the property, "sql.getMyValue". This
	 * convention can be useful when managing SQL in a property file.
	 * 
	 * @param queryParms
	 *            parameters to add to the SQL statement for the query.
	 * @return a StringKeyMap containing the data or null if not found.
	 * @throws DataAccessException
	 */
	public Object selectValueEasily(Object... queryParms) throws DataAccessException {
		// Get the name of the method that called THIS method.
		String callingMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		String sqlPropname = "sql." + callingMethodName;
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropname);
		}

		return selectValueUsingProperty(sqlPropname, queryParms);
	}

	/**
	 * Executes a query that returns a single scalar value. The query is specified by a property name.
	 * 
	 * @param sqlPropname
	 *            the property in the property file for this DAO that contains the SQL statement to use
	 * @param queryParms
	 *            parameters to be used in the SQL (replacing the placeholder '?' in each the SQL statement)
	 * @return
	 * @throws DataAccessException
	 */
	public Object selectValueUsingProperty(String sqlPropname, Object... queryParms) throws DataAccessException {
		try {
			String sql = buildSelectSQL(getSQLFromProperty(sqlPropname), queryParms);
			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			return qr.query(sql, new ScalarHandler(), queryParms);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * Executes a query containing java message resource parameters, that returns a single scalar value. The query is
	 * specified by a property name.
	 * 
	 * @param sqlPropname
	 *            the property in the property file for this DAO that contains the SQL statement to use
	 * @param messageParms
	 *            message parameters to substitute into the statement at the parameter locations ( replacing parameters
	 *            as given by the #java.text.MessageFormat documentation ).
	 * @param queryParms
	 *            parameters to be used in the SQL (replacing the placeholder '?' in each the SQL statement)
	 * @return
	 * @throws DataAccessException
	 */
	public Object selectValueUsingPropertyAndMessageParms(String sqlPropname, Object[] messageParms,
		Object... queryParms) throws DataAccessException {
		try {
			String sql = buildSelectSQL(getSQLFromProperty(sqlPropname), messageParms);
			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			return qr.query(sql, new ScalarHandler(), queryParms);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * Returns a single map containing data for a row in a database table. Similar to the
	 * {@link #selectSingleUsingProperty(String, Object...)} method except that the property name is derived by
	 * concatenating "sql." + the calling method name. Thus if you call this getOne method from inside a method named
	 * getCustomerById, it will look for the sql statement in the properties file under the property,
	 * "sql.getCustomerById". This is a good way to implement a consistent naming convention for all your daos.
	 * 
	 * @param queryParms
	 *            parameters to add to the SQL statement for the query.
	 * @return a StringKeyMap containing the data or null if not found.
	 * @throws DataAccessException
	 */
	public final StringKeyMap selectSingleEasily(Object... queryParms) throws DataAccessException {
		// Get the name of the method that called THIS method.
		String sqlPropName = "sql." + deriveMethodNameFromStackTrace(3);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropName);
		}

		return selectSingleUsingProperty(new RowHandler(), sqlPropName, queryParms);
	}
	/**
	 * Queries a single row from the database.
	 * @param handler controls the form of output returned
	 * @param queryParms   parameters to add to the SQL statement for the query.
	 * @return the output of the single row, controlled by the handler
	 * @throws DataAccessException
	 */
	public final <T> T selectSingleEasily(ResultSetHandler<T> handler, Object... queryParms) throws DataAccessException {
		// Get the name of the method that called THIS method.
		String sqlPropName = "sql." + deriveMethodNameFromStackTrace(3);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropName);
		}

		return selectSingleUsingProperty(handler, sqlPropName, queryParms);
	}


	/**
	 * Returns a list of maps containing data for several rows in a database table. Similar to the
	 * {@link #selectMultipleUsingProperty(String, Object...) } method except that the property name is derived by
	 * concatenating "sql." + the enclosing method name. Thus if you call this getOne method from inside a method named
	 * getCustomerById, it will look for the sql statement in the properties file under the property,
	 * "sql.getCustomerById". This is a good way to implement a consistent naming convention for all your daos.
	 * 
	 * @param queryParms
	 *            parameters to add to the SQL statement for the query.
	 * @return a StringKeyMap containing the data or null if not found.
	 * @throws DataAccessException
	 */
	public final List<StringKeyMap> selectMultipleEasily(Object... queryParms) throws DataAccessException {
		String sqlPropname = "sql." + deriveMethodNameFromStackTrace(3);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropname);
		}

		return selectMultipleUsingProperty(new RowListHandler(), sqlPropname, queryParms);
	}

	/**
	 * 
	 * @param handler
	 * @param queryParms
	 * @return
	 * @throws DataAccessException
	 */
	public final <T> T selectMultipleEasily(ResultSetHandler<T> handler, Object... queryParms) throws DataAccessException {
		String sqlPropname = "sql." + deriveMethodNameFromStackTrace(3);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropname);
		}

		return selectMultipleUsingProperty(handler, sqlPropname, queryParms);
	}
	 

	/**
	 * Issues a delete statement. By convention, the method will assume a property exists of the form: "sql." + [name of
	 * method that called this method].
	 * 
	 * @param queryParms
	 *            parameters to be passed into the sql statement.
	 * @return number of rows affected.
	 * @throws DataAccessException
	 */
	public final int deleteEasily(Object... queryParms) throws DataAccessException {
		// Get the name of the method that called THIS method.
		String sqlPropname = "sql." + deriveMethodNameFromStackTrace(3);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropname);
		}
		return this.deleteUsingProperty(sqlPropname, queryParms);
	}

	/**
	 * Issues an insert statement. By convention, the method will assume a property exists of the form: "sql." + [name
	 * of method that called this method].
	 * 
	 * @param dataToInsert
	 *            data to be inserted, keyed by column names.
	 * @return map of the data after insert, which may now contain any generated keys that were generated during the
	 *         insert.
	 * @throws DataAccessException
	 */
	public final StringKeyMap insertEasily(Map<String, Object> dataToInsert) throws DataAccessException {
		// Get the name of the method that called THIS method.
		String sqlPropname = "sql." + deriveMethodNameFromStackTrace(3);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropname);
		}
		return this.insertUsingProperty(sqlPropname, new StringKeyMap(dataToInsert));
	}

	/**
	 * Issues an update statement. By convention, the method will assume a property exists of the form: "sql." + [name
	 * of method that called this method].
	 * 
	 * @param dataToUpdate
	 *            map of data containing data to update AND the criteria values for the update.
	 * @param criteriaFields
	 *            database column names for the map data to be used as the criteria in the where clause
	 * @return number of rows updated
	 * @throws DataAccessException
	 */
	public final int updateEasily(Map<String, Object> dataToUpdate, String... criteriaFields) throws DataAccessException {
		// Get the name of the method that called THIS method.
		String sqlPropName = "sql." + deriveMethodNameFromStackTrace(3);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property name: " + sqlPropName);
		}
		return this.updateUsingProperty(sqlPropName, new StringKeyMap(dataToUpdate), criteriaFields);
	}

	/**
	 * Issues a select statement that returns a single row from the database.
	 * 
	 * @param sqlPropname
	 *            property that specifies the parameterized SQL select statement.
	 * @param queryParms
	 *            object array of parameters to be passed into the statement.
	 * @return a map that represents a row returned from the query. The map keys are strings, and are the column names
	 *         (or column aliases) that were indicated in the sql statement.
	 * @throws DataAccessException
	 *             which may wrap a SQLException or other exception
	 */
	public <T> T selectSingleUsingProperty(ResultSetHandler<T> handler, String sqlPropname, Object... queryParms)
		throws DataAccessException {
		try {
			String sql = buildSelectSQL(getSQLFromProperty(sqlPropname), queryParms);
			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			return qr.query(sql, handler, queryParms);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	

	private String getSQLFromProperty(String sqlPropName) {
		String sql = getStringProperty(sqlPropName);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property value: " + sql);
		}
		return sql;
	}

	/**
	 * Builds and returns a complete SQL statement when the statement template contain both MessageFormat parameters.
	 * This most commonly occurs when you need to populate a comma-separated list of values for a SQL WHERE IN and the
	 * jdbc replacement parameter '?' will not suffice.  In that use case  This method handles escaping of single-quotes (if any) in the statement
	 * to ensure the {@link MessageFormat#format(String, Object...) } method does not destroy them.
	 * 
	 * @param sql
	 *            base statement that may contain escapes for the sql.defaultColumns property.
	 * @param messageParms
	 *            , if any to be spliced in to the SQL statement if it contains the '{n}' message format parameters. If
	 *            you have no message parms strings to specify, use {@link #buildSelectSQL(String, Object...)} instead.
	 * 
	 * @return sql with any additional column modifications spliced in.
	 */
	protected String buildSelectSQL(String sql, Object... messageParms) {

		if (messageParms != null && messageParms.length > 0) {
			// First escape all the single quotes so the message format mechanism doesn't destroy them.
			sql = sql.replaceAll("'", "''");
			sql = MessageFormat.format(sql, messageParms);
		}

		

		return sql;

	}

	
	/**
	 * Parameterized method allows the specification fo a result set handler for the query.
	 * @param handler
	 * @param sqlPropname
	 * @param queryParms
	 * @return
	 * @throws DataAccessException
	 */
	public <T> T selectMultipleUsingProperty(ResultSetHandler<T> handler, String sqlPropname, Object... queryParms)
		throws DataAccessException {
		try {
			String sql = buildSelectSQL(getSQLFromProperty(sqlPropname), queryParms);
			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			return qr.query(sql, handler, queryParms);

		} catch (Exception e) {
			throw handleException(e);
		}
	}

	 
	/**
	 * Issues a select statement that may return multiple rows.
	 * 
	 * @param handler produces the correct return type of output
	 * @param sql
	 *            the parameterized SQL select statement.
	 * @param queryParms
	 *            object array of parameters to be passed into the statement.
	 * @return the returned type specified by the handler
	 * @throws DataAccessException
	 */
	public <T> T selectMultipleUsingStatement(ResultSetHandler<T> handler, String sql, Object... queryParms)
		throws DataAccessException {
		try {
			buildSelectSQL(sql, queryParms);
			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			return qr.query(sql, handler, queryParms);
		} catch (Exception e) {
			throw handleException(e);
		}
	} 
	
 
	
	/**
	 * Issues a select statement that returns a single row of data.
	 * 
	 * @param handler produces the correct return type of output
	 * @param sql
	 *            the parameterized SQL select statement.
	 * @param queryParms
	 *            object array of parameters to be passed into the statement.
	 * @return the returned type specified by the handler
	 * @throws DataAccessException
	 */
	public  <T> T  selectSingleUsingStatement(ResultSetHandler<T> handler, String sql, Object... queryParms) throws DataAccessException {
		try {

			buildSelectSQL(sql, queryParms);
			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			return qr.query(sql, handler, queryParms);
			
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * 
	 * @param sql
	 * @param queryParms
	 * @return
	 * @throws DataAccessException
	 */
	public JSONObject selectSingleUsingStatementJson(String sql, Object... queryParms) throws DataAccessException {
		try {

			buildSelectSQL(sql, queryParms);
			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			JSONObject results = qr.query(sql, new JsonObjectHandler(), queryParms);

			return results;
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * Issues an insert statement.
	 * 
	 * @param sqlPropname
	 *            property that specifies the parameterized SQL insert statement.
	 * @param mapOfData
	 *            data, keyed by column names to be inserted.
	 * @return the map of data as specified in the parameter, which may now include any auto-generated keys or columns
	 *         as part of the insert.
	 * @throws DataAccessException
	 *             which may wrap a SQLException or other kind of exception.
	 */
	public StringKeyMap insertUsingProperty(String sqlPropname, StringKeyMap mapOfData)
		throws DataAccessException {

		String sql = getStringProperty(sqlPropname);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property value: " + sql);
		}

		return insertUsingStatement(sql, mapOfData);
	}

	public StringKeyMap insertUsingStatement(String sql, StringKeyMap mapOfData)
		throws DataAccessException {
		try {

			// Need to parse the SQL to determine the param string.
			String columnsString = null;
			if (sql.contains("(")) {
				try {
					int paramBegin = sql.indexOf("(");
					int paramEnd = sql.indexOf(")", paramBegin);
					columnsString = sql.substring(paramBegin + 1, paramEnd);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new DataAccessException("Could not detect columns in insert statement: " + sql);
				}
			} else {
				throw new DataAccessException("Could not detect columns in insert statement: " + sql);
			}
			String[] columnsArr = columnsString.replaceAll(" ", "").split(",");
			int parameterLimit = 0;
			for (char c : sql.toCharArray()) {
				if (c == '?') {
					parameterLimit++;
				}
			}

			// Go through the columns, and pull values out of the map. Stop
			// doing that when you reach
			// the parameter limit.
			Object[] theValues = new Object[parameterLimit];
			for (int p = 0; p < parameterLimit; p++) {
				String column = columnsArr[p];
				Object obj = mapOfData.get(column);
				// Note: putting nulls in is OK. DBUtils handles the conversion
				// to null values
				// in the QueryRunner.fillStatement method.
				theValues[p] = obj;
			}

			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			Connection conn = null;
			PreparedStatement insert = null;
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("insert sql: " + sql);
					StringBuilder vals = new StringBuilder();
					vals.append("[");
					for (Object val : theValues) {
						vals.append(val);
						vals.append(", ");
					}
					if (vals.toString().endsWith(", ")) {
						vals.setLength(vals.length() - 2);
					}
					vals.append("]");
					logger.debug("     parms: " + vals.toString());

				}
				conn = qr.getDataSource().getConnection();
				insert = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				qr.fillStatement(insert, theValues);
				int rows = insert.executeUpdate();
				if (logger.isDebugEnabled()) {
					logger.debug(rows + " rows affected");
				}
				// Add generated keys from in the statement.
				addGeneratedKeysToMap(insert, mapOfData);

			} finally {
				DbUtils.close(insert);
				DbUtils.close(conn);
			}

			return mapOfData;

		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * Issues a dynamically-generated update statement, based on the data passed in on the map, and the the indicated
	 * criteria keys.
	 * 
	 * @param sqlPropname
	 *            specifies the property where the UPDATE statement has been given. The update statement must be of the
	 *            form: <code>update mytable set {0}, changed_date=getdate() 
	 * where column_a=? and column_b=?</code>. Note that if you want to include columns set by database functions (such
	 *            as <code>getdate()</code> in this example), you should add them to the sql statement AFTER the
	 *            <code>{0}</code> placeholder (as shown).
	 * @param mapOfData
	 *            map of data to be updated. Note that this method dynamically builds the SET clause based on the
	 *            columns given in the map. This map should also contain the data for the criteria keys as well.
	 * @param criteriaKeys
	 *            database column names for the map data to be used as the criteria in the where clause. In the example
	 *            above, you would pass <code>"column_a", "column_b"</code> in as the criteria keys.
	 * @return the number of rows affected
	 * @throws DataAccessException
	 */
	public int updateUsingProperty(String sqlPropname, StringKeyMap mapOfData, String... criteriaKeys)
		throws DataAccessException {

		String sql = getStringProperty(sqlPropname);
		if (logger.isDebugEnabled()) {
			logger.debug("sql property value: " + sql);
		}
		return updateUsingStatement(sql, mapOfData, criteriaKeys);
	}

	public int updateUsingStatement(String sql, StringKeyMap mapOfData, String... criteriaKeys)
		throws DataAccessException {
		try {

			// Fill in the 'set {0}' clause.
			StringBuilder setClause = new StringBuilder();
			ArrayList<Object> orderedValues = new ArrayList<Object>();
			for (String key : mapOfData.keySet()) {
				boolean isCriteria = false;
				for (String criteriaKey : criteriaKeys) {
					if (criteriaKey.equals(key)) {
						isCriteria = true;
						break;
					}

				}
				if (!isCriteria) {
					setClause.append(key).append(" = ?,");
					orderedValues.add(mapOfData.get(key));
				}
			}
			// Now, add the criteria fields in order to the ordered values.
			for (String criteriaKey : criteriaKeys) {
				orderedValues.add(mapOfData.get(criteriaKey));
			}
			// At this point the ordered values should be ordered perfectly to
			// match the parameterized statement
			Object[] theValues = orderedValues.toArray();
			// Replace the placeholder in the setClause with what we just built.
			sql = MessageFormat.format(sql, setClause.toString());
			// Just in case the sql now has consecutive commas in it, trim these
			// out.
			sql = sql.replaceAll(",\\s*,", ",");

			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);
			Connection conn = null;
			PreparedStatement update = null;
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("update sql: " + sql);
					StringBuilder vals = new StringBuilder();
					vals.append("[");
					for (Object val : theValues) {
						vals.append(val);
						vals.append(", ");
					}
					if (vals.toString().endsWith(", ")) {
						vals.setLength(vals.length() - 2);
					}
					vals.append("]");
					logger.debug("     parms: " + vals.toString());

				}
				conn = qr.getDataSource().getConnection();
				update = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				qr.fillStatement(update, theValues);
				int rows = update.executeUpdate();
				return rows;

			} finally {
				DbUtils.close(update);
				DbUtils.close(conn);
			}

		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * Used internally to add generated keys into an existing map of data.
	 * 
	 * @param statement
	 *            a prepared statement that was just executed and not closed.
	 * @param mapOfData
	 *            into which you want the generated keys placed. The keys used are the labels that the JDBC driver
	 *            returns for auto generated keys.
	 * @throws SQLException
	 */
	static void addGeneratedKeysToMap(PreparedStatement statement, StringKeyMap mapOfData) throws SQLException {
		// Add generated keys from in the statement.
		ResultSet genKeys = statement.getGeneratedKeys();
		try {
			ResultSetMetaData meta = genKeys.getMetaData();
			int colCount = meta.getColumnCount();
			if (colCount > 0) {
				String[] genLabels = new String[colCount];
				for (int c = 1; c <= colCount; c++) {
					genLabels[c - 1] = meta.getColumnLabel(c);
				}
				while (genKeys.next()) {
					for (String label : genLabels) {
						if (logger.isDebugEnabled()) {
							logger.debug("Generated key labeled: " + label + " will be added to results.");
						}
						// Put the generated keys in the map.
						mapOfData.put(label, genKeys.getObject(label));
					}
				}
			}
		} finally {
			DbUtils.close(genKeys);
		}
	}

	/**
	 * Issues a delete statement.
	 * 
	 * @param sqlPropName
	 *            property that specifies the parameterized SQL delete statement.
	 * @param queryParms
	 *            object array of parameters to be passed into the statement.
	 * @return number of rows affected
	 * @throws DataAccessException
	 *             which may wrap a SQLException or other kind of exception.
	 */
	public int deleteUsingProperty(String sqlPropName, Object... queryParms) throws DataAccessException {
		try {
			String sql = getStringProperty(sqlPropName);
			if (logger.isDebugEnabled()) {
				logger.debug("sql property value: " + sql);
			}

			QueryRunner qr = new QueryRunner(getDataSource(),!parameterMetadataSupport);

			if (logger.isDebugEnabled()) {
				logger.debug("delete sql: " + sql);
				StringBuilder vals = new StringBuilder();
				vals.append("[");
				if (queryParms != null) {
					for (Object val : queryParms) {
						vals.append(val);
						vals.append(", ");
					}
					if (vals.toString().endsWith(", ")) {
						vals.setLength(vals.length() - 2);
					}
				}
				vals.append("]");
				logger.debug("     parms: " + vals.toString());

			}
			// Note the underlying connection must be in autocommit mode.
			int rowsDeleted = qr.update(sql, queryParms);

			return rowsDeleted;

		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * Overrides base exception to provide checking for various types of SQLExceptions.
	 * 
	 * @param e
	 * @return a DataAccessException that wraps the given exception. Special handling is included for detecting certain
	 *         SQLException sql states and instantiating subclasses of DataAccessException as appropriate.
	 */
	protected DataAccessException handleException(Exception e) {
		logger.error(e.getMessage(), e);
		if (e instanceof DataAccessException) {
			return (DataAccessException) e;

		} else if (e instanceof SQLException) {
			SQLException s = (SQLException) e;
			String sqlState = s.getSQLState();
			
			if (sqlState == null && s.getCause() != null && s.getCause() instanceof SQLException)
				sqlState = ((SQLException) s.getCause()).getSQLState();
			
			logger.error("sqlstate: "+s.getSQLState());
			logger.error("error code: "+s.getErrorCode());
			logger.error("message: "+s.getMessage());
		
			if (sqlState == null || sqlState.isEmpty()) {
				DataAccessException dae = new DataAccessException(s);
				return dae;
			} else if (dialect.isDatabaseUnavailableSQLState(sqlState)) {
				// Mark the database as unavailable.
				return new UnavailableException(dialect.getSQLStateDescription(sqlState), e);
			} else {
				// Could add additional varieties here.
				String description = dialect.getSQLStateDescription(sqlState);
				if (description == null || "".equals(description.trim())) {
					description = e.getMessage() + "(" + s.getMessage() + ")";
				}
				DataAccessException dae = new DataAccessException(dialect.getSQLStateDescription(sqlState), s);
				return dae;
			}

		} else {

			DataAccessException dae = new DataAccessException(e);
			return dae;
		}
	}


}
