package sessions;

import java.util.concurrent.ConcurrentHashMap;

public class CS5300PROJ2RPCServer implements Runnable{
	private ConcurrentHashMap<String, CS5300PROJ1Session> sessionDataTable;

	public CS5300PROJ2RPCServer(ConcurrentHashMap<String, CS5300PROJ1Session> sessionDataTable) {
		this.sessionDataTable = sessionDataTable;
	}
	
	@Override
	public synchronized void run() {
		while(true) {
			//TODO RPC server code.
			// Sweet writing stuff
		}
	}
}
