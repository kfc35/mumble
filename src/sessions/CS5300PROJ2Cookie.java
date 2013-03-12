package sessions;

import javax.servlet.http.Cookie;

public class CS5300PROJ2Cookie extends Cookie{

	private CS5300PROJ2SessionId sessionID;
	private int version;
	private CS5300PROJ2Location ipp;
	public static String COOKIE_NAME = "CS5300PROJ2";
	
	public CS5300PROJ2Cookie(CS5300PROJ2SessionId sID, int v, CS5300PROJ2Location i) {
		super(CS5300PROJ2Cookie.COOKIE_NAME, ""); //For posterity and legacy
		sessionID = sID;
		version = v;
		ipp = i;
	}
	
	public CS5300PROJ2Cookie(String s) {
		super(CS5300PROJ2Cookie.COOKIE_NAME, ""); // For posterity and legacy
		String[] args = s.split("_", 4);
		/*
		 * Splits into : 
		 *   uID~port
		 *   address
		 *   version
		 *   port_address~port_address
		 */
		
		// calls with UID~port_address
		sessionID = new CS5300PROJ2SessionId(args[0] + "_" +  args[1]); 
		version = Integer.parseInt(args[2]); //version
		ipp = new CS5300PROJ2Location(args[3]); //port_address~port_address
	}
	
	/**
	 * Dummy cookie to erase cookies on the browser
	 */
	public CS5300PROJ2Cookie() {
		super(CS5300PROJ2Cookie.COOKIE_NAME, "");
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
	
	public void incrementVersion() {
		this.version++;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public CS5300PROJ2Location getIpp() {
		return ipp;
	}

	public void setIpp(CS5300PROJ2Location ipp) {
		this.ipp = ipp;
	}

	/**
	 * This is what it should look like:
	 * SID_version_IPP = uID~port_address_version_port_address~port_address
	 * where SID = uID~port_address
	 * IPP = port_address~port_address
	 * 
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sessionID.toString()).append("_");
		sb.append(version).append("_");
		sb.append(ipp.toString());
		return sb.toString();
	}

}
