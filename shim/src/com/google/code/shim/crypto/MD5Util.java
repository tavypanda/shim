package com.google.code.shim.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Utility for working with MD5 hashing.
 * 
 * @since 2007-04-28
 * @author dgau
 * 
 */
public class MD5Util {

	static Logger logger = LogManager.getLogger(MD5Util.class);

	/**
	 * Gives an MD5 digest for the given data.
	 * 
	 * @param data
	 *            the data to use for generating the digest.
	 * @return an MD5 message digest as a byte array
	 */
	public static byte[] md5Digest(String data) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(data.getBytes());
			byte[] hash = digest.digest();
			return hash;
		} catch (NoSuchAlgorithmException e) {
			logger.error("MD5 algorithm not available: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Gives a Base64 representation of an MD5 digest.
	 * 
	 * @param data
	 *            raw data to use for the digest.
	 * @return a Base64 string representation of the digest.
	 */
	public static String md5ToString(String data) {
		return new String(Base64.encodeBase64(md5Digest(data)));

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

	/**
	 * Returns the 32 character MD5 hash from a String
	 * 
	 * @param string
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String stringToMD5(String string) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.update(string.getBytes(Charset.forName("UTF-8")), 0, string.length());
		return new BigInteger(1, messageDigest.digest()).toString(16);
	}

	/**
	 * Returns the 32 character MD5 hash for a file
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static String fileToMD5(File file) throws Exception {
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		InputStream input = new FileInputStream(file);
		byte[] buf = new byte[1028];
		int len = 0;
		while ((len = input.read(buf)) > 0) {
			messageDigest.update(buf, 0, len);
		}
		return new BigInteger(1, messageDigest.digest()).toString(16);
	}
}
