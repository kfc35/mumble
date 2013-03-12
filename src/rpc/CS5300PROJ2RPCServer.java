package rpc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
	//TODO memberSet addings and updates
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
			CS5300PROJ2RPCMessage msg = new CS5300PROJ2RPCMessage(inBuf.toString());
			CS5300PROJ2RPCMessage returnMsg = null;
			switch (msg.getOpt()) {
				case READ:
					CS5300PROJ1Session sess = null;
					synchronized(sessionDataTable) {
						sess = sessionDataTable.get(msg.getSessionID());
					}
					if (sess == null) {
						returnMsg = new CS5300PROJ2RPCMessage(msg.getCallID(), -1, null);
					}
					else {
						returnMsg = new CS5300PROJ2RPCMessage(msg.getCallID(), sess.getVersion(), sess);
					}
					break;
					
				case WRITE:
					synchronized(sessionDataTable) {
						sessionDataTable.put(msg.getSessionID(), msg.getSession());
					}
					//TODO i may have to do more here than this...
					//E.g. GARBAGE COLLECT pre-existing session
					returnMsg = new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.WRITE, msg.getCallID(), 1);
					break;
					
				case DELETE:
					synchronized(sessionDataTable) {
						sessionDataTable.remove(msg.getSessionID());
					}
					returnMsg = new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.DELETE, msg.getCallID(), 1);
					break;
				
				default:
					break;
			}
			if (returnMsg != null) {
				byte[] bytes = null;
				try {
					bytes = returnMsg.toString().getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					// this should NEVER happen.
					e.printStackTrace();
				}
				DatagramPacket sendPacket = 
						new DatagramPacket(bytes, 512, returnAddr, returnPort);
				try {
					rpcSocket.send(sendPacket);
				} catch (IOException e) {
					if (CS5300PROJ1Servlet.DEBUG) {
						e.printStackTrace();
					}
					continue; //just drop this packet, no resending.
				}
			}
		}
	}
}
