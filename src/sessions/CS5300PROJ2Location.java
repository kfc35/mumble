package sessions;

public class CS5300PROJ2Location {
	private String primaryIP;
	private String primaryPort;
	private String backupIP;
	private String backupPort;
	
	public CS5300PROJ2Location(String pIP, String pPort, String bIP, String bPort) {
		primaryIP = pIP;
		primaryPort = pPort;
		backupIP = bIP;
		backupPort = bPort;
	}

	public String getPrimaryIP() {
		return primaryIP;
	}

	public void setPrimaryIP(String primaryIP) {
		this.primaryIP = primaryIP;
	}

	public String getPrimaryPort() {
		return primaryPort;
	}

	public void setPrimaryPort(String primaryPort) {
		this.primaryPort = primaryPort;
	}

	public String getBackupIP() {
		return backupIP;
	}

	public void setBackupIP(String backupIP) {
		this.backupIP = backupIP;
	}

	public String getBackupPort() {
		return backupPort;
	}

	public void setBackupPort(String backupPort) {
		this.backupPort = backupPort;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(primaryIP).append("_");
		sb.append(primaryPort).append("_");
		sb.append(backupIP).append("_");
		sb.append(backupPort);
		return sb.toString();
	}
	
}
