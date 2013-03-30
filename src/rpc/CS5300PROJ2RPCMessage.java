package rpc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import sessions.CS5300PROJ1Session;
import sessions.CS5300PROJ2SessionId;

public class CS5300PROJ2RPCMessage {

	enum TYPE {SEND, RECEIVE};
	enum OPT {READ, WRITE, DELETE};// MEMBERS only required for optional :P
	
	private TYPE type;
	private OPT opt;
	private int callID;
	private CS5300PROJ2SessionId sessionID;
	private int version; // -1 for not found, 1 for acknowledgment in Write/Delete return
	private CS5300PROJ1Session session;
	private long discardTime;
	private String port;
	
	public static int RPC_TIMEOUT = 3000;
	
	
	/**
	 * 
	 * Generic for all types of messages 
	 */
	public CS5300PROJ2RPCMessage(TYPE t, OPT o, int cID, CS5300PROJ2SessionId sID, int v, 
			CS5300PROJ1Session s, long dTime, String p) {
		opt = o;
		type = t;
		callID = cID;
		sessionID = sID;
		version = v;
		discardTime = dTime;
		session = s;
		port = p;
	}
	
	
	/**
	 * Send for session write
	 */
	public CS5300PROJ2RPCMessage(int cID, CS5300PROJ2SessionId sID, int v, 
			CS5300PROJ1Session s, long dTime, String p) {
		opt = OPT.WRITE;
		type = TYPE.SEND;
		callID = cID;
		sessionID = sID;
		version = v;
		session = s;
		discardTime = dTime;
		port = p;
	}
	
	/**
	 * Receive for session read
	 */
	public CS5300PROJ2RPCMessage(int cID, int v, CS5300PROJ1Session s, String p) {
		opt = OPT.READ;
		type = TYPE.RECEIVE;
		callID = cID;
		version = v;
		session = s;
		port = p;
	}
	
	/**
	 *  Send for read and delete
	 */
	public CS5300PROJ2RPCMessage(OPT o, int cID, CS5300PROJ2SessionId sID, int v, String p) {
		opt = o;
		type = TYPE.SEND;
		callID = cID;
		sessionID = sID;
		version = v;
		port = p;
	}
	
	/**
	 *  Receive for write and delete
	 */
	public CS5300PROJ2RPCMessage(OPT o, int cID, int v, String p) {
		type = TYPE.RECEIVE;
		opt = o;
		callID = cID;
		version = v;
		port = p;
	}
	
	/**
	 * A string parser
	 * @param m
	 */
	public CS5300PROJ2RPCMessage(String m) {
		String[] args = m.split("~", 9);
		if (args.length < 5) 
			return; // error case
		
		opt = OPT.valueOf(args[1]);
		callID = Integer.parseInt(args[2]);
		port = args[3];
		if (args[0].equals("RECEIVE")) { // Receiving message
			type = TYPE.RECEIVE;
			version = Integer.parseInt(args[4]);
			if (opt == OPT.READ) {
				if (args[5].toLowerCase().equals("null")) {
					session = null;
				} else {
				session = new CS5300PROJ1Session(args[5]);
				}
			}
		} else {
			type = TYPE.SEND;
			sessionID = new CS5300PROJ2SessionId(args[4] + "~" + args[5]);
			version = Integer.parseInt(args[6]);
			if (opt == OPT.WRITE) {
				discardTime = Long.parseLong(args[7]);
				if (args[8].toLowerCase().equals("null")) {
					session = null;
				} else {
				session = new CS5300PROJ1Session(args[8]);
				}
			}
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append("~"); //0
		sb.append(opt).append("~");
		sb.append(callID).append("~"); //2
		sb.append(port).append("~"); //3
		
		if (type == TYPE.RECEIVE) {
			sb.append(version); //4
			if (opt == OPT.READ) {
				//TODO session null case!!
				if (session == null) {
					sb.append("~").append("null");
				} else {
				sb.append("~").append(session.toString()); //5
				}
			} 
		} else {
			sb.append(sessionID.toString()).append("~"); //4
			sb.append(version); //5
			if (opt == OPT.WRITE) {
				sb.append("~").append(discardTime); //6
				sb.append("~").append(session.toString()); //7
			}
		}
		return sb.append("~").toString();
	}
	
	public byte[] toBytes() throws UnsupportedEncodingException {
		byte[] msgBytes = this.toString().getBytes("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(512);
		bb.put(msgBytes);
		return bb.array();
	}
	

	public OPT getOpt() {
		return opt;
	}

	public void setOpt(OPT opt) {
		this.opt = opt;
	}

	public TYPE getType() {
		return type;
	}
	
	public boolean isSend() {
		return type == TYPE.SEND;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public int getCallID() {
		return callID;
	}

	public void setCallID(int callID) {
		this.callID = callID;
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

	public long getDiscardTime() {
		return discardTime;
	}

	public void setDiscardTime(long discardTime) {
		this.discardTime = discardTime;
	}

	public CS5300PROJ1Session getSession() {
		return session;
	}

	public void setSession(CS5300PROJ1Session s) {
		this.session = s;
	}

	public String getPort() {
		return port;
	}


	public void setPort(String port) {
		this.port = port;
	}

}
