package com.google.code.shim.formats;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.XML;
/**
 * Provides static helper methods for interacting with JSON files and JSON formatted data.
 * @author dgau
 *
 */
public class JsonHelper {

	static Logger logger = LogManager.getLogger(JsonHelper.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String jsonFileName = null;

		for (String arg : args) {

			if (arg.startsWith("-json")) {
				jsonFileName = arg.substring(arg.indexOf("=") + 1);
			}

		}
		if (jsonFileName == null) {
			System.err.println("A json file is required.");
			System.exit(-1);
		}

		try {
			String jsonText = toJsonFromFile(jsonFileName);

			JSONObject jo = new JSONObject(jsonText);
			System.out.println(XML.toString(jo));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.exit(-1);
		}

	}

	/**
	 * Returns a JSON Object given the specified string, which should be a fully
	 * qualified URL whos resource is of JSON format.
	 * 
	 * @param urlString
	 * @return a json object
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject jsonFromUrlString(String urlString) throws IOException, JSONException {
		URL theUrl = new URL(urlString);
		InputStream input = theUrl.openStream();
		if (input != null) {
			int data = -1;
			StringBuilder buffer = new StringBuilder();
			do {
				data = input.read();
				if (data > -1) {
					buffer.append((char) data);
				}

			} while (data > -1);
			return new JSONObject(buffer.toString());
		}
		return null;
	}

	/**
	 * Returns a JSON Object given the specified string, which should be a fully
	 * qualified URL whos resource is of JSON format.
	 * 
	 * @param theUrl
	 * @return a json object
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject jsonFromUrl(URL theUrl) throws IOException, JSONException {
		InputStream input = theUrl.openStream();
		if (input != null) {
			int data = -1;
			StringBuilder buffer = new StringBuilder();
			do {
				data = input.read();
				if (data > -1) {
					buffer.append((char) data);
				}

			} while (data > -1);
			return new JSONObject(buffer.toString());
		}
		return null;
	}

	/**
	 * Returns a string containing JSON-formatted data. The string can be used
	 * to instantiated a JSONObject when needed. Internally, this method does
	 * validate the contents of the file, throwing a JSONException if the format
	 * is invalid.
	 * 
	 * @param fileName
	 * @return a json-formatted string
	 * @throws IOException
	 * @throws JSONException
	 */
	public static String toJsonFromFile(String fileName) throws IOException, JSONException {

		JSONObject jo = jsonFromFilename(fileName);
		return jo.toString();

	}

	/**
	 * Returns a JSONObject from the specified file. Internally, this method
	 * does validate the contents of the file, throwing a JSONException if the
	 * format is invalid.
	 * 
	 * @param fileName
	 * @return a json object
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject jsonFromFilename(String fileName) throws IOException, JSONException {
		File file = new File(fileName);	
		return jsonFromFile(file);
	}

	 
	
	/**
	 * Returns a JSONObject from the specified file. Internally, this method
	 * does validate the contents of the file, throwing a JSONException if the
	 * format is invalid.
	 * 
	 * @param file
	 * @return a json object
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject jsonFromFile(File file) throws IOException, JSONException {

		// Get the JSON string from a config file
		FileReader reader = new FileReader(file);
		char[] readBuffer = new char[100];
		StringBuilder json = new StringBuilder();
		while (reader.read(readBuffer) > 0) {
			json.append(readBuffer);
		}
		reader.close();

		JSONObject jo = new JSONObject(json.toString());

		return jo;
	}
	
	/**
	 * Reads in json configuration from a character-based reader.
	 * @param reader any kind of character based reader.
	 * @return a json object
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject jsonFromReader(Reader reader) throws IOException, JSONException {

		// Get the JSON string from a config file
		char[] readBuffer = new char[100];
		StringBuilder json = new StringBuilder();
		while (reader.read(readBuffer) > 0) {
			json.append(readBuffer);
		}
		reader.close();

		JSONObject jo = new JSONObject(json.toString());

		return jo;
	}
	
	/**
	 * Reads in json from an input stream (assumed to be character-based).
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject jsonFromInputStream(InputStream input) throws IOException, JSONException {
		if (input != null) {
			int data = -1;
			StringBuilder buffer = new StringBuilder();
			do {
				data = input.read();
				if (data > -1) {
					buffer.append((char) data);
				}

			} while (data > -1);
			return new JSONObject(buffer.toString());
		}
		return null;
	}
	
	/**
	 * Utility method that locates a JSONObject in the specified array that has
	 * the key value pair as specified, returning its index in the array.
	 * 
	 * @param byKey
	 *            name of the key
	 * @param havingValue
	 *            the value you're looking for (nulls are allowed)
	 * @param array
	 *            the array you're looking in
	 * @return the index of the object in the array, otherwise -1 if nothing is
	 *         found.
	 */
	public static int indexOf(String byKey, String havingValue, JSONArray array) throws JSONException {
		// Search
		if (array == null || byKey == null) {
			return -1;
		}
		
		int idx = -1;
		for (int i = 0; i < array.length(); i++) {
			JSONObject anObj = (JSONObject) array.get(i);
			if (anObj.getString(byKey).equals(havingValue) || (anObj.isNull(byKey) && havingValue == null)) {
				idx = i;
				break;
			}
		}
		return idx;

	}
	/**
	 * Finds a string in a JSONArray and returns its position in the array.
	 * @param stringValueToFind - must not be null
	 * @param array
	 * @return the array index of the string if found, otherwise -1
	 * @throws JSONException
	 */
	public static int indexOfString(String stringValueToFind, JSONArray array) throws JSONException {
		int idx = -1;
		for(int i = 0 ; i < array.length(); i++){
			if(array.getString(i).equals(stringValueToFind)){
				 idx = i;
				 break;
			}
		}
		return idx;
	}
	
	/**
	 * Utility method that locates a JSONObject in the specified array that has
	 * the key value pair as specified, returning the JSONObject in the array if found, null otherwise.
	 * 
	 * @param byKey
	 * @param havingValue
	 * @param array
	 * @return a json object contained in the json array
	 * @throws JSONException
	 */
	public static JSONObject find(String byKey, String havingValue, JSONArray array) throws JSONException {
		int idx = indexOf(byKey,havingValue,array);
		if(idx>=0){
			return array.getJSONObject(idx);
		}
		return null;
	}
	
	
	/**
	 * Convenience method that adds each key value pair from the map onto the stringer.
	 * @param stringer
	 * @param keyValues
	 * @return
	 * @throws JSONException
	 */
	public static JSONStringer spliceMap(JSONStringer stringer, Map<String,Object> keyValues) throws JSONException{
		if(stringer==null) return null;
		if(keyValues==null ) return stringer;
		for(Map.Entry<String, Object> entry: keyValues.entrySet()){
			stringer.key(entry.getKey()).value(entry.getValue());
		}
		return stringer;
		
	}
	

	/**
	 * Convenience method that adds each key value pair from the JSONObject onto the stringer.
	 * @param stringer
	 * @param keyValues
	 * @return
	 * @throws JSONException
	 */
	public static JSONStringer spliceJSONObject(JSONStringer stringer, JSONObject obj) throws JSONException{
		if(stringer==null) return null;
		if(obj==null ) return stringer;
		String[] keys = JSONObject.getNames(obj);
		for(String key: keys){
			stringer.key(key).value(obj.get(key));
		}
		return stringer;
		
	}
	

	/**
	 * Creates an XML string from the incoming json string.
	 * If the json is invalid, an JSONException will be thrown.
	 * @param json JSON formatted string
	 * @return XML string representing the JSON
	 * @throws JSONException
	 */
	
	public static String jsonToXml(String json) throws JSONException{
		JSONObject jsonObj = new JSONObject(json);
		return jsonToXml(jsonObj);
	}
	
	/**
	 * Creates an XML string from the incoming json string.
	 * If the json is invalid, an JSONException will be thrown.
	 * 
	 * @param jobj
	 * @return
	 * @throws JSONException
	 */
	public static String jsonToXml(JSONObject jobj) throws JSONException{
		return XML.toString(jobj);
	}
	
}

