package rpc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import sessions.CS5300PROJ1Session;
import sessions.CS5300PROJ2SessionId;

public class CS5300PROJ2RPCMessage {

	enum TYPE {S, R};
	enum OPT {R, W, D};// MEMBERS only required for optional :P

	/*
	 * D will have the form:
	 * for send: S~D~callID~port~SID~version
	 * for receive: R~callID~port~D~version
	 * 
	 * R will have the form:
	 * for send: S~R~callID~port~SID~version
	 * for receive: R~D~callID~port~version~message
	 * 
	 * W will have the form:
	 * for send: S~W~callID~SID~port~version~discardTime~dataMessage
	 * for receive: R~W~callID~port~version
	 * 
	 */

	private TYPE type;
	private OPT opt;
	private int callID;
	private CS5300PROJ2SessionId sessionID;
	private int version; // -1 for not found, 1 for acknowledgment in Write/Delete return
	private long discardTime;
	private String port;
	private CS5300PROJ1Session session;

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
		opt = OPT.W;
		type = TYPE.S;
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
		opt = OPT.R;
		type = TYPE.R;
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
		type = TYPE.S;
		callID = cID;
		sessionID = sID;
		version = v;
		port = p;
	}

	/**
	 *  Receive for write and delete
	 */
	public CS5300PROJ2RPCMessage(OPT o, int cID, int v, String p) {
		type = TYPE.R;
		opt = o;
		callID = cID;
		version = v;
		port = p;
	}

	/**
	 * A string parser
	 * @param m
	 * 
	 * D will have the form:
	 * for send: S~D~callID~port~SID~version
	 * for receive: R~callID~port~SID~version
	 * 
	 * R will have the form:
	 * for send: S~R~callID~port~SID~version
	 * for receive: R~D~callID~port~session
	 * 
	 * W will have the form:
	 * for send: S~W~callID~port~session
	 * for receive: R~W~callID~port~version
	 */
	public CS5300PROJ2RPCMessage(String m) {
		System.out.println("Making a message out of: " + m);
		String[] args = m.split("~", 7);
		if (args.length < 5) 
			return; // error case

		opt = OPT.valueOf(args[1]);
		callID = Integer.parseInt(args[2]);
		port = args[3];
		if (args[0].equals("R")) { // Receiving message
			type = TYPE.R;
			if (opt == OPT.R) {
				if (args[4].toLowerCase().equals("null")) {
					session = null;
				} else {
					session = new CS5300PROJ1Session(args[4] + "~" + args[5] + "~" + args[6]);
				}
			} else {
				version = Integer.parseInt(args[4]);
			}
		} else {
			type = TYPE.S;
			if (opt == OPT.W) {
				if (args[4].toLowerCase().equals("null")) {
					session = null;
				} else {
					System.out.println("Session is: " + args[4]);
					session = new CS5300PROJ1Session(args[4] + "~" + args[5] + "~" + args[6]);
				}
			} else {
				sessionID = new CS5300PROJ2SessionId(args[4] + "~" + args[5]);
				version = Integer.parseInt(args[6]);
			}
		}
	}

	/*
	 * D will have the form:
	 * for send: S~D~callID~port~SID~version
	 * for receive: R~callID~port~D~version
	 * 
	 * R will have the form:
	 * for send: S~R~callID~port~SID~version
	 * for receive: R~D~callID~port~version~message
	 * 
	 * W will have the form:
	 * for send: S~W~callID~port~session
	 * for receive: R~W~callID~port~version
	 * 
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append("~"); //0
		sb.append(opt).append("~");
		sb.append(callID).append("~"); //2
		sb.append(port).append("~"); //3

		if (type == TYPE.R) {
			sb.append(version); //4
			if (opt == OPT.R) {
				//TODO session null case!!
				if (session == null) {
					sb.append("~").append("null");
				} else {
					sb.append("~").append(session.getMessage()); //5
				}
			} 
		} else {
			sb.append(sessionID.toString()).append("~"); //4
			if (opt == OPT.W) {
				// 5
				if (session == null) {
					sb.append("~").append("null");
				} else {
					sb.append("~").append(session);
				}
			} else {
				sb.append(version); //5
			}
		}
		System.out.println("Message to string:" + sb.toString());
		return sb.append("~").toString();
	}

	public byte[] toBytes() throws UnsupportedEncodingException {
		System.out.println("bytes from string: " + toString());
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
		return type == TYPE.S;
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

	public String getDataMessage() {
		return session.getMessage();
	}

	public void setDataMessage(String s) {
		session.setMessage(s);
	}

	public void setSession(CS5300PROJ1Session s) {
		session = s;
	}

	public CS5300PROJ1Session getSession() {
		return session;
	}

	public String getPort() {
		return port;
	}


	public void setPort(String port) {
		this.port = port;
	}

}
