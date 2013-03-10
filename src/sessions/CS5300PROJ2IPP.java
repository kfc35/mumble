package sessions;

public class CS5300PROJ2IPP {
	private String IP;
	private String port;
	
	public CS5300PROJ2IPP(String IP, String port) {
		super();
		this.IP = IP;
		this.port = port;
	}
	
	public String getIP() {
		return IP;
	}
	public void setIP(String iP) {
		IP = iP;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(IP).append("_");
		sb.append(port);
		return sb.toString();
	}
}
