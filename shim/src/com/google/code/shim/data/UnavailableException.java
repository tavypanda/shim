package com.google.code.shim.data;

/**
 * A specific data access exception thrown when the source of data
 * cannot be reached (either through connection error or invalid credentials).
 * @author dgau
 *
 */
public class UnavailableException extends DataAccessException {
 
	private static final long serialVersionUID = 1L;

	public UnavailableException() {
		super();
	}

	public UnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnavailableException(String message) {
		super(message);
	}

	public UnavailableException(Throwable cause) {
		super(cause);
	}

	
}
