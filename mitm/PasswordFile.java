package mitm;

import java.util.Hashtable;
import java.util.Scanner;
import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;

/**
   Password file implemented in Java. Intended to be stored on HD between sessions.
*/

public class PasswordFile implements Serializable{
	Hashtable<String, PwdEntry> passwords;
	// constructor takes a plaintext file, parses it for usernames and passwords
	// and creates
	protected PasswordFile(File plaintext ) {

		passwords = new Hashtable(3);

		try {
			//uses whitespace as delimiter by default
			Scanner scan = new Scanner(plaintext);
			while (scan.hasNextLine()){
				String u = scan.next();
				String p = scan.next();
				System.out.println(u + p);
				passwords.put(u, new PwdEntry(u, p));
			}
		}
		catch (java.io.FileNotFoundException e) {
			System.out.println("File not found.\n Please check the path and try again");
			e.printStackTrace();
		}
		//rather than check for nextElement we just catch when we run out of
		//elements and proceed
		catch (java.util.NoSuchElementException e) { }

	}

	private class PwdEntry implements Serializable{
		String user;
		String pwd;
		byte[] salt;
		private PwdEntry(String u, String pass) {
			user = u;
			pwd = pass;
			try {
				//replace with proper salt
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update((u + pass).getBytes());
				salt = md.digest();
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
}

