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
import javax.crypto.Mac;
import java.io.ByteArrayOutputStream;
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
		catch (java.util.NoSuchElementException e) { }
		//rather than check for nextElement we just catch when we run out of
		//elements and proceed
	    catch (Exception e) { e.printStackTrace(); }

		writeOutObjects(pwdFile, pepperFile, plaintext);

	}

	private static void writeOutObjects(PasswordFile pwd, PepperFile pep, File f){
        
        try {
            //Make a secret key for ciphering
            KeyGenerator enckeygen = KeyGenerator.getInstance("AES");
            SecretKey sKey = enckeygen.generateKey();

            //make a cipher for encrypting with secret key
    	    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
    		cipher.init(Cipher.ENCRYPT_MODE, sKey);

            //encrypts password file as a sealedObject
    		SealedObject cipherPwd = new SealedObject(pwd, cipher);
    		System.out.println(cipherPwd);
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		ObjectOutputStream cipherOut = new ObjectOutputStream(baos);
            cipherOut.writeObject(cipherPwd);
            cipherOut.flush();
            byte[] byteCipher = baos.toByteArray();
            cipherOut.close();
       		File finalPwdFile = new File(f + "Encrypted");
    		FileOutputStream fos = new FileOutputStream(finalPwdFile);
    
            fos.write(byteCipher);
			fos.flush();
			fos.close();

            // Generate secret key for HMAC-SHA1
			KeyGenerator kg = KeyGenerator.getInstance("HMACSHA1");
	        SecretKey sk = kg.generateKey();

	        // Get instance of Mac object implementing HMAC-MD5, and
	        // initialize it with the above secret key
	        Mac mac = Mac.getInstance("HmacMD5");
	        mac.init(sk);
	        byte[] result = mac.doFinal(byteCipher);
       		
       		File macFile = new File(f + "HMAC");
    		FileOutputStream fos2 = new FileOutputStream(macFile);
    		    
            fos2.write(result);
			fos2.flush();
			fos2.close();

			File finalPepperFile = new File(f + "Pepper" + "Encrypted");
    		fos = new FileOutputStream(finalPepperFile);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);

    		SealedObject cipherPepper = new SealedObject(pep, cipher);		
			oos.writeObject(cipherPepper);
			oos.flush();
			oos.close();
			fos.flush();
			fos.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
}
