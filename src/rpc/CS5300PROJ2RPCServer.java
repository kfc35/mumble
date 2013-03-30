package rpc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.Inet4Address;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import sessions.CS5300PROJ1Servlet;
import sessions.CS5300PROJ1Session;
import sessions.CS5300PROJ2IPP;
import sessions.CS5300PROJ2Location;

public class CS5300PROJ2RPCServer implements Runnable{
	private ConcurrentHashMap<String, CS5300PROJ1Session> sessionDataTable;
	private ConcurrentHashMap<String, Integer> memberSet;
	private DatagramSocket rpcSocket;

	//TODO degrade gracefully if a socket cannot be opened?
	public CS5300PROJ2RPCServer(ConcurrentHashMap<String, CS5300PROJ1Session> sessionDataTable,
			ConcurrentHashMap<String, Integer> memberSet){
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
	
	public String getLocalPort() {
		if (this.rpcSocket != null) {
			return "" + this.rpcSocket.getLocalPort();
		}
		else return "";
	}
	
	public String getLocalAddress() {
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if (interfaces == null) return "";
		while (interfaces.hasMoreElements()){
		    NetworkInterface current = interfaces.nextElement();
		    try {
				if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
			} catch (SocketException e) {
				e.printStackTrace();
				continue;
			}
		    Enumeration<InetAddress> addresses = current.getInetAddresses();
		    while (addresses.hasMoreElements()){
		        InetAddress current_addr = addresses.nextElement();
		        if (current_addr.isLoopbackAddress()) continue;
		        if (current_addr instanceof Inet4Address) return current_addr.getHostAddress();
		    }
		}
		return "";
	}
	
	public boolean failed() {
		return this.rpcSocket == null;
	}
	
	@Override
	public synchronized void run() {
		while(!CS5300PROJ1Servlet.CRASH) {
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
			if (CS5300PROJ1Servlet.CRASH) { //put here because receive blocks
				break;
			}
			InetAddress returnAddr = recvPkt.getAddress();
			int returnPort = recvPkt.getPort();
			String msgString = null;
			try {
				msgString = new String(inBuf, 0, 512, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// this should NEVER happen.
				e1.printStackTrace();
			}
			System.out.println(msgString);
			CS5300PROJ2RPCMessage msg = new CS5300PROJ2RPCMessage(msgString);
			CS5300PROJ2IPP recvIPP = new CS5300PROJ2IPP(returnAddr.getHostAddress(), msg.getPort());
			System.out.println(recvIPP);
			CS5300PROJ2RPCMessage returnMsg = null;
			switch (msg.getOpt()) {
				case READ:
					CS5300PROJ1Session sess = null;
					synchronized(sessionDataTable) {
						sess = sessionDataTable.get(msg.getSessionID().toString());
					}
					if (sess == null) {
						returnMsg = new CS5300PROJ2RPCMessage(msg.getCallID(), -123, null, getLocalPort());
					}
					else {
						returnMsg = new CS5300PROJ2RPCMessage(msg.getCallID(), sess.getVersion(), sess, getLocalPort());
					}
					break;
					
				case WRITE:
					synchronized(sessionDataTable) {
						sessionDataTable.put(msg.getSessionID().toString(), msg.getSession());
					}
					CS5300PROJ2Location location = msg.getSession().getCookie().getLocation();
					synchronized(memberSet) {
						if (!memberSet.containsKey(location.getPrimaryIPP().toString()) && 
								!location.getPrimaryIPP().equals(new CS5300PROJ2IPP(this.getLocalAddress(), this.getLocalPort()))) {
							memberSet.put(location.getPrimaryIPP().toString(), -1);
						}
						if (location.getBackupIPP() != null && !memberSet.containsKey(location.getBackupIPP().toString()) &&
								!location.getBackupIPP().equals(new CS5300PROJ2IPP(this.getLocalAddress(), this.getLocalPort()))) {
							memberSet.put(location.getBackupIPP().toString(), -1);
						}
					}
					returnMsg = new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.WRITE, msg.getCallID(), 1, getLocalPort());
					break;
					
				case DELETE:
					synchronized(sessionDataTable) {
						sessionDataTable.remove(msg.getSessionID().toString());
					}
					returnMsg = new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.DELETE, msg.getCallID(), 1, getLocalPort());
					break;
				
				default:
					break;
			}
			/*This synchronized block is down here to avoid deadlock
			 * Our locks are ordered such that sessionDataTable is locked first */
			synchronized(memberSet) { //add this guy to the memberSet if not added before
				if (!memberSet.containsKey(recvIPP.toString())) {
					memberSet.put(recvIPP.toString(), msg.getCallID());
				}
				else if (memberSet.get(recvIPP.toString()) > msg.getCallID()) {
					//the most recent callID processed from this member
					//is LATER than the callID of this message from the SAME member
					continue;
				}
				else {
					memberSet.put(recvIPP.toString(), msg.getCallID());
				}
			}
			if (returnMsg != null) {
				byte[] bytes = null;
				try {
					bytes = returnMsg.toBytes();
				} catch (UnsupportedEncodingException e) {
					// this should NEVER happen.
					e.printStackTrace();
				}
				DatagramPacket sendPacket = 
						new DatagramPacket(bytes, 512, returnAddr, returnPort);
				try {
					if (CS5300PROJ1Servlet.CRASH) {
						break;
					}
					rpcSocket.send(sendPacket);
				} catch (IOException e) {
					if (CS5300PROJ1Servlet.DEBUG) {
						e.printStackTrace();
					}
					continue; //just drop this packet, no resending.
				}
			}
		}
		rpcSocket.close();
	}
}
