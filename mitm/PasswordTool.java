package mitm;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import javax.crypto.SealedObject;
import javax.crypto.NullCipher;

/**
 * javax.crypto.SealedObject could simplfiy our encryption
 */
public final class PasswordTool {

	public static void main(String[] args)
		throws java.io.IOException, javax.crypto.IllegalBlockSizeException {
		String fileString = args[0];
		String currentDirectory = System.getProperty("user.dir");
		String curFile = currentDirectory + "/" + fileString;

		File f = new File(curFile);
		PasswordFile pwdFile = createPasswordFile(f);

		//temporary cipher to allow other development
		// TODO: replace with correct cipher
		NullCipher n = new NullCipher();
		SealedObject sealedPwdFile = new SealedObject(pwdFile, n);
			// Generate secret key for HMAC-MD5
			//         KeyGenerator kg = KeyGenerator.getInstance("HmacMD5");
			//         SecretKey sk = kg.generateKey();

			//         // Get instance of Mac object implementing HMAC-MD5, and
			//         // initialize it with the above secret key
			//         Mac mac = Mac.getInstance("HmacMD5");
			//         mac.init(sk);
			//         byte[] result = mac.doFinal("Hi There".getBytes());

		File finalFile = new File(curFile + "Encrypted");
		FileOutputStream fos = new FileOutputStream(finalFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(sealedPwdFile);
		oos.close();
		fos.close();
	}

	public static final PasswordFile createPasswordFile(File plaintext) {

		final PasswordFile encrypted = new PasswordFile(plaintext);

		return encrypted;
	}
}
