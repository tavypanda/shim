package com.google.code.shim.data.sql.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;

import com.google.code.shim.data.sql.handler.BasicResultSetTypeMapper;

/**
 * Handles whitespace trimming on rows received.
 * 
 * @author dgau
 * 
 */
public class TrimmingRowProcessor extends BasicRowProcessor {

	private boolean trimAll = false;
	private String[] columns;
	private BasicResultSetTypeMapper mapper = new BasicResultSetTypeMapper();

	/**
	 * Indicate the column names (case insensitive) you want to be trimmed of whitespace.
	 * 
	 * @param colnamesToTrim
	 */
	public TrimmingRowProcessor(String... colnamesToTrim) {
		if (colnamesToTrim == null) {
			columns = new String[0];

		} else {

			columns = new String[colnamesToTrim.length];
			int idx = 0;
			for (String c : colnamesToTrim) {
				columns[idx++] = c.toLowerCase();
			}
			Arrays.sort(columns);
		}
	}

	/**
	 * Indicate you want all columns trimmed of whitespace in the result set.
	 * 
	 * @param all
	 */
	public TrimmingRowProcessor(boolean all) {
		trimAll = all;
	}

	@Override
	public Map<String, Object> toMap(ResultSet rs) throws SQLException {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		for (int i = 1; i <= cols; i++) {
			int columnType = rsmd.getColumnType(i);
			Object mapped = mapper.mapValue(columnType, rs.getObject(i));
			String columnName = rsmd.getColumnName(i).toLowerCase();
			if (trimAll) {

			} else {
				int whichColumn = Arrays.binarySearch(columns, columnName);
				if (whichColumn > 0 && mapped != null && mapped instanceof String) {
					// Trim the whitespace.
					mapped = ((String) mapped).trim();
				}
			}

			result.put(columnName, mapped);
		}

		return result;
	}

}
