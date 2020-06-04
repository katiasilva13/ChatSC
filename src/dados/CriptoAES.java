package dados;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Dani
 * @author ktia-
 */
public class CriptoAES {

	static Logger logger = Logger.getLogger(CriptoAES.class.getName());

	private static SecretKeySpec secretKey;
	private static byte[] key;

	static String IV = "AAAAAAAAAAAAAAAA";
	static String textopuro = "teste texto 12345678\0\0\0";
	static String chaveencriptacao = "0123456789abcdef";

	public static byte[] encryptAQV(String textopuro, String chaveencriptacao) throws Exception {
		try {
			Cipher encripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
			SecretKeySpec key = new SecretKeySpec(chaveencriptacao.getBytes(StandardCharsets.UTF_8), "AES");
			encripta.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
			return encripta.doFinal(textopuro.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Erro ao encriptar arquivo. ", e);
		}
		return null;
	}

	public static String decryptAQV(byte[] textoencriptado, String chaveencriptacao) throws Exception {
		try {
			Cipher decripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
			SecretKeySpec key = new SecretKeySpec(chaveencriptacao.getBytes(StandardCharsets.UTF_8), "AES");
			decripta.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
			return new String(decripta.doFinal(textoencriptado), StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Erro ao decriptar arquivo. ", e);
		}
		return null;
	}

	public static void setKey(String myKey) {
		MessageDigest sha = null;
		try {
			key = myKey.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

	public static String encrypt(String toEncrypt, String key) {
		try {
			setKey(key);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Erro ao encriptar. ", e);
		}
		return null;
	}

	public static String decrypt(String strToDecrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Erro ao decriptar. ", e);
		}
		return null;
	}
}