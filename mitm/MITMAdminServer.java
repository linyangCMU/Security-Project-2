/**
 * CSE 490K Project 2
 */

package mitm;

import java.net.*;
import javax.net.ssl.SSLServerSocket;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.security.KeyStore;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.Mac;
import java.io.ByteArrayInputStream;

// You need to add code to do the following
// 1) use SSL sockets instead of the plain sockets provided
// 2) check user authentication
// 3) perform the given administration command

class MITMAdminServer implements Runnable
{
	private ServerSocket m_serverSocket;
	private Socket m_socket = null;
	private HTTPSProxyEngine m_engine;
	public static final int MAC_LENGTH = 20;

	public MITMAdminServer( String localHost, int adminPort, HTTPSProxyEngine engine ) throws IOException {
		try {
			MITMSSLSocketFactory socketFactory =
				new MITMSSLSocketFactory();
			m_serverSocket = socketFactory.createServerSocket( localHost, adminPort, 0 );
			m_engine = engine;
		} catch(Exception e) { e.printStackTrace(); }
	}

	public void run() {
		System.out.println("Admin server initialized, listening on port " + m_serverSocket.getLocalPort());
		while( true ) {
			try {
				m_socket = m_serverSocket.accept();

				byte[] buffer = new byte[40960];

				Pattern userPwdPattern =
					Pattern.compile("username:(\\S+)\\s+password:(\\S+)\\s+command:(\\S+)\\sCN:(\\S*)\\s");

				BufferedInputStream in =
					new BufferedInputStream(m_socket.getInputStream(),
											buffer.length);

				// Read a buffer full.
				int bytesRead = in.read(buffer);

				String line =
					bytesRead > 0 ?
					new String(buffer, 0, bytesRead) : "";

				Matcher userPwdMatcher =
					userPwdPattern.matcher(line);

				// parse username and pwd
				if (userPwdMatcher.find()) {
					String userName = userPwdMatcher.group(1);
					String password = userPwdMatcher.group(2);

					if( authenticateUser(userName, password) ) {
						String command = userPwdMatcher.group(3);
						String commonName = userPwdMatcher.group(4);
						doCommand( command );
					} else {
						System.out.println("User " + userName + " unauthenticated.");
					}
				}
			}
			catch( InterruptedIOException e ) {
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	private boolean authenticateUser(String u, String p) {
		// MAC-then-decrypt pwd file
		File pwd_file;
		FileInputStream inputStream;
		KeyStore ks;

		try {

			// now we have a keystore.

			pwd_file = new File(JSSEConstants.PWD_FILE_LOCATION + "Encrypted");
			inputStream = new FileInputStream(pwd_file);
			byte[] pwdFileByteArray = new byte[(int) pwd_file.length() - MAC_LENGTH];

			inputStream.read(pwdFileByteArray);
			byte[] mac = new byte[MAC_LENGTH];
			inputStream.read(mac);
			String mac_string = "";
			for (int i=0; i < mac.length; i++) {
				mac_string +=
					Integer.toString( ( mac[i] & 0xff ) + 0x100, 16).substring( 1 );
			}

			ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(JSSEConstants.PWD_KEYSTORE_LOCATION), "bowdoincs".toCharArray());

			// Make sure that the MAC is valid for the encrypted passwords file.
			if (authenticateFile(pwdFileByteArray, mac, ks)) {
				//remove to method 'decryptpasswordfile'
				SecretKey cipherKey =
					(SecretKey) ks.getKey("cipher_key", "bowdoincs_cipher".toCharArray());

				ObjectInputStream objectStream =
					new ObjectInputStream(new ByteArrayInputStream(pwdFileByteArray));
				SealedObject encryptedPasswordFile = (SealedObject) objectStream.readObject();
				PasswordFile passwordFile = (PasswordFile) encryptedPasswordFile.getObject(cipherKey);
				// and one for the pepper
				SecretKey pepperKey =
					(SecretKey) ks.getKey("pepper_key", "bowdoincs_pepper".toCharArray());
				objectStream.close();
				objectStream = new ObjectInputStream(new FileInputStream(
																		 JSSEConstants.PWD_FILE_LOCATION + "PepperEncrypted"));
				SealedObject encryptedPepperFile = (SealedObject) objectStream.readObject();
				PepperFile pepperFile = (PepperFile) encryptedPepperFile.getObject(pepperKey);

				// Finally authenticate user against pwdFile

				String pepper = pepperFile.get(u);
				return passwordFile.checkUser(u, p, pepper);
			} else {
				System.out.println("PWD FILE HAS BEEN TAMPERED WITH");
				System.exit(1);
			}
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("Could not load password file; exiting.");
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean authenticateFile(byte[] pwdFile, byte[] retrieved_mac, KeyStore ks) {
		SecretKey mac_key;
		try {
			mac_key = (SecretKey) ks.getKey("mac_key", "bowdoincs_mac".toCharArray());
			Mac mac = Mac.getInstance("HMACSHA1");
			mac.init(mac_key);
			byte[] calculated_mac = mac.doFinal(pwdFile);
			return Arrays.equals(retrieved_mac, calculated_mac);
		} catch (Exception e) {
			System.out.println("Unable to load MAC key, quitting.");
			e.printStackTrace();
			System.exit(1);
		}

		return false;
	}

	// TODO implement the commands
	private void doCommand( String cmd ) throws IOException {
		String c = cmd.toLowerCase();
		if ( c.equals("stats") ){
			Scanner s = new Scanner(new FileInputStream(JSSEConstants.STATS_FILE_LOCATION));
			int connections = s.nextInt();
			try{
				m_socket.getOutputStream().write(("number of connections: " + connections).getBytes());
			} catch (Exception e) { e.printStackTrace(); }
		}
		else if ( c.equals("exit") ){
			System.exit(1);
		}
		m_socket.close();
	}

}
