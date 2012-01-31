package com.google.code.shim.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * An HTTP call - simplifies GET/POST/PUT/DELETE HTTP calls.  This class is used to both construct a URI, execute the call, and get response information.
 * 
 * Build HTTP calls by creating a WebCall object, and then chaining the methods to construct the call. Examples:
 * <ol>
 * <li>GET request:<br/>
 * <code>
 * WebCall call = new WebCall();<br/>
 * call.get()<br/>
			.http()<br/>.host("graphical.weather.gov")<br/>.path("xml/sample_products/browser_interface/ndfdXMLclient.php")<br/>
			.parm("lat", 42)<br/>
			.parm("lon", -87)<br/>
			.parm("product", "glance")<br/>
			.parm("begin", "2012-01-10T14:00")<br/>
			.parm("end", "2012-01-17T14:00")<br/>
			.execute();<br/>
 * </code></li>
 * <li>POST request:<br/>
 * <code>
 * WebCall call = new WebCall();<br/>
 * call.post()<br/>
			.http()<br/>.host("upcoming.yahooapis.com")<br/>.path("services/rest/")<br/>
			.formElement("method", "venue.add")<br/>
			.formElement("api_key", "foobar")<br/>
			.formElement("token", "asdflafdh")<br/>
			.formElement("venuename", "Test REST Venue.add")<br/>
			.formElement("venueaddress", "123 Privacy Ln")<br/>
			.formElement("venuecity", "Los Angeles")<br/>
			.formElement("metro_id", "1")<br/>
			.execute();<br/>
 * </code></li>
 * 
 * @author dgau
 * 
 */
public class WebCall {
	private static final Logger logger = LogManager.getLogger(WebCall.class);
	private final HttpClient httpclient = new DefaultHttpClient();

	private enum HttpReqType {
		GET, PUT, POST, DELETE;
	}

	private String uriString = "";
	private HttpEntity entity;
	private List<BasicHeader> headers = new ArrayList<BasicHeader>();// for query string
	private List<NameValuePair> qparams = new ArrayList<NameValuePair>();// for query string
	private List<NameValuePair> fparams = new ArrayList<NameValuePair>();// for form encoding
	private long duration;
	private HttpResponse response;
	private HttpReqType requestType;

	/**
	 * Empty constructor.
	 */
	public WebCall() {
	}

	/**
	 * Begin constructing an HTTP GET request.
	 * 
	 *  @return
	 * @throws HttpException
	 */
	public WebCall get() throws HttpException {
		requestType = HttpReqType.GET;
		return this;
	}
	/**
	 * Begin constructing an HTTP POST request.
	 * 
	 * @return
	 * @throws HttpException
	 */
	public WebCall post() throws HttpException {
		requestType = HttpReqType.POST;
		return this;
	}

	/**
	 * Begin constructing an HTTP PUT request.
	 * 
	 * @return
	 * @throws HttpException
	 */
	public WebCall put() throws HttpException {
		requestType = HttpReqType.PUT;
		return this;
	}
	/**
	 * Begin constructing an HTTP DELETE request.
	 * 
	 * @return
	 * @throws HttpException
	 */
	public WebCall delete() throws HttpException {
		requestType = HttpReqType.DELETE;
		return this;
	}
	/**
	 * Begin building the request URI for an HTTP call.
	 * @return
	 */
	public WebCall http() {
		uriString += "http://";
		return this;
	}
	/**
	 * Begin building the request URI for an HTTPS call.
	 * 
	 * @return
	 */
	public WebCall https() {
		uriString += "https://";
		return this;
	}
	/**
	 * Append the host to the request URI (make sure to call {@link #http()} or {@link #https()} first).
	 * @param uriHost
	 * @return
	 */
	public WebCall host(String uriHost) {
		uriString += uriHost;
		return this;
	}
	/**
	 * Append the port number to the request URI (make sure you called {@link #host(String)} before this method).
	 * @param uriPort
	 * @return
	 */
	public WebCall port(int uriPort) {
		uriString += ":" + uriPort;
		return this;
	}
	/**
	 * Append the path to the request URI.  THis method can be called multiple times to build the path context of the URI.
	 * @param uriPath
	 * @return
	 */
	public WebCall path(String uriPath) {
		if (!uriString.endsWith("/") && !uriPath.startsWith("/")) {
			uriString += "/";
		}
		uriString += uriPath;
		return this;
	}
	/**
	 * Appends a web fragment to the end of the URI.  Call this after you are done with making all your {@link #path(String)} calls.
	 * @param uriFragment
	 * @return
	 */
	public WebCall fragment(String uriFragment) {
		uriString += "#" + uriFragment;
		return this;
	}

	/**
	 * Adds a header to the call.  You can do this at any time in the URI construction process.
	 * 
	 * @param headerName
	 * @param headerValue
	 * @return
	 */
	public WebCall header(String headerName, String headerValue) {
		headers.add(new BasicHeader(headerName, headerValue));
		return this;
	}

	/**
	 * Adds a parm to the request URI directly (use this for GET requests).  You can do this at any time in the URI construction process.
	 * 
	 * @param parmName
	 * @param parmValue
	 * @return
	 */
	public WebCall parm(String parmName, Object parmValue) {
		qparams.add(new BasicNameValuePair(parmName, parmValue.toString()));
		return this;
	}

	/**
	 * Adds a parm to the body of the request (use this for urlencoded POST requests).  You can do this at any time in the URI construction process.
	 * 
	 * @param parmName
	 * @param parmValue
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public WebCall formElement(String elementName, Object elementValue) throws UnsupportedEncodingException {
		fparams.add(new BasicNameValuePair(elementName, elementValue.toString()));
		return this;
	}

	/**
	 * Adds data to the body content of the request. (use this for POST or PUT requests).  You can do this at any time in the URI construction process.
	 * 
	 * @param data
	 * @param mimeType
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws HttpException
	 */
	public WebCall payload(String data, String mimeType, String charset) throws HttpException {
		try {
			entity = new StringEntity(data, mimeType, charset);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(e);
		}

		return this;
	}

	/**
	 * Executes the request.
	 * After execution, you can use the {@link #getResponseEntity()}, {@link #getResponseEntityAsString()}, {@link #getResponseEntityAsString(String)},
	 * {@link #getStatusCode()}, {@link #getStatusReason()}, {@link #getResponseHeader(String)}, and {@link #getDuration()} methods as needed to 
	 * see how it went.
	 * @return
	 * @throws HttpException
	 */
	public WebCall execute() throws HttpException {
		try {
			uriString += "?" + URLEncodedUtils.format(qparams, "UTF-8");
			URI uri = new URI(uriString);

			// Any form elements given?
			if (this.fparams != null && !this.fparams.isEmpty()) {
				// Note, this blows away any previously set entity.
				entity = new UrlEncodedFormEntity(fparams, "UTF-8");
			}

			HttpRequestBase request = null;

			switch (requestType) {
			case GET:
				HttpGet get = new HttpGet(uri);
				request = get;
				break;

			case POST:
				HttpPost post = new HttpPost(uri);
				if (this.entity != null) {
					post.setEntity(this.entity);
				}
				request = post;
				break;

			case PUT:
				HttpPut put = new HttpPut(uri);
				if (this.entity != null) {
					put.setEntity(this.entity);
				}
				request = put;
				break;

			case DELETE:
				HttpDelete delete = new HttpDelete(uri);
				request = delete;
				break;
			}

			// Put the headers into the request.
			for (Header h : headers) {
				request.addHeader(h);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("URI: " + request.getURI());
			}

			// Finally, execute the request.
			long start = System.currentTimeMillis();
			response = httpclient.execute(request);
			this.duration = System.currentTimeMillis() - start;

		} catch (Exception e) {
			throw new HttpException(e);
		}
		return this;
	}

	/**
	 * Gets the duration of the call in milliseconds.
	 * 
	 * @return
	 */
	public long getDuration() {
		return this.duration;
	}

	/**
	 * When called after the {@link #execute()}, this returns the status code from the response, otherwise -1.
	 * 
	 * @return
	 */
	public int getStatusCode() {
		if (response == null)
			return -1;
		return response.getStatusLine().getStatusCode();
	}

	/**
	 * When called after the {@link #execute()}, this returns the status code from the response, otherwise null.
	 * 
	 * @return
	 */
	public String getStatusReason() {
		if (response == null)
			return null;
		return response.getStatusLine().getReasonPhrase();
	}

	/**
	 * When called after the {@link #execute()}, this returns the raw HttpEntity from the response, otherwise null.
	 * 
	 * @return
	 */
	public HttpEntity getResponseEntity() {
		if (response == null)
			return null;
		return response.getEntity();
	}

	/**
	 * When called after the {@link #execute()}, this returns the HttpEntity, as a String, from the response, otherwise
	 * null. The UTF-8 charset is used by default.
	 * 
	 * @return
	 * @throws HttpException
	 */
	public String getResponseEntityAsString() throws HttpException {
		try {
			if (response == null)
				return null;
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			throw new HttpException(e);
		}

	}

	/**
	 * When called after the {@link #execute()}, this returns the HttpEntity, as a String, from the response, otherwise
	 * null.
	 * 
	 * @param charset
	 * @return
	 * @throws HttpException
	 */
	public String getResponseEntityAsString(String charset) throws HttpException {
		try {
			if (response == null)
				return null;
			return EntityUtils.toString(response.getEntity(), charset);
		} catch (Exception e) {
			throw new HttpException(e);
		}

	}

	/**
	 * Returns response header information for the given header name.
	 * @param hdrName
	 * @return the headers or null if nothing was found.
	 */
	public Header[] getResponseHeader(String hdrName) throws HttpException{
		try {
			if (response == null)
				return null;
			
			return response.getHeaders(hdrName);
		} catch (Exception e) {
			throw new HttpException(e);
		}
	}
	
	public static final void main(String[] args) {
		try {

			WebCall call = new WebCall();
			call.get()
				.http().host("graphical.weather.gov").path("xml/sample_products/browser_interface/ndfdXMLclient.php")
				.parm("lat", 42.1297)
				.parm("lon", -87.8329)
				.parm("product", "glance")
				.parm("begin", "2012-01-10T14:00")
				.parm("end", "2012-01-17T14:00")
				.execute();

			System.out.println("status: " + call.getStatusCode());
			System.out.println("reason: " + call.getStatusReason());
			System.out.println("response: " + call.getResponseEntityAsString());
			System.out.println(call.getDuration() + "ms");

			call = new WebCall();
			call.post()
				.http().host("upcoming.yahooapis.com").path("services/rest/")
				.formElement("method", "venue.add")
				.formElement("api_key", "foobar")
				.formElement("token", "asdflafdh")
				.formElement("venuename", "Test REST Venue.add")
				.formElement("venueaddress", "123 Privacy Ln")
				.formElement("venuecity", "Los Angeles")
				.formElement("metro_id", "1")
				.execute();

			System.out.println("status: " + call.getStatusCode());
			System.out.println("reason: " + call.getStatusReason());
			System.out.println("response: " + call.getResponseEntityAsString());
			System.out.println(call.getDuration() + "ms");

		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}
}
