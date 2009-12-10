package mitm;

import java.util.Hashtable;
import java.io.Serializable;
import java.security.MessageDigest;
/**
   Password file implemented in Java. Intended to be stored on HD between sessions.
*/

public class PasswordFile implements Serializable{
	Hashtable<String, PwdEntry> passwords;
	// constructor takes a plaintext file, parses it for usernames and passwords
	// and creates
	protected PasswordFile() {

		passwords = new Hashtable(3);

	}
	
	protected void addEntry(String u, String p, String salt, String pepper) {
	    byte[] pwd;
	    try {
    	    MessageDigest md = MessageDigest.getInstance("MD5");
    	    md.update((salt + pepper + p).getBytes());
            pwd = md.digest();
            passwords.put(u, new PwdEntry(salt, pwd));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	private class PwdEntry implements Serializable{
		String salt;
		byte[] pwd;

		private PwdEntry(String s, byte[] p) {
			salt = s;
			pwd = p;
		}
	}
		
}

