package com.google.code.shim.data.sql;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

/**
 * Basic type mapper that maps
 * <ul>
 * <li>CLOB to String</li>
 * <li>NUMERIC to Double</li>
 * <li>DECIMAL to Double</li>
 * <li>Timestamp to java.util.Date</li>
 * </ul>
 * All other type conversions are unaltered.
 * 
 * @author dgau
 * 
 */
public class BasicResultSetTypeMapper implements ResultSetTypeMapper {

	@Override
	public Object mapValue(int sqlType, Object fromValue) throws SQLException {
		if (fromValue == null)
			return null;

		switch (sqlType) {
		case Types.CLOB:
			Clob clob = (Clob) fromValue;
			return clob.getSubString(1, (int) clob.length());

		case Types.TIMESTAMP:
			Timestamp ts = (Timestamp) fromValue;
			return new Date(ts.getTime());

		case Types.NUMERIC:
		case Types.DECIMAL:
			if (fromValue instanceof BigDecimal) {
				BigDecimal bigd = (BigDecimal) fromValue;
				return bigd.doubleValue();
			}
		}
		return fromValue;

	}

}
