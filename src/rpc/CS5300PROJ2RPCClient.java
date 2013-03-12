package rpc;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import rpc.CS5300PROJ2RPCMessage.OPT;
import sessions.*;

public class CS5300PROJ2RPCClient {
	
	private ConcurrentHashMap<CS5300PROJ2SessionId, CS5300PROJ1Session> sessionDataTable;
	private double callID;
	private DatagramSocket rpcSocket;
	private CS5300PROJ2IPP ippDest;
	private CS5300PROJ2SessionId sessionID;
	private int version;
	
	public CS5300PROJ2RPCClient(ConcurrentHashMap<CS5300PROJ2SessionId, CS5300PROJ1Session> sessionDataTable,
			double callID, CS5300PROJ2IPP ipp, CS5300PROJ2SessionId sessionID, int version) 
					throws SocketException, NumberFormatException, UnknownHostException {
		this.sessionDataTable= sessionDataTable;
		rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(CS5300PROJ2RPCMessage.RPC_TIMEOUT);
		ippDest = ipp;
		this.callID = callID;
		this.sessionID = sessionID;
		this.version = version;
	}

	public void read() 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.READ, callID, sessionID, version));
		if (recv.getVersion() >= version) {
			synchronized (sessionDataTable) {
				sessionDataTable.put(sessionID, recv.getSession());
			}
		} //TODO: else
	}
	
	public void write(CS5300PROJ1Session session, long discardTime) 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(callID, sessionID, version, session, discardTime));
		synchronized (sessionDataTable) {
			session.setEnd(discardTime);
			session.setVersion(version);
			sessionDataTable.put(sessionID, session);
		}
	}
	
	public void delete() 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.DELETE, callID, sessionID, version));
		// no acknowledgment necessary
		synchronized (sessionDataTable) {
			sessionDataTable.remove(sessionID);
		}
	}
	
	private CS5300PROJ2RPCMessage sendAndReceive(CS5300PROJ2RPCMessage m) 
			throws NumberFormatException, IOException {
			CS5300PROJ2RPCMessage recvM = null;
			
		byte[] bytes = m.toString().getBytes("UTF-8");
		DatagramPacket sendPacket = 
				new DatagramPacket(bytes, 512, InetAddress.getByName(ippDest.getIP()), Integer.parseInt(ippDest.getPort()));
		rpcSocket.send(sendPacket);
		byte[] inBuf = new byte[512];
		DatagramPacket recvPacket = new DatagramPacket(inBuf, 512);
		
		try {
			do {
				recvPacket.setLength(inBuf.length);
				rpcSocket.receive(recvPacket);
				recvM = new CS5300PROJ2RPCMessage(new String(inBuf, 0, 512, "UTF-8"));
			} while (recvM != null && recvM.getCallID() != m.getCallID());
		} catch (InterruptedIOException iioe) {
		} catch (IOException ioe) {
			//TODO what to do here?
		}
		rpcSocket.close();
		return recvM;
	}
	
	
}
