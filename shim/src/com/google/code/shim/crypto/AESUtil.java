package com.google.code.shim.crypto;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>
 * An encryption utility for Advanced Encryption Standard encoding
 * </p>
 * 
 * <p>
 * This class provides three basic capabilities, all available via static method
 * calls.
 * </p>
 * <ol>
 * <li>The ability to generate an AES 128-bit secret key that is used to
 * encrypt/decrypt data.</li>
 * <li>The ability to encrypt data.</li>
 * <li>The ability to decrypt data.</li>
 * </ol>
 * 
 * <p>
 * You can also access the aforementioned functions from the command line. See
 * the {@link #main(String[])} javadoc for more info.
 * </p>
 * @since 2007-04-28
 * @author dgau
 */
public class AESUtil {

	/**
	 * Turns array of bytes into string
	 * 
	 * @param buf
	 *           Array of bytes to convert to hex string
	 * @return Generated hex string
	 */
	public static String asHexString(byte buf[]) {
		StringBuilder strbuf = new StringBuilder(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}

	/**
	 * Converts a hexadecimal String to a byte array.
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] asByteArray(String hexStr) {
		byte bArray[] = new byte[hexStr.length() / 2];
		for (int i = 0; i < (hexStr.length() / 2); i++) {
			byte firstNibble = Byte.parseByte(hexStr.substring(2 * i, 2 * i + 1), 16); // [x,y)
			byte secondNibble = Byte.parseByte(hexStr.substring(2 * i + 1, 2 * i + 2), 16);
			int finalByte = (secondNibble) | (firstNibble << 4); // bit-operations
																				  // only with
																				  // numbers, not
																				  // bytes.
			bArray[i] = (byte) finalByte;
		}
		return bArray;
	}

	/**
	 * Given an input string and a hexadecimal AES secret key, this method
	 * outputs the encrypted hexadecimal value.
	 * 
	 * @param whatToEncrypt
	 * @param aesHexKey
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String whatToEncrypt, String aesHexKey) throws Exception {

		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec skeySpec = new SecretKeySpec(asByteArray(aesHexKey), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encryptedBytes = cipher.doFinal(whatToEncrypt.getBytes());
		return asHexString(encryptedBytes);

	}

	/**
	 * Given an input encrypted string (in hexadecimal format) and a hexadecimal
	 * AES secret key, this method outputs a decrypted string value.
	 * 
	 * @param whatToDecrypt
	 * @param aesHexKey
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String whatToDecrypt, String aesHexKey) throws Exception {

		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec skeySpec = new SecretKeySpec(asByteArray(aesHexKey), "AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decryptedBytes = cipher.doFinal(asByteArray(whatToDecrypt));
		return new String(decryptedBytes);

	}

	/**
	 * Creates an AES 128-bit secret key as a hexadecimal string.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getAesHexKey() throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128); // Higher than 128-bit encryption requires a download of additional provider implementations for the JDK.

		// Generate the secret key specs.
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();

		return asHexString(raw);
	}

	/**
	 * Command line access to the AES key generation and encryption.
	 * 
	 * @param args
	 *           <ol>
	 *           <li>-aesHexKey=[hexadecimal secret AES key to use] if left
	 *           blank, a key will be created and written to System.out</li>
	 *           <li>-encrypt=[string value to encrypt] if left blank, the method
	 *           will check to see if it should decrypt something instead.</li>
	 *           <li>-decrypt=[hexadecimal string value to decrypt] if left
	 *           blank, nothing further occurs.</li>
	 *           </ol>
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String aesHexKey = null;
		String encryptSrc = null;
		String decryptSrc = null;
		for (String arg : args) {
			System.out.println(arg);

			if (arg.startsWith("-aesHexKey")) {
				aesHexKey = arg.substring(arg.indexOf("=") + 1);
			}
			if (arg.startsWith("-encrypt")) {
				encryptSrc = arg.substring(arg.indexOf("=") + 1);
			}
			if (arg.startsWith("-decrypt")) {
				decryptSrc = arg.substring(arg.indexOf("=") + 1);
			}
		}

		if (aesHexKey == null || aesHexKey.trim().length() == 0) {
			System.out.println("Key: " + AESUtil.getAesHexKey());
			System.exit(0);
		}

		if (encryptSrc != null && encryptSrc.trim().length() > 0) {
			System.out.println("Encrypted: " + AESUtil.encrypt(encryptSrc, aesHexKey));
		}

		if (decryptSrc != null && decryptSrc.trim().length() > 0) {
			System.out.println("Decrypted: " + AESUtil.decrypt(decryptSrc, aesHexKey));
		}
	}
}