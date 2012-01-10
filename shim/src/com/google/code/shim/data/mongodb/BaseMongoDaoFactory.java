package com.google.code.shim.data.mongodb;

import com.google.code.shim.data.DataAccessException;
import com.mongodb.DB;

/**
 * Simply capable of handing back the basic Mongo DAOs that are common to all
 * apps. Generally, you'll extend this DAO factory and include getters to your
 * own additional DAOs.
 * 
 * @author dgau
 * 
 */
public abstract class BaseMongoDaoFactory {

	/**
	 * An injected reference to the datasource to use for DAOs.
	 */
	protected DB db = null;

	/**
	 * Creates a factory by injecting a datasource that will be used for the
	 * DAOs created by this factory.
	 * 
	 * @param source
	 * @throws DataAccessException
	 */
	public BaseMongoDaoFactory(DB source) throws DataAccessException {
		this.db = source;
	}

}
