package rpc;

import sessions.CS5300PROJ1Session;
import sessions.CS5300PROJ2SessionId;

public class CS5300PROJ2RPCMessage {

	enum TYPE {SEND, RECEIVE};
	enum OPT {READ, WRITE, DELETE};// MEMBERS only required for optional :P
	
	private TYPE type;
	private OPT opt;
	private double callID;
	private CS5300PROJ2SessionId sessionID;
	private int version; // -1 for not found, 1 for acknowledgment in Write/Delete return
	private CS5300PROJ1Session session;
	private long discardTime;
	
	public static int RPC_TIMEOUT = 3000;
	
	
	/**
	 * 
	 * Generic for all types of messages 
	 */
	public CS5300PROJ2RPCMessage(TYPE t, OPT o, double cID, CS5300PROJ2SessionId sID, int v, 
			CS5300PROJ1Session s, long dTime) {
		opt = o;
		type = t;
		callID = cID;
		sessionID = sID;
		version = v;
		discardTime = dTime;
		session = s;
	}
	
	
	/**
	 * Send for session write
	 */
	public CS5300PROJ2RPCMessage(double cID, CS5300PROJ2SessionId sID, int v, 
			CS5300PROJ1Session s, long dTime) {
		opt = OPT.WRITE;
		type = TYPE.SEND;
		callID = cID;
		sessionID = sID;
		version = v;
		session = s;
		discardTime = dTime;
	}
	
	/**
	 * Receive for session read
	 */
	public CS5300PROJ2RPCMessage(double cID, int v, CS5300PROJ1Session s) {
		opt = OPT.READ;
		type = TYPE.RECEIVE;
		callID = cID;
		version = v;
		session = s;
	}
	
	/**
	 *  Send for read and delete
	 */
	public CS5300PROJ2RPCMessage(OPT o, double cID, CS5300PROJ2SessionId sID, int v) {
		opt = o;
		type = TYPE.SEND;
		callID = cID;
		sessionID = sID;
		version = v;
	}
	
	/**
	 *  Receive for write and delete
	 */
	public CS5300PROJ2RPCMessage(OPT o, double cID, int v) {
		type = TYPE.RECEIVE;
		opt = o;
		callID = cID;
		version = v;
	}
	
	/**
	 * A string parser
	 * @param m
	 */
	public CS5300PROJ2RPCMessage(String m) {
		String[] args = m.split("~");
		if (args.length < 4) 
			return; // error case
		
		opt = OPT.valueOf(args[1]);
		callID = Double.parseDouble(args[2]);
		if (args[0].equals("RECEIVE")) { // Receiving message
			type = TYPE.RECEIVE;
			version = Integer.parseInt(args[3]);
			if (opt == OPT.READ) {
				session = new CS5300PROJ1Session(args[4]);
			}
		} else {
			type = TYPE.SEND;
			sessionID = new CS5300PROJ2SessionId(args[3]);
			version = Integer.parseInt(args[4]);
			if (opt == OPT.WRITE) {
				discardTime = Long.parseLong(args[5]);
				session = new CS5300PROJ1Session(args[6]);
			}
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type).append("~"); //0
		sb.append(opt).append("~");
		sb.append(callID).append("~"); //2
		
		if (type == type.RECEIVE) {
			sb.append(version); //3
			if (opt == OPT.READ) {
				//TODO session null case!!
				sb.append("~").append(session.toString()); //4
			} 
		} else {
			sb.append(sessionID.toString()).append(":"); //3
			sb.append(version); //4
			if (opt == OPT.WRITE) {
				sb.append("~").append(discardTime); //5
				sb.append("~").append(session.toString()); //6
			}
		}
		return sb.toString();
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

	public double getCallID() {
		return callID;
	}

	public void setCallID(double callID) {
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

}
