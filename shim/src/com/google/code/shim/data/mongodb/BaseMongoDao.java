package com.google.code.shim.data.mongodb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.code.shim.data.BaseDao;
import com.google.code.shim.data.DataAccessException;
import com.google.code.shim.data.UnavailableException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;

/**
 * Abstract DAO functionality for interacting with a MongoDB collection.
 * 
 * This DAO faciliates basic CRUD operations through its various methods, and
 * sets conventions that can be used for consistent handling of database calls.
 * 
 * @author dgau
 * 
 */
public abstract class BaseMongoDao extends BaseDao {
	static final Logger logger = LogManager.getLogger(BaseMongoDao.class);
	private final DB db;
	private final String collectionName;

	/**
	 * Every DAO must be instantiated with a reference to a Mongo DB (analogous
	 * to a JDBC DataSource)
	 * 
	 * @param injectedDb
	 *            the DB
	 * @param collName
	 *            collection name to use
	 * @throws DataAccessException
	 */
	public BaseMongoDao(DB injectedDb, String collName) throws DataAccessException {
		super();
		this.db = injectedDb;
		this.collectionName = collName;

	}

	/**
	 * Gets the DB.
	 * 
	 * @return the mongo database
	 */
	protected DB getDB() {
		return this.db;
	}

	/**
	 * Gets the collection.
	 * 
	 * @return the mongodb collection
	 */
	protected DBCollection getCollection() {
		return getDB().getCollection(collectionName);
	}

	/**
	 * Finds a mongodb object. By convention, the method will assume a property
	 * exists of the form: "mongo." + [name of method that called this method].
	 * 
	 * @param queryParms
	 *            parameters to be passed into the sql statement.
	 * @return map representing the mongodb object.
	 * @throws DataAccessException
	 */
	public Map<String,Object> findOne(Object... queryParms) throws DataAccessException {
		return findOneUsingProperty(getDerivedPropertyName(), queryParms);
	}

	/**
	 * Finds a list of mongodb objects. By convention, the method will assume a
	 * property exists of the form: "mongo." + [name of method that called this
	 * method].
	 * 
	 * @param queryParms
	 *            parameters to be passed into the sql statement.
	 * @return list of maps, each map representing a mongodb object.
	 * 
	 * @throws DataAccessException
	 */
	public List<Map<String,Object>> findMany(Object... queryParms) throws DataAccessException {
		return findManyUsingProperty(getDerivedPropertyName(), queryParms);
	}

	/**
	 * Modifies an existing document's fields. 
	 * The query parms are plugged into a JSON object to locate it.  Existing fields not in the objectToSave map
	 * will be retained as-is (this behavior is different than the {@link #save(Map, Object...)} method).  
	 * If the document is not found, a new document will be inserted.
	 * 
	 * @param objectToSave
	 * @param queryParms
	 * @throws DataAccessException
	 */
	public void modify(Map<String, Object> objectToSave, Object... queryParms) throws DataAccessException {
		saveUsingProperty(getDerivedPropertyName(), objectToSave, queryParms);
	}
	
	/**
	 * Issues a save. The query parms are plugged into a JSON object to locate
	 * the document. Please note this method will COMPLETELY REPLACE any previously
	 * existing object.  If the document is not found, a new document will be inserted.
	 * 
	 * @param objectToSave
	 * @param queryParms
	 * @throws DataAccessException
	 */
	public void save(Map<String, Object> objectToSave, Object... queryParms) throws DataAccessException {
		saveUsingProperty(getDerivedPropertyName(), objectToSave, queryParms);
	}

	/**
	 * Issues a delete. The parms are plugged into a JSON object specified by
	 * 
	 * @param queryParms
	 * @throws DataAccessException
	 */
	public void delete(Object... queryParms) throws DataAccessException {
		deleteUsingProperty(getDerivedPropertyName(), queryParms);
	}

	/**
	 * Used internally for deriving the property name containing any templated
	 * statements that Mongodb is to exeute to insert, save, query or delete
	 * data.
	 * 
	 * @return
	 */
	private final String getDerivedPropertyName() {
		String propName = "mongo." + deriveMethodNameFromStackTrace(4);
		if (logger.isDebugEnabled()) {
			logger.debug("mongo query property name: " + propName);
		}
		return propName;
	}

	/**
	 * <p>
	 * Issues a find one query.
	 * </p>
	 * 
	 * @param propertyName
	 *            name of the property where there are templated query parms for
	 *            the object.
	 * @param queryParms
	 *            query parameters to fill in the query template.
	 * @return a single map representing a mongodb document or null if nothing
	 *         was found
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object> findOneUsingProperty(String propertyName, Object... queryParms) throws DataAccessException {
		try {
			String queryJson = getStringProperty(propertyName);
			if (logger.isDebugEnabled()) {
				logger.debug("query json: " + queryJson);
			}

			// Fill parms
			queryJson = fillJson(queryJson, queryParms);

			// Restrict returned attributes?
			String fieldsJson = getStringProperty(propertyName + ".fields", null);

			// Do the query.
			DBCollection coll = getCollection();
			DBObject result = null;
			if (fieldsJson == null) {
				DBObject query = (DBObject) JSON.parse(queryJson);
				result = coll.findOne(query);
			} else {
				DBObject query = (DBObject) JSON.parse(queryJson);
				DBObject fields = (DBObject) JSON.parse(fieldsJson);
				result = coll.findOne(query, fields);
			}
			if (result == null)
				return null;
			return result.toMap();

		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * 
	 * <p>
	 * Issues a find many query.
	 * </p>
	 * <p>
	 * Note that this method will build a list from the mongodb cursor, so if
	 * your query returns a large number of documents, be mindful of the memory
	 * consuption involved with this.
	 * </p>
	 * 
	 * @param propertyName
	 *            name of the property where there are templated query parms for
	 *            the object.
	 * @param queryParms
	 *            query parameters to fill in the query template.
	 * @return list of maps. each map represents a mongodb document
	 * @throws DataAccessException
	 */
	public List<Map<String,Object>> findManyUsingProperty(String propertyName, Object... queryParms) throws DataAccessException {
		try {
			String queryJson = getStringProperty(propertyName);
			if (logger.isDebugEnabled()) {
				logger.debug("query json: " + queryJson);
			}

			// Fill parms
			if (queryJson != null) {
				queryJson = fillJson(queryJson, queryParms);
			}

			// Restrict returned attributes?
			String fieldsJson = getStringProperty(propertyName + ".fields", null);

			// Do the query.
			DBCollection coll = getCollection();
			DBCursor result = null;
			if (fieldsJson == null) {
				DBObject query = (DBObject) JSON.parse(queryJson);
				result = coll.find(query);
			} else {
				DBObject query = (DBObject) JSON.parse(queryJson);
				DBObject fields = (DBObject) JSON.parse(fieldsJson);
				result = coll.find(query, fields);
			}
			ArrayList<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> oneResult: results){
				results.add(oneResult);
			}
			return results;

		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/**
	 * Issues a save command, which will REPLACE any existing object entirelyw ith the data in the
	 * objectToSave map of data.  If the object does not exist, a new one will be inserted.
	 * 
	 * @param propertyName
	 *            name of the property where there are templated query parms for
	 *            the object.
	 * @param objectToSave
	 *            map of data to save. The map values will be converted to a
	 *            JSON object.
	 * @param queryParms
	 *            query parameters to fill in the query template.
	 * @throws DataAccessException
	 * @see <a href="http://www.mongodb.org/display/DOCS/Updating">Updating in MongoDb</a>
	 */
	public void saveUsingProperty(String propertyName, Map<String, Object> objectToSave, Object... queryParms)
		throws DataAccessException {
		try {
			String queryJson = getStringProperty(propertyName);
			if (logger.isDebugEnabled()) {
				logger.debug("query json: " + queryJson);
			}
			// If query doesn't exist, throw exception.
			if (queryJson == null || queryJson.isEmpty()) {
				throw new DataAccessException("No query was provided for the save method.");
			}

			// Fill parms
			queryJson = fillJson(queryJson, queryParms);

			// Build the query.
			DBObject query = (DBObject) JSON.parse(queryJson);
			
			
			// Create the DBObject to save.
			DBObject toSave = new BasicDBObject();
			toSave.putAll(objectToSave);
			replaceBigDecimals(toSave);

			
			// Do the save.
			DBCollection coll = getCollection();
			coll.update(query, toSave, true, false, WriteConcern.SAFE);

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				// if(objectToSave instanceof Map<String,Object>){
				// Map<String,Object>.trace( (Map<String,Object>) objectToSave );
				// }
				logger.debug("error saving: " + JSON.serialize(objectToSave));
			}
			throw handleException(e);
		}

	}
	
	/**
	 * Modifies an existing dbobject, inserting it if it does not exist.   
	 * This method modifies only the fields indicated in the  <code>objectToSave</code> map, 
	 * and  PRESERVES  existing values NOT PART of the map, making it different from 
	 * {@link #saveUsingProperty(String, Map, Object...)}.  
	 * 
	 * @param propertyName
	 *            name of the property where there are templated query parms for
	 *            the object.
	 * @param objectToSave
	 *            map of data to save. The map values will be converted to a
	 *            JSON object.
	 * @param queryParms
	 *            query parameters to fill in the query template.
	 * @throws DataAccessException
	 * @see <a href="http://www.mongodb.org/display/DOCS/Updating">Updating in MongoDb</a>
	 */
	public void modifyUsingProperty(String propertyName, Map<String, Object> objectToSave, Object... queryParms)
		throws DataAccessException {
		try {
			String queryJson = getStringProperty(propertyName);
			if (logger.isDebugEnabled()) {
				logger.debug("query json: " + queryJson);
			}
			// If query doesn't exist, throw exception.
			if (queryJson == null || queryJson.isEmpty()) {
				throw new DataAccessException("No query was provided for the save method.");
			}

			// Fill parms
			queryJson = fillJson(queryJson, queryParms);

			// Build the query.
			DBObject query = (DBObject) JSON.parse(queryJson);
			
			// Create the DBObject to save.
			DBObject toSave = new BasicDBObject();

			replaceBigDecimals(objectToSave);

			//Build the modifiers needed for the individual fields
			for(String key : objectToSave.keySet()){
				DBObject setInstruction = new BasicDBObject();
				setInstruction.put(key, objectToSave.get("key"));
				toSave.put("$set", setInstruction);
			}
			
			// Do the modification.
			DBCollection coll = getCollection();
			coll.update(query, toSave, true, false, WriteConcern.SAFE);

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				// if(objectToSave instanceof Map<String,Object>){
				// Map<String,Object>.trace( (Map<String,Object>) objectToSave );
				// }
				logger.debug("error saving: " + JSON.serialize(objectToSave));
			}
			throw handleException(e);
		}

	}


	/**
	 * Issues a delete command.
	 * 
	 * @param propertyName
	 *            name of the property where there are templated query parms for
	 *            the object.
	 * @param queryParms
	 *            query parameters to fill in the query template.
	 * @throws DataAccessException
	 */
	public void deleteUsingProperty(String propertyName, Object... queryParms) throws DataAccessException {
		try {
			String queryJson = getStringProperty(propertyName);
			if (logger.isDebugEnabled()) {
				logger.debug("json: " + queryJson);
			}
			// If query doesn't exist, throw exception.
			if (queryJson == null || queryJson.isEmpty()) {
				throw new DataAccessException("No query was provided for the delete method.");
			}

			// Fill parms
			queryJson = fillJson(queryJson, queryParms);

			// Build the delete object..
			DBObject deleteMatchesThisObject = (DBObject) JSON.parse(queryJson);

			// Do the delete
			DBCollection coll = getCollection();
			coll.remove(deleteMatchesThisObject, WriteConcern.SAFE);

		} catch (Exception e) {
			throw handleException(e);
		}

	}

	@Override
	/**
	 * Overrides
	 */
	protected DataAccessException handleException(Exception e) {
		logger.error(e.getMessage(), e);
		if (e instanceof DataAccessException) {
			return (DataAccessException) e;

		} else if (e instanceof MongoException) {
			MongoException m = (MongoException) e;

			int mongoState = m.getCode();

			switch (mongoState) {

			default:
				return new DataAccessException(m.getMessage() + "(" + m.getCode() + ")");

			}
		} else if (e instanceof java.net.ConnectException) {
			return new UnavailableException(e);

		} else {

			DataAccessException dae = new DataAccessException(e);
			return dae;
		}
	}

	/**
	 * BigDecimals are not cleanly mapped to supported data types due to 
	 * <a href="http://comments.gmane.org/gmane.comp.db.mongodb.user/34582">this issue</a>
	 * 
	 * This method iterates recursively through a given DBObject or Map to find
	 * BigDecimals and replace them with double values.
	 * 
	 * @param inObj
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void replaceBigDecimals(Object inObj) {
		// Holding collection for keys that have big decimal values.
		ArrayList<Object> bigDecimalsHere = new ArrayList<Object>();

		if (inObj instanceof Map) {
			Map map = (Map) inObj;
			// Iterate through the map to find BigDecimals.
			for (Object key : map.keySet()) {
				Object value = map.get(key);
				if (value == null)
					continue;
				if (value instanceof BigDecimal) {
					bigDecimalsHere.add(key);
				} else if (value instanceof Map) {
					replaceBigDecimals(value);
				} else if (value instanceof DBObject) {
					replaceBigDecimals(value);
				} else if (value instanceof List) {
					List list = (List) value;
					for (Object listVal : list) {
						replaceBigDecimals(listVal);
					}
				}
			}
			// Go back and replace the BigDecimals
			for (Object key : bigDecimalsHere) {
				//
				// Replace with a double value
				//
				BigDecimal bd = (BigDecimal) map.get(key);
				map.put(key, new Double(bd.doubleValue()));
			}

		} else if (inObj instanceof DBObject) {
			DBObject dbobj = (DBObject) inObj;
			// Iterate through the object to find BigDecimals.
			for (String key : dbobj.keySet()) {
				Object value = dbobj.get(key);
				if (value == null)
					continue;
				if (value instanceof BigDecimal) {
					bigDecimalsHere.add(key);
				} else if (value instanceof Map) {
					replaceBigDecimals(value);
				} else if (value instanceof DBObject) {
					replaceBigDecimals(value);
				} else if (value instanceof List) {
					List list = (List) value;
					for (Object listVal : list) {
						replaceBigDecimals(listVal);
					}
				}
			}
			// Go back and replace the BigDecimals
			for (Object key : bigDecimalsHere) {
				//
				// Replace with a double value
				//
				BigDecimal bd = (BigDecimal) dbobj.get((String) key);
				dbobj.put((String) key, new Double(bd.doubleValue()));
			}
		}

	}

	/**
	 * Fills the jsonString in the same fashion as a PreparedStatement gets
	 * filled with parameters (substituting each '?' with the corresponding parm
	 * from the array.
	 * 
	 * @param jsonString
	 *            the template
	 * @param parms
	 * @return a filled copy of the template jsonString
	 */
	protected static String fillJson(String jsonString, Object... parms) throws DataAccessException {
		Matcher matcher = Pattern.compile("\\?").matcher(jsonString);
		int count = 0;
		while(matcher.find()){
			count++;
		}
		if(count > parms.length){
			String errMsg = "Not enough parameters were provided for the JSON.  Expected " + count + " parameters, but only "+
			parms.length + " were provided";
			throw new DataAccessException(errMsg);
		}
		  
		String copy = jsonString;
		for (Object object : parms) {
			String replacement = null;
			if(object == null){ 
				replacement = "null";
			} else if (object instanceof String) {
				replacement = "\"" + ((String) object) + "\"";
			} else if (object instanceof Date) {
				long timestamp = ((Date) object).getTime();
				replacement = "" + timestamp;
			} else {
				replacement = object.toString();
			}
		 
			copy = copy.replaceFirst("\\?", "" + replacement);
		}
		return copy;
	}

}
