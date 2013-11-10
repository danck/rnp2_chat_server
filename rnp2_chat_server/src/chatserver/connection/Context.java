package chatserver.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;

import chatserver.ChatServer;
import chatserver.backend.GuestList;


/**
 * Provides a facade for each ClientHanderTask to interact with their current connection as well as
 * with backend routines.
 * 
 * @author m215025
 *
 */
public class Context {
	private BufferedReader 	reader;
	private BufferedWriter 	writer;
	private GuestList		gl;
	private Socket 			socket;
	
	public Context(Socket socket, GuestList gl) throws IOException
	{
		this.socket = socket;
		this.gl		= gl;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter (socket.getOutputStream()));
		
		ChatServer.infoLogger.log(Level.INFO, "Connected to " + socket.getInetAddress().toString());
	}
	
	/**
	 * Provide the last unread line that the client has sent
	 * 
	 * @return		A newline-terminated string as read from the socket
	 * @throws 		IOException
	 */
	String receiveLine() throws IOException {
		String rec = reader.readLine();
		ChatServer.infoLogger.log(Level.INFO, "[ "
				+ socket.getInetAddress().toString() + " ] " + "Received\t"
				+ rec);
		return rec;
	}
	
	/**
	 * Send a message to the client. Automatically append a 'newline'.
	 * 
	 * @param 	msg	Message to be sent to the client
	 * @throws 		IOException
	 */
	void sendLine(String msg) throws IOException {
		ChatServer.infoLogger.log(Level.INFO, "[ "
				+ socket.getInetAddress().toString() + " ] " +  "Sending\t" + msg);
		writer.write(msg);
		writer.newLine();
		writer.flush();
	}
	
	/**
	 * Add the given nickname with the current IP to the list of guests
	 * 
	 * @param nickname
	 * @return	true	if the current IP has been added for the fist time
	 * 			false	if an old nickname for the current IP has been overwritten
	 */
	boolean login(String nickname) {
		String hostIP 	= socket.getInetAddress().toString();
		gl.addGuest(hostIP, nickname);
		if (gl.addGuest(hostIP, nickname) == null)
			ChatServer.infoLogger.log(Level.INFO, "Added " + hostIP + " as " + nickname);
		else
			ChatServer.infoLogger.log(Level.INFO, "Updated " + hostIP + " as " + nickname);
		return true;
	}
	
	void logout() {
		String hostIP 	= socket.getInetAddress().toString();
		gl.removeGuest(hostIP);
		ChatServer.infoLogger.log(Level.FINE, "Removed " + hostIP);
	}
	
	/**
	 * Provide a string of the current guests in list
	 * 
	 * @return 	A string formatted like:<br/>
	 * 			<code>n Hostname-1 chatname-1 ... Hostname-n chatname-n</code><br/>
	 * 			where <code>n</code> is the current number of entries in the guest list
	 * 			and Hostname and chatname are the IP addresses associated with the nicknames
	 */
	String getParticipants() {
		StringBuilder sb = new StringBuilder();
		Map<String, String> list = gl.getList();
		sb.append(list.size() + " ");
		for (String host : list.keySet()){
			sb.append(host + " ");
			sb.append(list.get(host) + " ");
		}
		
		return sb.toString();
	}
	
	/**
	 * Terminate this connection:<br/>
	 * 
	 * <li>Remove the current client from the guest list (if logged in).</li>
	 * <li>Close all related streams and sockets for a clean exit.</li>
	 */
	void shutdown() {
		try {
			writer.flush();
			writer.close();
			reader.close();
			socket.close();
			ChatServer.infoLogger.log(Level.INFO, "socket closed");
		} catch (IOException e) {
			ChatServer.errorLogger.log(Level.WARNING, e.getMessage(), e);
		} finally {
			gl.removeGuest(socket.getInetAddress().toString());
		}
	}
}