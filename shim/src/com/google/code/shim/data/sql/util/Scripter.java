package com.google.code.shim.data.sql.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.code.shim.data.DataAccessException;
import com.google.code.shim.data.sql.BaseSqlDao;
 
/**
 * Executes database scripts.
 * 
 * Each statement must end with a semicolon (this is used to delimit statements).  
 * Whitespace is ignored.
 * @author dgau
 *
 */

public class Scripter extends BaseSqlDao {
	static Logger logger = LogManager.getLogger(Scripter.class); 
	public Scripter(DataSource dsToUse) throws DataAccessException{
		super(dsToUse); 
	}
	/**
	 * 
	 * @param resourceName script file.
	 *   
	 * @throws IOException
	 * @throws DataAccessException 
	 */
	public void runScript(String resourceName ) throws IOException, DataAccessException {
		InputStream input = getClass().getResourceAsStream(resourceName);
		runScript(input);
	}
	
	public void runScript(File file) throws IOException, DataAccessException{
		FileInputStream input = new FileInputStream(file); 
		runScript(input);
	}
	
	private void runScript(InputStream input) throws IOException, DataAccessException{
		try{
		 
			int i = -1;
			StringBuilder buffer = new StringBuilder();
			while ((i = input.read()) >= 0) {
				char c = (char) i;

				// look for semicolons to execute SQL
				if (c == ';') {
					// Run sql statement
					logger.info("SQL> " + buffer.toString());
					Statement stmt = getDataSource().getConnection().createStatement();
					stmt.execute(buffer.toString());

					logger.info("SQL> complete.");
					// Reset buffer.
					buffer.setLength(0);
				} else {
					buffer.append(c);

				}
			}
			
		} catch (SQLException e){
			throw handleException(e);
		} finally {
			input.close();
		}
		
	}
}
