package com.google.code.shim.http;

/**
 * Base exception class for all HTTP-related exceptions
 * @author dgau
 *
 */
public class HttpException extends Exception {

	
	private static final long serialVersionUID = 1L;

	public HttpException() {
		super();
	}

	public HttpException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public HttpException(String msg) {
		super(msg);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

	
	
}
