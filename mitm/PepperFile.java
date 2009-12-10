package mitm;

import java.util.Hashtable;
import java.io.Serializable;
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
		
}

