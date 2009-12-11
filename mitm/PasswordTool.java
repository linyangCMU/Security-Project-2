package mitm;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import javax.crypto.SealedObject;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.Cipher;
import java.util.Scanner;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import java.security.KeyStore;
import javax.crypto.SecretKey;
import javax.crypto.Mac;
import java.io.ByteArrayOutputStream;
import java.security.cert.Certificate;
import java.security.KeyStore.PasswordProtection;

/**
   javax.crypto.SealedObject could simplfiy our encryption
*/
public final class PasswordTool {

	public static void main(String[] args) {
		String fileString = args[0];
		String currentDirectory = System.getProperty("user.dir");
		String curFile = currentDirectory + "/" + fileString;
		File f = new File(curFile);
		createPasswordFiles(f);
	}

	private static String randomString(int len) {
		SecureRandom r = new SecureRandom();
		String s = "";
		for (int i = 0; i < len; i++) {
			s += (char) (r.nextInt(74) + 48);
		}
		return s;
	}

	public static final void createPasswordFiles(File plaintext) {

		PasswordFile pwdFile = new PasswordFile();
		PepperFile pepperFile = new PepperFile();

		try {
			//uses whitespace as delimiter by default
			Scanner scan = new Scanner(plaintext);
			while (scan.hasNextLine()){
				String u = scan.next();
				String p = scan.next();

				String pepper = randomString(16);
				pepperFile.addEntry(u, pepper);

				String salt = randomString(16);
				pwdFile.addEntry(u, p, salt, pepper);

			}
		}
		catch (java.io.FileNotFoundException e) {
			System.out.println("File not found.\n Please check the path and try again");
			e.printStackTrace();
		}
		catch (java.util.NoSuchElementException e) { }
		//rather than check for nextElement we just catch when we run out of
		//elements and proceed
		catch (Exception e) { e.printStackTrace(); }

		writeOutObjects(pwdFile, pepperFile, plaintext);

	}

	private static void writeOutObjects(PasswordFile pwd, PepperFile pep, File f){

		try {
			KeyStore keystore = KeyStore.getInstance("JCEKS");
			keystore.load(null, "bowdoincs".toCharArray());
			// Make a secret key for ciphering the password/salt file
			KeyGenerator enckeygen = KeyGenerator.getInstance("AES");
			SecretKey cipherKey = enckeygen.generateKey();
			keystore.setEntry("cipher_key", new KeyStore.SecretKeyEntry(cipherKey),
							  new KeyStore.PasswordProtection("bowdoincs_cipher".toCharArray()));
			// Make a separate secret key for ciphering the pepper file
			SecretKey pepperKey = enckeygen.generateKey();
			keystore.setEntry("pepper_key", new KeyStore.SecretKeyEntry(pepperKey),
							  new KeyStore.PasswordProtection("bowdoincs_pepper".toCharArray()));

			// make a cipher for encrypting with secret key
			Cipher salt_cipher = Cipher.getInstance("AES/CTR/NoPadding");
			salt_cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
			// and one for the pepper
			Cipher pepper_cipher = Cipher.getInstance("AES/CTR/NoPadding");
			pepper_cipher.init(Cipher.ENCRYPT_MODE, pepperKey);

			//encrypts password file as a sealedObject
			SealedObject cipherPwd = new SealedObject(pwd, salt_cipher);
			// In order to MAC it we need the bytestream, so
			// we print it to the byte array byteCipher.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream cipherOut = new ObjectOutputStream(baos);
			cipherOut.writeObject(cipherPwd);
			byte[] byteCipher = baos.toByteArray();
			cipherOut.close();
			// Now we create the actual file on the disk
			File finalPwdFile = new File(f + "Encrypted");
			FileOutputStream fos = new FileOutputStream(finalPwdFile);

			fos.write(byteCipher);
			fos.flush();

			// Generate secret key for HMAC-SHA1
			KeyGenerator kg = KeyGenerator.getInstance("HMACSHA1");
			SecretKey mac_key = kg.generateKey();
			keystore.setEntry("mac_key", new KeyStore.SecretKeyEntry(mac_key),
							  new KeyStore.PasswordProtection("bowdoincs_mac".toCharArray()));
			keystore.store(new FileOutputStream(JSSEConstants.PWD_KEYSTORE_LOCATION),
						   "bowdoincs".toCharArray());

			// Get instance of Mac object implementing HMAC-MD5, and
			// initialize it with the above secret key
			Mac mac = Mac.getInstance("HMACSHA1");
			mac.init(mac_key);
			byte[] mac_code = mac.doFinal(byteCipher);
			String mac_string = "";
			for (int i=0; i < mac_code.length; i++) {
				mac_string +=
					Integer.toString( ( mac_code[i] & 0xff ) + 0x100, 16).substring( 1 );
			}

			fos.write(mac_code);
			fos.close();

			File finalPepperFile = new File(f + "Pepper" + "Encrypted");
			fos = new FileOutputStream(finalPepperFile);
			ObjectOutputStream pepper_oos = new ObjectOutputStream(fos);

			SealedObject cipherPepper = new SealedObject(pep, pepper_cipher);
			pepper_oos.writeObject(cipherPepper);
			pepper_oos.close();
			fos.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
}
