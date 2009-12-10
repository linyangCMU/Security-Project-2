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
import javax.crypto.SecretKey;
/**
 * javax.crypto.SealedObject could simplfiy our encryption
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
				System.out.println(u + p);
				
				String pepper = randomString(16);
				pepperFile.addEntry(u, pepper);
				
				String salt = randomString(16);
				pwdFile.addEntry(u, p, salt, pepper);
            	
            	System.out.println("Salt: " + salt + ", pepper: " + pepper);
			}
		}
		catch (java.io.FileNotFoundException e) {
			System.out.println("File not found.\n Please check the path and try again");
			e.printStackTrace();
		}
	    catch (Exception e) { e.printStackTrace(); }
		//rather than check for nextElement we just catch when we run out of
		//elements and proceed
		writeOutObjects(pwdFile, pepperFile, plaintext);

	}

	private static void writeOutObjects(PasswordFile pwd, PepperFile pep, File f){
	    //temporary cipher to allow other development
		// TODO: replace with correct cipher
		// Create and initialize the cipher
		// Generate a random encryption key
        //SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
        //KeyGenerator enckeygen = KeyGenerator.getInstance("AES");
        //enckeygen.init(prng);
        //SecretKey enckey = enckeygen.generateKey();
        
        try {

            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            KeyGenerator enckeygen = KeyGenerator.getInstance("AES");
            SecretKey sKey = enckeygen.generateKey();
    	    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

    		cipher.init(Cipher.ENCRYPT_MODE, sKey);

			//SealedObject sealedPwdFile = new SealedObject(pwdFile, n);
			// Generate secret key for HMAC-MD5
			//         KeyGenerator kg = KeyGenerator.getInstance("HmacMD5");
			//         SecretKey sk = kg.generateKey();

			//         // Get instance of Mac object implementing HMAC-MD5, and
			//         // initialize it with the above secret key
			//         Mac mac = Mac.getInstance("HmacMD5");
			//         mac.init(sk);
			//         byte[] result = mac.doFinal("Hi There".getBytes());

			File finalFile = new File(f + "Encrypted");
			FileOutputStream fos = new FileOutputStream(finalFile);
			CipherOutputStream cos = new CipherOutputStream(fos, cipher);
			ObjectOutputStream oos = new ObjectOutputStream(cos);

			oos.writeObject(pwd);
			oos.writeObject(pep);
			oos.close();
			fos.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
}
