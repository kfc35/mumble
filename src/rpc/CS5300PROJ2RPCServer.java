package rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
		while(true) {
			byte[] inBuf = new byte[512];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			try {
				rpcSocket.receive(recvPkt);
			} catch (IOException e) {
				if (CS5300PROJ1Servlet.DEBUG) {
					e.printStackTrace();
				}
				continue; //just drop this packet
			}
			InetAddress returnAddr = recvPkt.getAddress();
			int returnPort = recvPkt.getPort();
			//TODO Sweet, is this how I'm supposed to parse the buffer?
			CS5300PROJ2RPCMessage msg = new CS5300PROJ2RPCMessage(inBuf.toString());
			switch (msg.getOpt()) {
				case READ:
					CS5300PROJ1Session sess = sessionDataTable.get(msg.getSessionID());
					break;
					
				case WRITE:
					break;
					
				case DELETE:
					break;
				
				default:
					break;
			}
		}
	}
}
