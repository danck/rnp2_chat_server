package chatserver.connection;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import chatserver.ChatServer;

/**
 * Implements the protocol to interact with a particular client
 */
@SuppressWarnings("rawtypes") // Safe because no return value ("future") is expected
public class ClientHandlerTask implements Callable {
	
	private enum ClientCommand {
		NEW, INFO, BYE;
	}
	
	private enum ServerCommand {
		OK, LIST, ERROR, BYE;
	}
	
	private Context ctx;
	
	public ClientHandlerTask(Context c) 
	{
		this.ctx = c;
	}
	
	/**
	 * Entrance point for handling the client.
	 */
	@Override
	public Object call() throws Exception 
	{		
		boolean quit = false;
		
		while (quit == false) {
			try {
				String input 	= ctx.receiveLine();
				Scanner scan = new Scanner(input);
				String bareCommand = scan.next();
				
				// BYE
				if (bareCommand.equals(ClientCommand.BYE.toString())) {
					logout();
					replyWith(ServerCommand.BYE.toString());
					quit = true;
					ctx.shutdown();
				} 
				
				// INFO
				else if (bareCommand.equals(ClientCommand.INFO.toString())) {
					String list = ctx.getParticipants();
					replyWith(ServerCommand.LIST.toString() + " " + list);
				}
				
				// NEW
				else if (bareCommand.equals(ClientCommand.NEW.toString())) {
					if (scan.hasNext()) {
						String name = scan.next();
						if (name.length() <= ChatServer.NAME_MAX_LENGTH) {
							if (checkSyntax(name)) {
								ctx.login(name);
								replyWith(ServerCommand.OK.toString());
							} else
							{
								error("Whitespace and special characters are prohibited");
							}
						} else
							error("Name too long - maximum allowed: " + ChatServer.NAME_MAX_LENGTH);
					}
					else
						error("No username given");
				} 
				
				else {
					error("unknown command " + bareCommand);
					quit = true;
				}
				scan.close();
			} catch (IOException e) {
				ChatServer.errorLogger.log(Level.WARNING, e.getMessage(), e);
				quit = true;
				logout();
				ctx.shutdown();
			}
		}
		return null;
	}
	
	private boolean login(String nickname) {
		return ctx.login(nickname);
	}
	
	private boolean checkSyntax(String name) {
		for (char c : name.toCharArray())
		{
			if (! Character.isLetterOrDigit(c))
				return false;
		}
		return true;
	}
	/**
	 * Remove the current client from the guest list and state success.
	 */
	private void logout() {
		ChatServer.infoLogger.log(Level.INFO, "Logging out...");
		ctx.logout();
	}
	
	/**
	 * Send a message to the connected client
	 * 
	 * @param msg	Message to client
	 */
	private void replyWith(String msg) {
		try {
			ctx.sendLine(msg);
		} catch (IOException e) {
			ChatServer.errorLogger.log(Level.WARNING, e.getMessage(), e);
			logout();
			ctx.shutdown();
		}
	}
	
	/**
	 * Report a protocol error. Log out client and terminate connection immediately.
	 * 
	 * @param msg	Description of what caused the error
	 */
	private void error(String msg){
		try {
			ctx.sendLine("ERROR " + msg);
		} catch (IOException e) {
			ChatServer.errorLogger.log(Level.WARNING, e.getMessage(), e);
		}
		logout();
		ctx.shutdown();
	}
}