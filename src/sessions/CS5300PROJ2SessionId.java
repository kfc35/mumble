package sessions;

public class CS5300PROJ2SessionId {
	
	private String sessionId;
	private CS5300PROJ2IPP originIPP;
	
	public CS5300PROJ2SessionId(String sessionId, CS5300PROJ2IPP originIPP) {
		super();
		this.sessionId = sessionId;
		this.originIPP = originIPP;
	}
	
	public CS5300PROJ2SessionId(String s) {
		String args[] = s.split("_");
		sessionId = args[0];
		originIPP = new CS5300PROJ2IPP(args[1], args[2]);
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public CS5300PROJ2IPP getOriginIPP() {
		return originIPP;
	}

	public void setOriginIPP(CS5300PROJ2IPP originIPP) {
		this.originIPP = originIPP;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sessionId).append("_");
		sb.append(originIPP.toString());
		return sb.toString();
	}
	
	

}
