package sessions;

public class CS5300PROJ2Location {
	private CS5300PROJ2IPP primaryIPP;
	private CS5300PROJ2IPP backupIPP;

	public CS5300PROJ2Location(CS5300PROJ2IPP primaryIPP,
			CS5300PROJ2IPP backupIPP) {
		super();

		this.primaryIPP = primaryIPP;
		this.backupIPP = backupIPP;
		checkValidIPPs();
	}

	public CS5300PROJ2Location(CS5300PROJ2IPP primaryIPP) {
		this(primaryIPP, null);
	}

	public CS5300PROJ2Location(String s) {
		String[] args = s.split("~");
		try {
			primaryIPP = new CS5300PROJ2IPP(args[0]);
		} catch (Exception e) {
			primaryIPP = null;
		}
		try {
			if (args[1].toLowerCase().equals("null")) {
				backupIPP = null;
			} else {
				backupIPP = new CS5300PROJ2IPP(args[1]);
			}
		} catch (Exception e) {
			backupIPP = null;
		}
		checkValidIPPs();
	}

	public CS5300PROJ2IPP getPrimaryIPP() {
		return primaryIPP;
	}

	public void setPrimaryIPP(CS5300PROJ2IPP primaryIPP) {
		this.primaryIPP = primaryIPP;
		checkValidIPPs();
	}

	public CS5300PROJ2IPP getBackupIPP() {
		return backupIPP;
	}

	public boolean hasBackupIPP() {
		return backupIPP != null;
	}

	public void setBackupIPP(CS5300PROJ2IPP backupIPP) {
		this.backupIPP = backupIPP;
		checkValidIPPs();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (primaryIPP == null) {
			sb.append("null");
		} else {
			sb.append(primaryIPP.toString());
		}
		sb.append("~");
		if (backupIPP == null) {
			sb.append("null");
		} else {
			sb.append(backupIPP.toString());
		}
		return sb.toString();
	}

	public boolean equalsPrimary(CS5300PROJ2IPP o) {
		if (this.primaryIPP == null) {
			return false;
		}
		return primaryIPP.equals(o);
	}

	public boolean equalsBackup(CS5300PROJ2IPP o) {
		return backupIPP != null && backupIPP.equals(o);
	}

	public boolean equalsEither(CS5300PROJ2IPP o) {
		return equalsPrimary(o) || equalsBackup(o);
	}

	// Checks for void IPPs and that the first and second aren't the same
	private void checkValidIPPs() {
		if (this.primaryIPP == null) {
			if (this.backupIPP != null) {
				this.primaryIPP = this.backupIPP;
				this.backupIPP = null;
			}
		}
		if (this.primaryIPP != null && this.primaryIPP.equals(this.backupIPP)) {
			this.backupIPP = null;
		}
	}

}
