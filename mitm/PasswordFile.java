package mitm;

import java.util.Hashtable;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;
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
	
	public boolean checkUser(String u, String p, String pepper) {
	    try {
	        PwdEntry user = passwords.get(u);
	        String salt = user.salt;
	        byte[] hashed_pwd = user.pwd;
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        md.update((salt + pepper + p).getBytes());
	        byte[] calculated_pwd = md.digest();
	        System.out.println("calculated_pwd: " + byteArrayToString(calculated_pwd));
	        System.out.println("hashed_pwd: " + byteArrayToString(hashed_pwd));
	        return Arrays.equals(hashed_pwd, calculated_pwd);
        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("THIS IS BAD PRACTICE");
        return false;
	}

	private class PwdEntry implements Serializable{
		String salt;
		byte[] pwd;

		private PwdEntry(String s, byte[] p) {
			salt = s;
			pwd = p;
		}
		
	}
	
	public String byteArrayToString(byte[] in) {
	    String out_string = "";
        for (int i=0; i < in.length; i++) {
          out_string +=
                Integer.toString( ( in[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
   		return out_string;
	}
	
	public String toString() {
	    String output = "";
	    String user;
	    for (Enumeration<String> e = passwords.keys(); e.hasMoreElements();) {
	        try {
	            user = e.nextElement();
	            PwdEntry p = passwords.get(user);
    	        output += "Users " + user + " has salt " + p.salt + 
    	            " and password array at " + p.pwd + "\n";
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }
	    return output;
	}
}

