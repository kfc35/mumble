package rpc;

import sessions.CS5300PROJ2SessionId;

public class CS5300PROJ1RPCMessage {

	enum OPT {READ, WRITE, DELETE};// MEMBERS only required for optional :P
	enum TYPE {SEND, RECEIVE};
	
	private OPT opt;
	private TYPE type;
	private int callID;
	private CS5300PROJ2SessionId sessionID;
	private int version; // 0, 1 for acknowledgment in Write/Delete return
	private String data;
	private long discardTime;
	
	
	/**
	 * 
	 * Generic for all types of messages 
	 * @param o
	 * @param t
	 * @param cID
	 * @param sID
	 * @param v
	 * @param dTime
	 * @param d
	 */
	public CS5300PROJ1RPCMessage(OPT o, TYPE t, int cID, CS5300PROJ2SessionId sID, int v, String d, long dTime) {
		opt = o;
		type = t;
		callID = cID;
		sessionID = sID;
		version = v;
		discardTime = dTime;
		data = d;
	}
	
	
	public CS5300PROJ1RPCMessage(OPT o, CS5300PROJ2SessionId sID, int v, String d, long dTime) {
	
	}
	
	public CS5300PROJ1RPCMessage(OPT o, CS5300PROJ2SessionId sID, int v) {
		
	}
	
	public CS5300PROJ1RPCMessage(String m) {
		
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
