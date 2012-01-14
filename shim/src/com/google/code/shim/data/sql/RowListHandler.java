package com.google.code.shim.data.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.handlers.AbstractListHandler;

import com.google.code.shim.collections.Row;

/**
 * <code>ResultSetHandler</code> implementation that converts a <code>ResultSet</code> into a <code>List</code> of
 * <code>Map</code>s. This class is thread safe.
 * 
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class RowListHandler extends AbstractListHandler<Row> {

	/**
	 * The RowProcessor implementation to use when converting rows into Maps.
	 */
	private final OrderedBasicRowProcessor convert;

	public RowListHandler() {
		super();
		convert = new OrderedBasicRowProcessor();
	}

	/**
	 * Converts the <code>ResultSet</code> row into a <code>Row</code> object.
	 * 
	 * @param rs
	 *            <code>ResultSet</code> to process.
	 * @return A <code>Row</code>, never null.
	 * 
	 * @throws SQLException
	 *             if a database access error occurs
	 * 
	 * @see org.apache.commons.dbutils.handlers.AbstractListHandler#handle(ResultSet)
	 */
	@Override
	protected Row handleRow(ResultSet rs) throws SQLException {
		Map<String, Object> result = convert.toMap(rs);

		return new Row(result);
	}

}
