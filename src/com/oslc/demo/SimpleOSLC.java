package com.oslc.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;


public class SimpleOSLC {

	public static final String id = "admin";
	public static final String pwd = "";

	public static String eTag = null;

	public static void main(String[] args) {
		// example
		// http://quagmire.rtp.raleigh.ibm.com/cqweb/oslc/repo/7.0.0/db/SAMPL/record/16777224-33597287
		//SimpleOSLC.runExample(args[0]);
		SimpleOSLC.runExample("http://localhost/cqweb/oslc/repo/oslcdemo/db/OSLC/record/16777224-33554433");
	}
	
	public static void runExample(String uri) {
		// initialize HTTP client
		HttpClient http = new HttpClient();
		Credentials credential = new UsernamePasswordCredentials(id, pwd);
		http.getParams().setParameter("http.protocol.single-cookie-header", true);
		http.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		http.getParams().setAuthenticationPreemptive(true);
		http.getState().setCredentials(AuthScope.ANY, credential);

		// prepare and execute GET request
		String responseBody = doGET(http, uri);
		
		System.out.println(responseBody);

		ByteArrayInputStream bais;
		try {
			bais = new ByteArrayInputStream(responseBody.getBytes("UTF-8"));
			//model.read(bais, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// fetch the updated record to view the changes
		doGET(http, uri);
	}

	private static String doGET(HttpClient http, String uri) {
		GetMethod get = new GetMethod();
		try {
			get.setURI(new URI(uri, false, "UTF-8"));
			get.setRequestHeader("OSLC-Core-Version", "2.0");
			get.setRequestHeader("Accept", "application/rdf+xml");
			System.out.println("GET: " + uri + "\n");
			int status = http.executeMethod(get);
			if (status != HttpStatus.SC_OK) {
				throw new Exception("GET response status not 200 OK, received " + status);
			}

			Header eTagHeader = get.getResponseHeader("ETag");
			if (eTagHeader != null) {
				eTag = eTagHeader.getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// retrieve the response as a string
		String responseBody = "";
		try {
			InputStream	responseStream = get.getResponseBodyAsStream();
			StringBuffer out = new StringBuffer();
		    byte[] b = new byte[4096];
		    for (int n; (n = responseStream.read(b)) != -1;) {
		        out.append(new String(b, 0, n));
		    }
		    responseBody = out.toString();
		    System.out.println("RESULT:\n");
		    System.out.println(responseBody);
		    return responseBody;
		}
		catch(IOException ioe){
			ioe.printStackTrace();
			return null;
		}
	}

	private static void doPUT(HttpClient http, String uri, String content) {
		PutMethod put = new PutMethod();
		try {
			put.setURI(new URI(uri, false, "UTF-8"));
			put.setRequestHeader("OSLC-Core-Version", "2.0");
			put.setRequestHeader("Accept", "application/rdf+xml");
			put.setRequestHeader("Content-Type", "application/rdf+xml");
			if (eTag != null) {
				put.setRequestHeader("If-Match", eTag);
			}
			RequestEntity entity = new StringRequestEntity(content, "application/rdf+xml", "UTF-8");
			((EntityEnclosingMethod)put).setRequestEntity(entity);
			System.out.println("PUT: " + uri + "\n");
			System.out.println(content);
			int status = http.executeMethod(put);
			if (status != HttpStatus.SC_OK) {
				throw new Exception("PUT response status not 200 OK, received " + status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

