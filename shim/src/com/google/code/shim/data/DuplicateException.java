package com.google.code.shim.data;

/**
 * A specific data access exception thrown when, during persistence
 * operations, a duplicate key error is raised.
 * @author dgau
 *
 */
public class DuplicateException extends DataAccessException {
 
	private static final long serialVersionUID = 1L;

	public DuplicateException() {
		super();
	}

	public DuplicateException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateException(String message) {
		super(message);
	}

	public DuplicateException(Throwable cause) {
		super(cause);
	}

	
}
