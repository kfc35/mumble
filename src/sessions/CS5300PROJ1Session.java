package sessions;

import java.util.Date;

public class CS5300PROJ1Session {
 
	/** Default */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private String message;
	private long end;
	private CS5300PROJ2Cookie cookie;
	
	public CS5300PROJ1Session(String m, long e, CS5300PROJ2Cookie c) {
		message = m;
		end = e;
		cookie = c;
	}
	
	/**
	 * Brand new session, only need the uid and my own ipp
	 */
	public CS5300PROJ1Session(CS5300PROJ2SessionId sID) {
		message = CS5300PROJ1Servlet.DEFAULT_MESSAGE;
		end = (new Date()).getTime() + CS5300PROJ1Servlet.DISCARD_TIME_FROM_CURRENT;
		CS5300PROJ2Location ipps = new CS5300PROJ2Location(sID.getOriginIPP());
		cookie = new CS5300PROJ2Cookie(sID, 0, ipps);
	}
	
	/**
	 * Expired or not found session
	 */
	public CS5300PROJ1Session() {
		end = -1;
	}
	
	public CS5300PROJ1Session(String s) {
		System.out.println("Trying to create a session with the string: " + s);
		String[] args = s.split("~", 7);
		/*
		 * Splits into 
		 * uID                                 |
		 * port_address_version_port_address   | All part of cookie
		 * port_address                        |
		 * end
		 * message
		 */
		cookie = new CS5300PROJ2Cookie(s.substring(0, s.indexOf(args[6]) - 1));
		end = Long.parseLong(args[5]);
		message = args[6];
	}
	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setEnd(long end) {
		this.end = end;
	}
	
	public long getEnd() {
		return end;
	}

	public CS5300PROJ2Cookie getCookie() {
		return cookie;
	}

	public void setCookie(CS5300PROJ2Cookie cookie) {
		this.cookie = cookie;
	}
	
	public int getVersion() {
		return cookie.getVersion();
	}
	
	public void setVersion(int v) {
		cookie.setVersion(v);
	}
	
	public void incrementVersion() {
		cookie.incrementVersion();
	}
	
	public CS5300PROJ2SessionId getSessionID() {
		return cookie.getSessionID();
	}
	
	public void setPrimaryIPP(CS5300PROJ2IPP ipp) {
		cookie.getLocation().setPrimaryIPP(ipp);
	}
	
	public void setBackupIPP(CS5300PROJ2IPP ipp) {
		cookie.getLocation().setBackupIPP(ipp);
	}

	/**
	 * This is what it should look like:
	 * cookie~end~message 
	 * overall is ^^^ uID~port_address_version_port_address~port_address~end~message^^^
	 * cookie = SID_version_IPP = uID~port_address_version_port_address~port_address
	 * where SID = uID~port_address
	 * IPP = port_address~port_address
	 * 
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(cookie.toString()).append("~");
		sb.append(end).append("~");
		sb.append(message);
		return sb.toString();
	}
}
