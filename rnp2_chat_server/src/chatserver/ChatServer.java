package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

import chatserver.backend.GuestList;
import chatserver.connection.ClientHandlerTask;
import chatserver.connection.Context;



public class ChatServer {

	// The number of worker threads equals the maximum number of concurrent connections.
	// If the maximum is reached new connections will be stalled until new resources are
	// freed (i.e. running connections are being closed)
	public final static int NUMBER_IO_THREADS	= 80;
	
	// Port on which the server listens for clients
	public final static int PORT 			= 50000;
	
	// Loggers for debugging and error logging purposes
	public final static Logger infoLogger 	= Logger.getLogger("info");
	public final static Logger errorLogger 	= Logger.getLogger("errors");

	public static void main(String[] args) {
		initialize();
		GuestList gl = new GuestList();
		
		ExecutorService pool = Executors.newFixedThreadPool(NUMBER_IO_THREADS);

		try {
			ServerSocket ssocket = new ServerSocket(PORT);

			try {
				infoLogger.log(Level.INFO, "Starting Server on Port " + PORT);
				infoLogger.log(Level.INFO, "Number of IO threads: " + NUMBER_IO_THREADS);

				// Main loop for accepting client connections
				while (true) {
					
					// Return a socket for each actual connection
					Socket s = ssocket.accept();
					
					infoLogger.log(Level.INFO,
							"Connecting to " + s.getRemoteSocketAddress());
					
					// Wrap the received socket in a ClientHandlerTask and submit it to the threadpool
					@SuppressWarnings("unchecked")
					Callable<Void> task = new ClientHandlerTask(new Context(s,gl));
					pool.submit(task);
				}
			} catch (IOException e) {
				errorLogger
						.log(Level.SEVERE, "Failed to initialize Connection: "
								+ e.getMessage(), e);
			} catch (RuntimeException e) {
				errorLogger.log(Level.SEVERE,
						"Unexpected error: " + e.getMessage(), e);
			}
		} catch (IOException e) {
			errorLogger.log(Level.SEVERE,
					"Failed to initialize server: " + e.getMessage(), e);
		} catch (RuntimeException e) {
			errorLogger.log(Level.SEVERE,
					"Unexpected error: " + e.getMessage(), e);
		}
	}

	/*
	 * Sets up things we don't know about yet
	 */
	private static void initialize() {
		
	}
}