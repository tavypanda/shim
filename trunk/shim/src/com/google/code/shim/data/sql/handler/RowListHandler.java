package com.google.code.shim.data.sql.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import com.google.code.shim.collections.StringKeyMap;

/**
 * <code>ResultSetHandler</code> implementation that converts a <code>ResultSet</code> into a <code>List</code> of
 * <code>Map</code>s. This class is thread safe.
 * 
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class RowListHandler extends AbstractListHandler<StringKeyMap> {

	/**
	 * The RowProcessor implementation to use when converting rows into Maps.
	 */
	private final RowProcessor convert;

	public RowListHandler() {
		super();
		convert = new OrderedBasicRowProcessor();
	}
	
	public RowListHandler(RowProcessor r) {
		super();
		convert = r;
	}

	/**
	 * Converts the <code>ResultSet</code> row into a <code>StringKeyMap</code> object.
	 * 
	 * @param rs
	 *            <code>ResultSet</code> to process.
	 * @return A <code>StringKeyMap</code>, never null.
	 * 
	 * @throws SQLException
	 *             if a database access error occurs
	 * 
	 * @see org.apache.commons.dbutils.handlers.AbstractListHandler#handle(ResultSet)
	 */
	@Override
	protected StringKeyMap handleRow(ResultSet rs) throws SQLException {
		Map<String, Object> result = convert.toMap(rs);

		return new StringKeyMap(result);
	}

}
