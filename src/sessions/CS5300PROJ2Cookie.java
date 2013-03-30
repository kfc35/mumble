package sessions;

import javax.servlet.http.Cookie;

public class CS5300PROJ2Cookie extends Cookie{

	private CS5300PROJ2SessionId sessionID;
	private CS5300PROJ2Location location;
	public static String COOKIE_NAME = "CS5300PROJ2";
	
	public CS5300PROJ2Cookie(CS5300PROJ2SessionId sID, int j, CS5300PROJ2Location i) {
		super(CS5300PROJ2Cookie.COOKIE_NAME, ""); //For posterity and legacy
		sessionID = sID;
		super.setVersion(j);
		location = i;
		this.setValue(toString());
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
		super.setVersion(Integer.parseInt(args[2])); //version
		location = new CS5300PROJ2Location(args[3]); //port_address~port_address
		this.setValue(toString());
	}
	
	/**
	 * Dummy cookie to erase cookies on the browser
	 */
	public CS5300PROJ2Cookie() {
		super(CS5300PROJ2Cookie.COOKIE_NAME, "");
		this.setValue(toString());
	}
	
	public CS5300PROJ2SessionId getSessionID() {
		return sessionID;
	}

	public void setSessionID(CS5300PROJ2SessionId sessionID) {
		this.sessionID = sessionID;
		this.setValue(toString());
	}

	public int getVersion() {
		return super.getVersion();
	}
	
	public void incrementVersion() {
		super.setVersion(super.getVersion() + 1);
		this.setValue(toString());
	}

	public void setVersion(int version) {
		super.setVersion(version);
		this.setValue(toString());
	}

	public CS5300PROJ2Location getLocation() {
		return location;
	}

	public void setLocation(CS5300PROJ2Location ipp) {
		this.location = ipp;
		this.setValue(toString());
	}
	
	public CS5300PROJ2IPP getPrimaryIPP() {
		return this.location.getPrimaryIPP();
	}
	
	public CS5300PROJ2IPP getBackupIPP() {
		return this.location.getBackupIPP();
	}
	
	public boolean hasBackupIPP() {
		return this.getBackupIPP() == null;
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
		sb.append(super.getVersion()).append("_");
		sb.append(location.toString());
		return sb.toString();
	}
	
	public boolean equalsEitherLocation(CS5300PROJ2IPP i) {
		return location.equalsEither(i);
	}

}
