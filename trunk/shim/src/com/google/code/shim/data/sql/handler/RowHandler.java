package com.google.code.shim.data.sql.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

/**
 * <code>ResultSetHandler</code> implementation that converts the first <code>ResultSet</code> row into a
 * <code>Map<String,Object></code>.
 * 
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class RowHandler implements ResultSetHandler<Map<String, Object>> {

	/**
	 * The RowProcessor implementation to use when converting rows into Maps.
	 */
	private final RowProcessor convert;

	public RowHandler() {
		super();
		convert = new OrderedBasicRowProcessor();
	}

	public RowHandler(RowProcessor r) {
		super();
		convert = r;
	}

	@Override
	public Map<String, Object> handle(ResultSet rs) throws SQLException {
		return rs.next() ? new LinkedHashMap<String, Object>(convert.toMap(rs)) : null;
	}

}
