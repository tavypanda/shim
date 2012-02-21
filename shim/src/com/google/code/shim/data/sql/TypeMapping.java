package com.google.code.shim.data.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.csvreader.CsvReader;

public class TypeMapping {

	private final DbType databaseType;
	
	public TypeMapping(DbType dbtype, String driver) throws IOException{
		databaseType=dbtype;
		
		InputStream stream = TypeMapping.class.getResourceAsStream("/com/google/code/shim/sql/"+ dbtype.name().toLowerCase() + "-" + driver +".txt"); 
		BufferedReader br = new BufferedReader( new InputStreamReader(stream)) ;
		CsvReader reader = new CsvReader( br );
		//TODO
	}
	 
}
