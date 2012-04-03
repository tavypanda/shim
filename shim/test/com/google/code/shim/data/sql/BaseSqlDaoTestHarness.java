package com.google.code.shim.data.sql;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import com.google.code.shim.collections.StringKeyMap;
import com.google.code.shim.data.DataAccessException;

/**
 * Proxy class for testing the "*easily*" methods of BaseSqlDao.
 * @author dgau
 *
 */
public class BaseSqlDaoTestHarness extends BaseSqlDao{

	public BaseSqlDaoTestHarness(DataSource injectedDs) throws DataAccessException {
		super(injectedDs);
	}
	public BaseSqlDaoTestHarness(DataSource injectedDs, String dialect) throws DataAccessException {
		super(injectedDs,dialect);
	}
	
	//
	// select value easily tests
	//
	public <T> T testSelectValueEasily(Object... queryParms) throws DataAccessException{
		return selectValueEasily(queryParms);
	}
	public <T> T testSelectValueEasilyBad(Object... queryParms) throws DataAccessException{
		return selectValueEasily(queryParms);
	}

	//
	// select single easily tests
	//
	public StringKeyMap testSelectSingleEasily(Object... queryParms) throws DataAccessException{
		return selectSingleEasily(queryParms);
	}
	
	public StringKeyMap testSelectSingleEasilyBad(Object... queryParms) throws DataAccessException{
		return selectSingleEasily(queryParms);
	}
	
	public StringKeyMap testSelectSingleEasilyWithHandler(ResultSetHandler<StringKeyMap> handler, Object... queryParms) throws DataAccessException{
		return selectSingleEasily(handler, queryParms);
	}
	
	public StringKeyMap testSelectSingleEasilyWithHandlerBad(ResultSetHandler<StringKeyMap> handler, Object... queryParms) throws DataAccessException{
		return selectSingleEasily(handler, queryParms);
	}
	
	//
	// select multiple easily tests
	//
	public List<StringKeyMap> testSelectMultipleEasily(Object... queryParms) throws DataAccessException{
		return selectMultipleEasily(queryParms);
	}
	public List<StringKeyMap> testSelectMultipleEasilyBad(Object... queryParms) throws DataAccessException{
		return selectMultipleEasily(queryParms);
	}
	public List<StringKeyMap> testSelectMultipleEasilyWithHandler(AbstractListHandler<StringKeyMap> handler, Object... queryParms) throws DataAccessException{
		return selectMultipleEasily(handler, queryParms);
	}
	public List<StringKeyMap> testSelectMultipleEasilyWithHandlerBad(AbstractListHandler<StringKeyMap> handler, Object... queryParms) throws DataAccessException{
		return selectMultipleEasily(handler, queryParms);
	}
	
	//
	// insert easily tests
	//
	public StringKeyMap testInsertEasily(StringKeyMap map) throws DataAccessException{
		return insertEasily(map);
	}
	
	//
	// update easily tests
	//
	public int testUpdateEasily(StringKeyMap map, String...criteriaFields) throws DataAccessException{
		return updateEasily(map, criteriaFields);
	}
	
	//
	// delete easily tests
	//
	public int testDeleteEasily(Object... queryParms) throws DataAccessException{
		return deleteEasily(queryParms);
	}
	
	//
	// save easily tests
	//
	public StringKeyMap testSaveEasily(StringKeyMap map, String...criteriaFields) throws DataAccessException{
		return saveEasily(map, criteriaFields);
	}
}