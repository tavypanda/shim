package com.google.code.shim.data.sql.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.dbutils.ResultSetHandler;
import org.json.JSONException;
import org.json.JSONStringer;

/**
 * <code>ResultSetHandler</code> implementation that converts a <code>ResultSet</code> into JSON string representing a
 * Google DataTable.  
 * 
 * @see <a href="http://code.google.com/apis/chart/interactive/docs/datatables_dataviews.html">Google DataTable
 *      Documentation</a>
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class GoogleDataTableHandler implements ResultSetHandler<String> {

	@Override
	public String handle(ResultSet rs) throws SQLException {
		try {
			final JSONStringer j = new JSONStringer();

			// cols
			j.object();
			j.key("cols").array();

			ResultSetMetaData m = rs.getMetaData();
			for (int i = 1; i <= m.getColumnCount(); i++) {
				String columnName = m.getColumnName(i);
				int sqlType = m.getColumnType(i);
				j.object();
				j.key("id").value(columnName);
				j.key("label").value(columnName);
				j.key("type");
				switch (sqlType) {
				case Types.BIT:
				case Types.BIGINT:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.INTEGER:
				case Types.NUMERIC:
				case Types.SMALLINT:
				case Types.TINYINT:
					j.value("number");
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
					j.value("date");
					break;
				case Types.VARCHAR:
				case Types.NVARCHAR:
				case Types.NCHAR:
				case Types.CHAR:
					j.value("string");
					break;
				default:
					j.value("not supported");
				}
				j.endObject();
			}
			j.endArray();

			// rows.
			j.key("rows").array();
			while (rs.next()) {
				j.object();
				j.key("c").array();

				// Get the value.
				for (int i = 1; i <= m.getColumnCount(); i++) {
					j.object();
					j.key("v");

					int sqlType = m.getColumnType(i);

					switch (sqlType) {
					case Types.BIT:
					case Types.BIGINT:
					case Types.DECIMAL:
					case Types.DOUBLE:
					case Types.FLOAT:
					case Types.INTEGER:
					case Types.NUMERIC:
					case Types.SMALLINT:
					case Types.TINYINT:
						j.value(rs.getObject(i));
						break;
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP:
						j.value(rs.getObject(i));
						break;
					case Types.VARCHAR:
					case Types.NVARCHAR:
					case Types.NCHAR:
					case Types.CHAR:
						j.value(rs.getObject(i));
						break;
					default:
						// not supported.
						j.value("");
					}
					// TODO:Future support for formats
					// j.key("f").value( FORMAT STUFF HERE );

					j.endObject();

				}

				j.endArray();
				j.endObject();
			}

			j.endArray();
			j.endObject();

			return j.toString();
		} catch (JSONException e) {
			throw new SQLException(e);
		}
	}

}
