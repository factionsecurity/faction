package com.fuse.unittests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fuse.utils.FSUtils;

/**
 * Covers the at-rest encryption helpers in FSUtils: the AES-GCM format written
 * today and the read-only fallback for values written by earlier releases in
 * AES/ECB with a constant salt.
 */
public class CryptoTests {

	private static final String SECRET = "unit-test-secret-key";

	@BeforeClass
	public static void setSecret() {
		if (System.getenv("FACTION_SECRET_KEY") == null) {
			System.setProperty("FACTION_SECRET_KEY", SECRET);
		}
	}

	private static String secret() {
		String env = System.getenv("FACTION_SECRET_KEY");
		return env == null ? SECRET : env;
	}

	@Test
	public void passwordRoundTrip() {
		String encrypted = FSUtils.encryptPassword("p@ssw0rd with spaces and ünïcode");
		assertTrue("new values must use the GCM format", encrypted.startsWith("$AESGCM$"));
		assertEquals("p@ssw0rd with spaces and ünïcode", FSUtils.decryptPassword(encrypted));
	}

	@Test
	public void bytesRoundTrip() {
		byte[] data = new byte[1024];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}
		String encrypted = FSUtils.encryptBytes(data);
		assertTrue(encrypted.startsWith("$AESGCM$"));
		assertArrayEquals(data, FSUtils.decryptBytes(encrypted));
	}

	@Test
	public void sameInputProducesDifferentCiphertext() {
		String first = FSUtils.encryptPassword("same");
		String second = FSUtils.encryptPassword("same");
		assertNotEquals("random salt/IV must make ciphertexts unique", first, second);
		assertEquals("same", FSUtils.decryptPassword(first));
		assertEquals("same", FSUtils.decryptPassword(second));
	}

	@Test
	public void tamperedCiphertextIsRejected() {
		String encrypted = FSUtils.encryptPassword("integrity");
		byte[] raw = Base64.decodeBase64(encrypted.substring("$AESGCM$".length()));
		raw[raw.length - 1] ^= 0x01; // flip a bit in the GCM tag
		String tampered = "$AESGCM$" + Base64.encodeBase64String(raw);
		assertEquals("", FSUtils.decryptPassword(tampered));
	}

	@Test
	public void legacyEcbValuesStillDecrypt() throws Exception {
		// Reproduce exactly what encryptPassword() wrote before the GCM format existed.
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(secret().getBytes());
		char[] b64hash = Base64.encodeBase64String(hash).toCharArray();
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(b64hash, "f04ce910-bedb-4d8f-a023-4d2441dc0fba".getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
		Cipher legacy = Cipher.getInstance("AES/ECB/PKCS5Padding");
		legacy.init(Cipher.ENCRYPT_MODE, key);
		String legacyValue = Base64.encodeBase64String(legacy.doFinal("old-smtp-password".getBytes(StandardCharsets.UTF_8)));

		assertEquals("old-smtp-password", FSUtils.decryptPassword(legacyValue));
	}

	@Test
	public void garbageInputIsHandled() {
		assertEquals("", FSUtils.decryptPassword(null));
		assertEquals("", FSUtils.decryptPassword(""));
		assertEquals("", FSUtils.decryptPassword("$AESGCM$AAAA"));
		assertEquals("", FSUtils.decryptPassword("not-base64-!!"));
	}
}
