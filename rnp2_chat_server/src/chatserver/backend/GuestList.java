package chatserver.backend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import chatserver.ChatServer;

/**
 * Database of the current chat participants<br/>
 * 
 * Provides thread-safe API to conduct user related transactions
 * @author m215025
 *
 */
public class GuestList {
	
	/**
	 * ReentrantLock with fair scheduling
	 */
	private Lock lock = new ReentrantLock(true);
	
	/**
	 * Holds nickname and host name of the chatty participants
	 */
	private Map<String, String> guestlist 	= new HashMap<String, String>();
	
	/**
	 * Unmodifiable view of the most recent guest list.
	 */
	private Map<String, String> cachedList 	= Collections.unmodifiableMap(guestlist);

	/**
	 * Add a guest to the chatroom
	 * 
	 * @param hostname	TODO
	 * @param nickname	Custom name to show inside a chatroom. 
	 * 					Must not contain white space or special characters	
	 */
	public String addGuest(String hostname, String nickname){
		lock.lock();
		String rv = guestlist.put(hostname, nickname);
		// Update the cached guest list
		cachedList 	= Collections.unmodifiableMap(guestlist);
		lock.unlock();
		return rv;
	}
	
	/**
	 * Remove guest with this particular hostname.<br/>
	 * 
	 * @param hostname	Hostname is the only unique identifier. 
	 * 					Nicknames are not guaranteed to be unique.
	 * 					(well host names aren't either, considering there could be a proxy or WHATEVER!)
	 */
	public String removeGuest(String hostname){
		lock.lock();
		String rv = guestlist.remove(hostname);
		// Update the cached guest list
		cachedList 	= Collections.unmodifiableMap(guestlist);
		lock.unlock();
		return rv;
	}
	
	/**
	 * Provide a thread-safe view of the most recent guest list.<br/>
	 * 
	 * @return	Most recent guest list
	 */
	public final Map<String, String> getList() {
		return cachedList;
	}
}