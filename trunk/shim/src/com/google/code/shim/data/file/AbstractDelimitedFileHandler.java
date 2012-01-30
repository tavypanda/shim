package com.google.code.shim.data.file;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

/**
 * Abstract class for handling a file. Implements "event like" processing methods for handling each row of the file.
 * 
 * @author dgau
 *
 */
public abstract class AbstractDelimitedFileHandler implements Runnable {

	static Logger logger = LogManager.getLogger(AbstractDelimitedFileHandler.class);
	private CsvReader reader;
	protected final DataSource ds;
	protected final String fileName;
	/**
	 * Zero-based counter for the number of times a row has been read from the file.
	 */
	protected String[] headers;

	/**
	 * 
	 * @param customerIndexFilename
	 *            file must be tab delimited with its first column containing the integer customer id.
	 * @param dataSource
	 */
	public AbstractDelimitedFileHandler(String theFileName, DataSource dataSource) {
		this.fileName = theFileName;
		this.ds = dataSource;

	}

	/**
	 * Called once, before any loop processing begins. By default, this method opens the file and reads the first
	 * header, placing it into the {@link #headers} variable.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {

		reader = new CsvReader(this.fileName, '\t');

		reader.readHeaders();

		headers = reader.getHeaders();

	}

	/**
	 * Implement handling that occurs immediately before the loop through the customer ids begins.
	 * 
	 * @throws Exception
	 */
	public abstract void beforeLoop() throws Exception;

	/**
	 * Implement handling for each record.
	 * 
	 * @param headers
	 *            headers for the file, if any.
	 * @param values
	 *            values for the current record.
	 * @throws Exception
	 */
	public abstract void onEachRecord(String[] headers, String[] values) throws Exception;

	/**
	 * Implement handling that occurs immediate after the loop through the records finishes.
	 * 
	 * @throws Exception
	 */
	public abstract void afterLoop() throws Exception;

	/**
	 * Gets the current loop index in the file.
	 * 
	 * @return the current loop index.
	 */
	public long getCurrentRow() {
		return this.reader == null ? -1 : this.reader.getCurrentRecord();
	}

	/**
	 * Called once, during finalization of the loop - this is also called before exceptions, if any, are thrown,
	 * allowing you to clean up any resources.
	 */
	public abstract void finish();

	/**
	 * Executes the processing. This handler returns null by default, but you can modify this by setting the
	 * {@link #returnValue} protected variable in your own implementation if you wish. Sequence of method calls is:
	 * <ol>
	 * <li>{@link #init()}, called once, before all other methodss.</li>
	 * <li>{@link #beforeLoop()}, called once, immediately before the loop begins</li>
	 * <li>{@link #onEachRecord(Integer)}, called once per each customer id in the index file</li>
	 * <li>{@link #afterLoop()}, called once, immediately after the loop concludes</li>
	 * <li></li>
	 * </ol>
	 */
	@Override
	public void run() {
		try {
			init();

			beforeLoop();
			while (reader.readRecord()) {
				
				onEachRecord(reader.getHeaders(), reader.getValues());

			}
			afterLoop();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			finish();
		}

	}

}
