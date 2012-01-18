package com.google.code.shim.data.sql.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.dbutils.ResultSetHandler;
import org.json.JSONException;
import org.json.JSONObject;
 
/**
 * For resultsets of a single row.  The row will be returned as a single JSONObject.
 * @author dgau
 *
 */
public class JsonObjectHandler implements ResultSetHandler<JSONObject> {

	@Override
	public JSONObject handle(ResultSet rs) throws SQLException {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int cols = rsmd.getColumnCount();

			JSONObject result = new JSONObject();
			while(rs.next()){
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
				//Stop at the first row.
				break;
			}

			return result;
		} catch (JSONException e) {
			throw new SQLException(e);
		}
	} 
	

}
