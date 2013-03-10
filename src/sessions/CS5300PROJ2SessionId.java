package sessions;

public class CS5300PROJ2SessionId {
	
	private String sessionId;
	private String originIP;
	private String originPort; //UDP port
	
	public CS5300PROJ2SessionId(String sId, String oIP, String oPort) {
		sessionId = sId;
		originIP = oIP;
		originPort = oPort;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getOriginIP() {
		return originIP;
	}

	public void setOriginIP(String originIP) {
		this.originIP = originIP;
	}

	public String getOriginPort() {
		return originPort;
	}

	public void setOriginPort(String originPort) {
		this.originPort = originPort;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sessionId).append("_");
		sb.append(originIP).append("_");
		sb.append(originPort);
		return sb.toString();
	}
	
	

}
