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

	public CS5300PROJ2RPCClient(int callID, CS5300PROJ2IPP ipp, CS5300PROJ2SessionId sessionID, int i) 
					throws SocketException, NumberFormatException, UnknownHostException {
		rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(CS5300PROJ2RPCMessage.RPC_TIMEOUT);
		ippDest = ipp;
		this.callID = callID;
		this.sessionID = sessionID;
		this.version = i;
	}

	public CS5300PROJ2RPCClient(int callID, CS5300PROJ2Cookie cookie, boolean primary) throws 
			NumberFormatException, SocketException, UnknownHostException {
		this(callID, primary ? cookie.getPrimaryLocation() : cookie.getBackupLocation(), 
				cookie.getSessionID(), cookie.getVersion());
	}

	public CS5300PROJ1Session read() 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.READ, callID, sessionID, version), true);
		if (recv == null) {
			return null;
		}
		return recv.getSession();
	}

	public boolean write(CS5300PROJ1Session session, long discardTime) 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recv = sendAndReceive(
				new CS5300PROJ2RPCMessage(callID, sessionID, version, session, discardTime), false);
		return (recv.getVersion() == 1);
	}

	public CS5300PROJ2RPCMessage delete() 
			throws NumberFormatException, IOException {
		return sendAndReceive(
				new CS5300PROJ2RPCMessage(CS5300PROJ2RPCMessage.OPT.DELETE, callID, sessionID, version), false);
	}

	private CS5300PROJ2RPCMessage sendAndReceive(CS5300PROJ2RPCMessage m, boolean twice) 
			throws NumberFormatException, IOException {
		CS5300PROJ2RPCMessage recvM = null;

		byte[] bytes = m.toString().getBytes("UTF-8");
		DatagramPacket sendPacket = 
				new DatagramPacket(bytes, 512, InetAddress.getByName(ippDest.getIP()), Integer.parseInt(ippDest.getPort()));
		rpcSocket.send(sendPacket);
		byte[] inBuf = new byte[512];
		DatagramPacket recvPacket = new DatagramPacket(inBuf, 512);

		for (int i = 0 ; i < 2 ; i++) {
			try {
				do {
					recvPacket.setLength(inBuf.length);
					rpcSocket.receive(recvPacket);
					recvM = new CS5300PROJ2RPCMessage(new String(inBuf, 0, 512, "UTF-8"));
				} while (recvM != null && recvM.getCallID() != m.getCallID());
			} catch (InterruptedIOException iioe) {
				if (i == 0 && twice) {
					continue;
				}
				recvM = null; // Set it back to a null
			} catch (IOException ioe) {
				//TODO what to do here?
			}
			break;
		}
		rpcSocket.close();
		return recvM;
	}


}
