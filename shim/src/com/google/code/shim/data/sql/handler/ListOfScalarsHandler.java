package com.google.code.shim.data.sql.handler;

import java.sql.ResultSet;
import java.sql.SQLException; 
 
import org.apache.commons.dbutils.handlers.AbstractListHandler; 
/**
 * Takes the first column of each result set row, and builds a list of results from each row, returning the list.
 * @author dgau
 *
 * @param <T>
 */
public class ListOfScalarsHandler<T> extends AbstractListHandler<T> {

	@SuppressWarnings("unchecked")
	@Override
	protected T handleRow(ResultSet rs) throws SQLException {
		return (T) rs.getObject(1);
	}
 

}
