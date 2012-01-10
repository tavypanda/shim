package com.google.code.shim.data.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.dbutils.ResultSetHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * For resultsets of more than one row. Each row will be returned as a
 * JSONObject, wrapped in a JSONArray.
 * 
 * @author dgau
 * 
 */
public class JsonArrayHandler implements ResultSetHandler<JSONArray> {

	@Override
	public JSONArray handle(ResultSet rs) throws SQLException {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();
			JSONArray theArray = new JSONArray();
			while (rs.next()) {
				JSONObject result = new JSONObject();
				for (int i = 1; i <= cols; i++) {
					int type = rsmd.getColumnType(i);
					
					switch (type) {
					
					case Types.TIMESTAMP:
						Date theDate = rs.getDate(i);
						if(rs.wasNull()) continue;
						result.put(rsmd.getColumnName(i), theDate.getTime());
						break;

					default:
						Object theValue = rs.getObject(i);
						if(rs.wasNull()) continue;
						
						result.put(rsmd.getColumnName(i), theValue);
					}
					
				}
				theArray.put(result);
			}
			return theArray;
		} catch (JSONException e) {
			throw new SQLException(e);
		}
	}

}
