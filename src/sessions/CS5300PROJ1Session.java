package sessions;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.http.Cookie;

public class CS5300PROJ1Session {
 
	/** Default */
	private static final long serialVersionUID = 1L;

	private String message;
	private long end;
	private CS5300PROJ2Cookie cookie;
	
	public CS5300PROJ1Session(String m, long e, CS5300PROJ2Cookie c) {
		message = m;
		end = e;
		cookie = c;
	}

	public CS5300PROJ1Session(String m, CS5300PROJ2Cookie c) {
		this( m, (new Date()).getTime() + CS5300PROJ1Servlet.EXPIRY_TIME_FROM_CURRENT
				, c);
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
