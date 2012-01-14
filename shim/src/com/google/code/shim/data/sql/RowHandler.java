package com.google.code.shim.data.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import com.google.code.shim.collections.Row;

/**
 * <code>ResultSetHandler</code> implementation that converts the first
 * <code>ResultSet</code> row into a <code>Row</code>. 
 *
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class RowHandler implements ResultSetHandler<Row> {

	/**
	 * The RowProcessor implementation to use when converting rows into Maps.
	 */
	private final OrderedBasicRowProcessor convert;

	public RowHandler() {
		super();
		convert = new OrderedBasicRowProcessor();
	}

	@Override
	public Row handle(ResultSet rs) throws SQLException {
		return  rs.next() ?  new Row(convert.toMap(rs)) : null; 
	}

	

}
