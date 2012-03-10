package com.google.code.shim.springframework;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Convenience class that is capable of extracting the spring application context statically whenever needed by your application
 * @author dgau
 *
 */
public class ApplicationContextProvider  implements ApplicationContextAware{
	private static ApplicationContext ctx = null;
	/**
	 * Get the spring application context.
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return ctx;
	}
	
	/**
	 * Setter that sets a static application context reference on this class.
	 */
	public void setApplicationContext(ApplicationContext theContext) throws BeansException {
		ctx = theContext;
	}
}
