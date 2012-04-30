package com.google.code.shim.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Utility for working with cryptographic hashing.
 * 
 * @since 2007-04-28
 * @author dgau
 * 
 */
public class HashUtil {

	static Logger logger = LogManager.getLogger(HashUtil.class);

	

	/**
	 * Produces an MD5 hash of the specified text using Base64 encoding.
	 * @param data
	 * @return
	 */
	public static String hashBase64MD5(String data) {
		try{
			return new String(Base64.encodeBase64(produceDigest("MD5",data)));
		} catch(  NoSuchAlgorithmException e){
			logger.error("MD5 hash algorithm unavailable. "+e.getMessage());
			return null;
		}
	}
	
	public static String hashHexMD5(String data) {
		try{
			
			return new String(Hex.encodeHex( produceDigest("MD5",data)));
		} catch(  NoSuchAlgorithmException e){
			logger.error("MD5 hash algorithm unavailable. "+e.getMessage());
			return null;
		}
	}
	

	/**
	 * Produces an SHA-256 hash of the specified text using Base64 encoding.
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static String hashBase64SHA256(String text) {
		try{
			return new String(Base64.encodeBase64(produceDigest("SHA-256", text)));
		} catch(  NoSuchAlgorithmException e){
			logger.error("SHA-256 hash algorithm unavailable. "+e.getMessage());
			return null;
		}
	}

	/**
	 * Produces a byte-array (digest) of the specified text string for the given algorithm
	 * @param algorithm
	 * @param text
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] produceDigest(String algorithm, String text) throws NoSuchAlgorithmException	{
		if (text == null)
			return null;

		MessageDigest messageDigest = null;
		messageDigest = MessageDigest.getInstance(algorithm);
		messageDigest.update(text.getBytes());
		return (messageDigest.digest());
	}
	
	

	/**
	 * Returns the byte representation of the contents for any file. This is useful for making MD5 digests, etc.
	 */
	public static byte[] getBytesForFile(File file) throws Exception {
		ByteArrayOutputStream bytesIn = new ByteArrayOutputStream();
		InputStream stream = new FileInputStream(file);

		byte bufferToRead[] = new byte[1024];
		int length = 0;

		while ((length = stream.read(bufferToRead, 0, 1024)) != -1) {
			bytesIn.write(bufferToRead, 0, length);
		}

		return bytesIn.toByteArray();
	}

}
