package sessions;

import java.io.UnsupportedEncodingException;
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
	
	/*
	 * Brand new session, only need the uid and my own ipp
	 */
	public CS5300PROJ1Session(CS5300PROJ2SessionId sID) {
		message = CS5300PROJ1Servlet.DEFAULT_MESSAGE;
		end = (new Date()).getTime() + CS5300PROJ1Servlet.EXPIRY_TIME_FROM_CURRENT;
		CS5300PROJ2Location ipps = new CS5300PROJ2Location(sID.getOriginIPP());
		cookie = new CS5300PROJ2Cookie(sID, 0, ipps);
	}
	
	public CS5300PROJ1Session(String s) {
		String[] args = s.split("~", 5);
		/*
		 * Splits into 
		 * uID                                 |
		 * port_address_version_port_address   | All part of cookie
		 * port_address                        |
		 * end
		 * message
		 */
		cookie = new CS5300PROJ2Cookie(args[0] + "~" + args[1] + "~" + args[2]);
		end = Long.parseLong(args[3]);
		message = args[4];
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
		try {
			return (new String(sb.toString().getBytes("UTF-8"), 0, 512, "UTF-8")).toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
