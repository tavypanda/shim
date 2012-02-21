package com.google.code.shim.data.sql.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

import com.google.code.shim.collections.StringKeyMap;

/**
 * <code>ResultSetHandler</code> implementation that converts the first
 * <code>ResultSet</code> row into a <code>StringKeyMap</code>. 
 *
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class RowHandler implements ResultSetHandler<StringKeyMap> {

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
	public StringKeyMap handle(ResultSet rs) throws SQLException {
		return  rs.next() ?  new StringKeyMap(convert.toMap(rs)) : null; 
	}

	

}
