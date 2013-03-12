package sessions;

import javax.servlet.http.Cookie;

public class CS5300PROJ2Cookie extends Cookie{

	private CS5300PROJ2SessionId sessionID;
	private int version;
	private CS5300PROJ2Location ipp;
	
	public CS5300PROJ2Cookie(CS5300PROJ2SessionId sID, int v, CS5300PROJ2Location i) {
		super("" + v, "" + v);
		sessionID = sID;
		version = v;
		ipp = i;
	}
	/*
	public CS5300PROJ2Cookie(String s) {
		
	}
	*/
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sessionID.toString()).append("~");
		sb.append(version).append("~");
		return sb.toString();
	}

}
