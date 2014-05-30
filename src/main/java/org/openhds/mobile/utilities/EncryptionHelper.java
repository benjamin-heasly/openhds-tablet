package org.openhds.mobile.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

public class EncryptionHelper {

	private static final String ENCRYPTION_PASSWORD_KEY = "database-password";
	private static final String ENCRYPTION_SHARED_PREF = "openhds-provider";

	private String stringKey;
	private SecretKey key;

	private Cipher aesCipher;
	private FileInputStream fileInputStream;
	FileOutputStream fileOuputStream;
	CipherOutputStream cipherOutputStream;


	public EncryptionHelper(Context context) throws NoSuchAlgorithmException {

		SharedPreferences sp = context.getSharedPreferences(
				ENCRYPTION_SHARED_PREF, Context.MODE_PRIVATE);
		stringKey = sp.getString(ENCRYPTION_PASSWORD_KEY, "");

		if (stringKey.isEmpty()) {
			KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
			key = keygenerator.generateKey();
			stringKey = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
			Editor editor = sp.edit();
			editor.putString(ENCRYPTION_PASSWORD_KEY, stringKey);
			editor.commit();
		} else {
			byte[] encodedKey = Base64.decode(stringKey, Base64.DEFAULT);
			key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		}

	}

	public void encryptFile(File file) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {

		aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		aesCipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] bFile = new byte[(int) file.length()];

		fileInputStream = new FileInputStream(file);
		fileInputStream.read(bFile);
		fileInputStream.close();

		fileOuputStream = new FileOutputStream(file);

		cipherOutputStream = new CipherOutputStream(fileOuputStream, aesCipher);
		cipherOutputStream.write(bFile);
		cipherOutputStream.close();

	}

	public void decryptFile(File file) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchPaddingException, IOException {

		aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		aesCipher.init(Cipher.DECRYPT_MODE, key);

		byte[] bFile = new byte[(int) file.length()];

		fileInputStream = new FileInputStream(file);
		fileInputStream.read(bFile);
		fileInputStream.close();

		fileOuputStream = new FileOutputStream(file);

		cipherOutputStream = new CipherOutputStream(fileOuputStream, aesCipher);
		cipherOutputStream.write(bFile);
		cipherOutputStream.close();
	}
}
