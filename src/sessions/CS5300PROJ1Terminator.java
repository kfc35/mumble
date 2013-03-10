package sessions;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 
 * @author sweet
 * This class is responsible to removing over-time sessions from the session table
 *
 */
public class CS5300PROJ1Terminator implements Runnable {
	private ConcurrentHashMap<String, CS5300PROJ1Session> sessionDataTable;

	public CS5300PROJ1Terminator(ConcurrentHashMap<String, CS5300PROJ1Session> sessionDataTable) {
		this.sessionDataTable = sessionDataTable;
	}

	@Override
	public synchronized void run() {
		while(true) {
			try{
				this.wait(1000 * 120); // Runs every 2 minutes 
				System.out.println("Terminator check");

				// Synchronizes the sessionDataTable so that the access and removes don't conflict
				synchronized (sessionDataTable) {
					// For every entry in the hashtable, remove all expired sessions 
					for (Entry<String, CS5300PROJ1Session> e: sessionDataTable.entrySet()) {
						CS5300PROJ1Session session = e.getValue();
						
						if (session.getEnd() < System.currentTimeMillis()) {
							if (CS5300PROJ1Servlet.DEBUG) {
								System.out.println("Terminator removing the session: " 
										+ session.getSessionId());
							}
							sessionDataTable.remove(e.getKey());
						}
					}
				}
			} catch (InterruptedException e) {
				if (CS5300PROJ1Servlet.DEBUG) {
					System.out.println("Terminator erroed in waiting.");
				}
			}
		}
	}

}
