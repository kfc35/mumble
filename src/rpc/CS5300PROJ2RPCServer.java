package rpc;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import sessions.CS5300PROJ1Servlet;
import sessions.CS5300PROJ1Session;
import sessions.CS5300PROJ2IPP;
import sessions.CS5300PROJ2SessionId;

public class CS5300PROJ2RPCServer implements Runnable{
	private ConcurrentHashMap<CS5300PROJ2SessionId, CS5300PROJ1Session> sessionDataTable;
	private ConcurrentHashMap<CS5300PROJ2IPP, Integer> memberSet;
	private DatagramSocket rpcSocket;

	//TODO degrade gracefully if a socket cannot be opened?
	public CS5300PROJ2RPCServer(ConcurrentHashMap<CS5300PROJ2SessionId, CS5300PROJ1Session> sessionDataTable,
			ConcurrentHashMap<CS5300PROJ2IPP, Integer> memberSet){
		this.sessionDataTable = sessionDataTable;
		this.memberSet = memberSet;
		try {
			this.rpcSocket = new DatagramSocket();
		}
		catch(SocketException se) {
			if (CS5300PROJ1Servlet.DEBUG) {
				se.printStackTrace();
			}
			this.rpcSocket = null;
		}
	}
	
	public String getPort() {
		if (this.rpcSocket != null) {
			return "" + this.rpcSocket.getPort();
		}
		else return "";
	}
	
	public String getAddress() {
		return this.rpcSocket.getLocalAddress().getHostAddress();
	}
	
	@Override
	public synchronized void run() {
		System.out.println(this.rpcSocket.getLocalAddress().getHostAddress());
		while(true) {
			//TODO RPC server code.
		}
	}
}