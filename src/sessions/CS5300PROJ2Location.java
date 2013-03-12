package sessions;

public class CS5300PROJ2Location {
	private CS5300PROJ2IPP primaryIPP;
	private CS5300PROJ2IPP backupIPP;

	public CS5300PROJ2Location(CS5300PROJ2IPP primaryIPP,
			CS5300PROJ2IPP backupIPP) {
		super();
		this.primaryIPP = primaryIPP;
		this.backupIPP = backupIPP;
	}

	public CS5300PROJ2Location(CS5300PROJ2IPP primaryIPP) {
		this(primaryIPP, null);
	}

	public CS5300PROJ2Location(String s) {
		String[] args = s.split("~");
		primaryIPP = new CS5300PROJ2IPP(args[0]);
		if (args[1].toLowerCase().equals("null")) {
			backupIPP = null;
		} else {
			backupIPP = new CS5300PROJ2IPP(args[1]);
		}
	}

	public CS5300PROJ2IPP getPrimaryIPP() {
		return primaryIPP;
	}

	public void setPrimaryIPP(CS5300PROJ2IPP primaryIPP) {
		this.primaryIPP = primaryIPP;
	}

	public CS5300PROJ2IPP getBackupIPP() {
		return backupIPP;
	}
	
	public boolean hasBackupIPP() {
		return backupIPP == null;
	}

	public void setBackupIPP(CS5300PROJ2IPP backupIPP) {
		this.backupIPP = backupIPP;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(primaryIPP.toString()).append("~");
		if (backupIPP == null) {
			sb.append("null");
		} else {
			sb.append(backupIPP.toString());
		}
		return sb.toString();
	}
	
	public boolean equalsPrimary(CS5300PROJ2IPP o) {
		return primaryIPP.equals(o);
	}
	
	public boolean equalsBackup(CS5300PROJ2IPP o) {
		return backupIPP != null && backupIPP.equals(o);
	}
	
	public boolean equalsEither(CS5300PROJ2IPP o) {
		return equalsPrimary(o) || equalsBackup(o);
	}

}
