package sessions;

import java.util.Date;

import javax.servlet.http.Cookie;

public class CS5300PROJ1Session extends Cookie {
 
	/** Default */
	private static final long serialVersionUID = 1L;
	public static String COOKIE_NAME = "CS5300PROJ1SESSION";

	//TODO 512 byte restriction on session state value
	private String sessionId;
	private String message;
	private long end;
	private String location;
	
	public CS5300PROJ1Session(String session, String m, long e, 
			String l) {
		super(session, m);
		sessionId = session;
		message = m;
		end = e;
		this.setVersion(0);
		this.location = l;
	}

	public CS5300PROJ1Session(String session, String m, String l) {
		this(session, m, (new Date()).getTime() + CS5300PROJ1Servlet.EXPIRY_TIME_FROM_CURRENT
				, l);
	}
	
	public CS5300PROJ1Session(String s) {
		super(COOKIE_NAME, s);
		String[] args = s.split(";");
		sessionId = args[0];
		setVersion(Integer.parseInt(args[1]));
		message = args[2];
		end = Long.parseLong(args[3]);
		location = args[4];
	}
	
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
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
	

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public void incrementVersion() {
		this.setVersion(this.getVersion() + 1);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sessionId).append("_");
		sb.append(getVersion()).append("_");
		sb.append(end).append("_");
		sb.append(location.toString()).append("_");
		sb.append(message);
		return sb.toString().substring(0, 512);
	}
}
