package com.google.code.shim.data.sql.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.csvreader.CsvReader;
import com.google.code.shim.data.DataAccessException;
import com.google.code.shim.data.sql.BaseSqlDao;
 
/**
 * Load utility.  Allows you to load delimited files into database tables directly, 
 * following certain file formatting conventions.
 * @author dgau
 *
 */
public class Loader extends BaseSqlDao {
	static Logger logger = LogManager.getLogger(Loader.class); 
	
	static SimpleDateFormat lenientISODateTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
	static{
		lenientISODateTime.setLenient(true);
	}
	public Loader(DataSource dsToUse) throws DataAccessException{
		super(dsToUse); 
	}
	/**
	 * 
	 * Loads a database table from a file (specified by a classpath resource).  Tthe following file conventions are required:
	 * <ol>
	 * <li>the file must have the name: [table name].[filesuffix]   </li>
	 * <li>the file must contain a single header line whose header names exactly correspond to the columns in the table</li>
	 * <li>For "upsert" behavior, id lookup is necessary (to do an update instead of an insert).  To indicate which columns
	 * are ids, suffix the column name with an asterisk '*'.  Key columns MUST come before non-key columns in your file format
	 * (Compound keys are acceptable).  </li> 
	 * 
	 * </ol>
	 * @param delimiter must be provided
	 * @param charsetName
	 * @throws IOException
	 * @throws DataAccessException 
	 * @throws ParseException 
	 */
	public void load(String resourceName, char delimiter, String charsetName ) throws IOException, DataAccessException, ParseException {
		InputStream in = getClass().getResourceAsStream(resourceName);
		try{
			if(charsetName==null || charsetName.isEmpty()){
				charsetName="US-ASCII";
			}
			
			//Derive the table name.
			int suffixBegin = resourceName.lastIndexOf('.');
			String table = resourceName.substring(0, suffixBegin);
			table = table.substring(resourceName.lastIndexOf('/')+1);
			 
			//Begin scanning the file.
			CsvReader reader = new CsvReader (in, delimiter, Charset.forName(charsetName));
			
			//Read the headers
			reader.readHeaders();
			String[] rawColumns = reader.getHeaders();
			String[] columns = new String[rawColumns.length];
			
			reader.readRecord();
			String[] dataTypes = reader.getValues();
			
			//Scan for key and keep track of where they are in the statement.
			ArrayList<Integer> keyValueIndices = new ArrayList<Integer>();
			ArrayList<Integer> updateValueIndices  = new ArrayList<Integer>();
			
			ArrayList<String> keys = new ArrayList<String>();
			for(int i=0; i<rawColumns.length; i++){
				String column=rawColumns[i];
				if(column.endsWith("*")){
					keys.add(column.substring(0,column.lastIndexOf("*")));
					keyValueIndices.add(i);
					columns[i] = column.substring(0,column.lastIndexOf("*"));
				} else {
					updateValueIndices.add(i);
					columns[i] = rawColumns[i];
				}
			} 
			
			//Rename
			
			//Build the SQL.
			//Insert
			StringBuilder insertSql = new StringBuilder();
			insertSql.append("insert into " ).append(table).append(" (");
			for(int i=0;i<columns.length; i++){
				if(columns[i].endsWith("*")){
					insertSql.append(columns[i].substring(0,columns[i].lastIndexOf("*")));
				} else {
					insertSql.append(columns[i]);
				}
				if(i<columns.length-1){
					insertSql.append(", ");
				}
			}
			insertSql.append(" ) values ( ");
			for(int i=0;i<columns.length; i++){
				insertSql.append("?");
				
				if(i<columns.length-1){
					insertSql.append(", ");
				}
			}
			insertSql.append(" )");
			if(logger.isDebugEnabled()){
				logger.debug("Insert SQL: " + insertSql.toString());
			}
			
			PreparedStatement insert = getDataSource().getConnection().prepareStatement(insertSql.toString());
			
			
			//Select and Update
			PreparedStatement select = null;
			PreparedStatement update = null;
			boolean upsertMode = !keys.isEmpty();
			if(upsertMode){
				
				//Select
				StringBuilder selectSql = new StringBuilder();
				selectSql.append("select count(*) from " ).append(table).append(" where ");
				Iterator<String> keyIter = keys.iterator();
				while(keyIter.hasNext()){
					String column = keyIter.next();
					selectSql.append(column).append("=?");
					if(keyIter.hasNext()){
						selectSql.append(" and ");
					}
				}
				
				if(logger.isDebugEnabled()){
					logger.debug("Select SQL: " + selectSql.toString());
				}
				select = getDataSource().getConnection().prepareStatement(selectSql.toString());
				
				
				//Update
				StringBuilder updateSql = new StringBuilder();
				updateSql.append("update " ).append(table).append(" set ");
				for(int i=0;i<columns.length; i++){
					if(columns[i].endsWith("*")){
						continue;
					}
					updateSql.append(columns[i]).append("=?");
					if(i<columns.length-1){
						updateSql.append(", ");
					}
				}
				if(!keys.isEmpty()){
				updateSql.append(" where ");
				keyIter = keys.iterator();
				while(keyIter.hasNext()){
					String column = keyIter.next();
					updateSql.append(column).append("=?");
					if(keyIter.hasNext()){
						updateSql.append(" and ");
					}
				}
				}
				if(logger.isDebugEnabled()){
					logger.debug("Update SQL: " + updateSql.toString());
				}
				
				
				update = getDataSource().getConnection().prepareStatement(updateSql.toString());
				
			}
			
			//Loop through the rest of the file.
			QueryRunner qr = new QueryRunner(getDataSource());
			while(reader.readRecord()){
				String[] stringValues = reader.getValues();
				
				Object[] values = convert(dataTypes, stringValues);
				if(upsertMode){
					Object[] valueParmsForSelect = new String[ keyValueIndices.size() ];
					Object[] valueParmsForUpdate = new String[ values.length ];
					int idx1 = 0;//for update
					//First the "SET" parameters.
					for(Integer idx: updateValueIndices){
						valueParmsForUpdate[idx1++] = values[idx];
					}
					//Then the "WHERE" parameters
					int idx2=0;//for select
					for(Integer idx: keyValueIndices){
						valueParmsForUpdate[idx1++] = values[idx];
						valueParmsForSelect[idx2++] = values[idx];
					}
					
					//Select count
					qr.fillStatement(select,  valueParmsForSelect );
					ResultSet rs = select.executeQuery();
					int count = 0;
					while(rs.next()){
						count = rs.getInt(1);
					}
					rs.close();
					if(count>0){
						qr.fillStatement(update, valueParmsForUpdate);
						update.executeUpdate();
					} else {
						qr.fillStatement(insert, values);
						insert.executeUpdate();
					}
					
				} else {
					qr.fillStatement(insert, values);
					insert.executeUpdate();
				}
				
				
				
			}
			
			DbUtils.closeQuietly( select );
			DbUtils.closeQuietly( insert );
			DbUtils.closeQuietly( update );
			
			
		} catch (SQLException e){
			throw handleException(e);
		} finally {
			in.close();
		}
		
	}
	
	/**
	 * Converts the string data from the reader into objects, as the database framework uses type-detection to 
	 * make the appropriate parameter SETs
	 * @param dataTypes
	 * @param stringValues
	 * @return
	 * @throws ParseException
	 */
	private static Object[] convert(String[] dataTypes, String[] stringValues) throws ParseException {
		Object[] converted = new Object[stringValues.length];
		for(int i = 0; i< stringValues.length; i++){
			String dataTypeName = dataTypes[i];
			String rawValue = stringValues[i];
			Object convertedValue = null;
			if(rawValue==null){
				continue;
			}
			if("varchar".equalsIgnoreCase(dataTypeName)){
				convertedValue = rawValue;
			} else if ("integer".equalsIgnoreCase(dataTypeName)){
				convertedValue = Integer.parseInt(rawValue);
			} else if ("boolean".equalsIgnoreCase(dataTypeName)){
				convertedValue = Short.parseShort(rawValue);
			} else if ("decimal".equalsIgnoreCase(dataTypeName)){
				convertedValue = Double.parseDouble(rawValue);
			} else if ("numeric".equalsIgnoreCase(dataTypeName)){
				convertedValue = Double.parseDouble(rawValue);
			} else if ("datetime".equalsIgnoreCase(dataTypeName)){
				convertedValue = lenientISODateTime.parse(rawValue);
			}
				
			converted[i] = convertedValue;
		}
		return converted;
	}
}
