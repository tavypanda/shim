package com.google.code.shim.data;

/**
 * Data access exception is thrown by DAOs to handle other kinds of underlying exceptions.
 * This gives any DAO implementor the ability to wrap the different kinds of exceptions that
 * occur depending on whether you are using an ORM framework or native JDBC to do data access.
 * @author dgau
 *
 */
public class DataAccessException extends Exception {
 
	private static final long serialVersionUID = 1L;

	public DataAccessException() {
		super();
	}

	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataAccessException(String message) {
		super(message);
	}

	public DataAccessException(Throwable cause) {
		super(cause);
	}

	
}
