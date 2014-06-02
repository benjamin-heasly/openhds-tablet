package org.openhds.mobile.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class EncryptionHelper {

	private static final String ENCRYPTION_PASSWORD_KEY = "database-password";
	private static final String ENCRYPTION_SHARED_PREF = "openhds-provider";

	private static final String ENCRYPTED_FLAG = ".encrypted";

	private static Cipher aesCipher;

	/*
	 * EncryptionHelper Generates a 128 bit AES key and stores it in Android's
	 * SharedPreferences. This Key is unique to each installed instance of the
	 * application.
	 */

	public EncryptionHelper() {

	}

	private static Key getOrGenerateKey(Context context)
			throws NoSuchAlgorithmException {

		String stringKey;
		SecretKey key;
		SharedPreferences sp;

		sp = context.getSharedPreferences(ENCRYPTION_SHARED_PREF,
				Context.MODE_PRIVATE);

		stringKey = sp.getString(ENCRYPTION_PASSWORD_KEY, "");

		if (stringKey.isEmpty()) {
			key = KeyGenerator.getInstance("AES").generateKey();
			stringKey = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);

			sp.edit().putString(ENCRYPTION_PASSWORD_KEY, stringKey).commit();
		} else {
			byte[] encodedKey = Base64.decode(stringKey, Base64.DEFAULT);
			key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		}

		return key;
	}

	public static void encryptFile(File file, Context context) {
		if (null != file && !isEncrypted(file)) {
			startCipher(Cipher.ENCRYPT_MODE, context, file);
			flagAsEncrypted(file);
		}

	}

	public static void decryptFile(File file, Context context) {

		if (null != file && isEncrypted(file)) {
			flagAsDecrypted(file);
			startCipher(Cipher.DECRYPT_MODE, context, file);
		}
	}

	private static void readAndWriteFile(File file, Cipher cipher)
			throws IOException {

		byte[] bFile = new byte[(int) file.length()];

		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.read(bFile);
		fileInputStream.close();

		CipherOutputStream cipherOutputStream = new CipherOutputStream(
				new FileOutputStream(file), aesCipher);
		cipherOutputStream.write(bFile);
		cipherOutputStream.close();
	}

	private static void startCipher(int cipherMode, Context context, File file) {

		try {
			aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aesCipher.init(cipherMode, getOrGenerateKey(context));
			readAndWriteFile(file, aesCipher);

		} catch (InvalidKeyException e) {
			MessageUtils.showLongToast(context, "InvalidKeyException: " + e);
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			MessageUtils.showLongToast(context, "NoSuchAlgorithmException: "
					+ e);
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			MessageUtils.showLongToast(context, "NoSuchPaddingException: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			MessageUtils.showLongToast(context, "IOException: " + e);
			e.printStackTrace();
		}

	}

	private static boolean isEncrypted(File file) {

		if (new File(file.getAbsolutePath().concat(ENCRYPTED_FLAG)).exists()) {
			return true;
		} else if (file.exists()) {
			return false;
		}
		return false;

	}

	private static void flagAsEncrypted(File file) {
		String newFilepath = file.getAbsolutePath().concat(ENCRYPTED_FLAG);
	}

	// This works because the file handed in is from ODKCollect's data provider
	// and NOT the actual file path that we're renaming. Meaning we're handed
	// the normal filename, not the one
	// flagged as encrypted so all we have to do is set the path back to the one
	// stored in the ODKCollect provider.
	private static void flagAsDecrypted(File file) {
		File oldFile = new File(file.getAbsolutePath().concat(ENCRYPTED_FLAG));
	}
}
