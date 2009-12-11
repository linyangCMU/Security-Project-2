package mitm;

import java.util.Hashtable;
import java.io.Serializable;
import java.util.Enumeration;
/**
   Password file implemented in Java. Intended to be stored on HD between sessions.
*/

public class PepperFile implements Serializable{
	Hashtable<String, String> peppers;
	// constructor takes a plaintext file, parses it for usernames and passwords
	// and creates
	protected PepperFile() {

		peppers = new Hashtable(3);

	}

	protected void addEntry(String u, String pepper) {
		peppers.put(u, pepper);
	}

	public String get(String u) {
		try {
			return peppers.get(u);
		} catch (Exception e) {
			return "";
		}
	}

	public String toString() {
		String output = "";
		String user;
		try {
			for (Enumeration<String> e = peppers.keys(); e.hasMoreElements();) {
				user = e.nextElement();
				output += "User " + user + " has pepper " + peppers.get(user) + "\n";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return output;
	}

}
