package rpc;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import sessions.CS5300PROJ1Session;
import sessions.CS5300PROJ2Cookie;
import sessions.CS5300PROJ2IPP;
import sessions.CS5300PROJ2SessionId;

public class CS5300PROJ2RPCClient {

	private int callID;
	private DatagramSocket rpcSocket;
	private CS5300PROJ2IPP ippDest;
	private CS5300PROJ2SessionId sessionID;
	private int version;
	private String port;

	public CS5300PROJ2RPCClient(int callID, CS5300PROJ2IPP ipp, CS5300PROJ2SessionId sessionID, int i, String p) 
			throws SocketException, NumberFormatException, UnknownHostException {
		rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(CS5300PROJ2RPCMessage.RPC_TIMEOUT);
		ippDest = ipp;
		this.callID = callID;
		this.sessionID = sessionID;
		this.version = i;
		this.port = p;
	}

	public CS5300PROJ2RPCClient(int callID, CS5300PROJ2Cookie cookie, boolean primary, String p) throws 
	NumberFormatException, SocketException, UnknownHostException {
		this(callID, primary ? cookie.getPrimaryIPP() : cookie.getBackupIPP(), 
				cookie.getSessionID(), cookie.getVersion(), p);
	}

	public CS5300PROJ1Session read() 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.READ, callID, sessionID, version, port), true);
		if (recv == null) {
			return null;
		}
		return recv.getSession();
	}

	public boolean write(CS5300PROJ1Session session, long discardTime) 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(callID, sessionID, version, session, discardTime, port), false);
		if (recv == null) {
			return false;
		}
		return (recv.getVersion() == 1);
	}

	public boolean delete() 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.DELETE, callID, sessionID, version, port), false);
		
		if (recv == null) {
			return false;
		}
		return (recv.getVersion() == 1);
	}

	private CS5300PROJ2RPCMessage sendAndReceive(CS5300PROJ2RPCMessage m, boolean twice) 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recvM = null;

		byte[] bytes = m.toBytes();

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
			recvM = null; // Set it back to a null
		} catch (IOException ioe) {
			//TODO what to do here?
			recvM = null;
		}
		rpcSocket.close();
		return recvM;
	}

	public int getCallID() {
		return callID;
	}

	public void setCallID(int callID) {
		this.callID = callID;
	}

	public CS5300PROJ2IPP getIppDest() {
		return ippDest;
	}

	public void setIppDest(CS5300PROJ2IPP ippDest) {
		this.ippDest = ippDest;
	}

	public CS5300PROJ2SessionId getSessionID() {
		return sessionID;
	}

	public void setSessionID(CS5300PROJ2SessionId sessionID) {
		this.sessionID = sessionID;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
