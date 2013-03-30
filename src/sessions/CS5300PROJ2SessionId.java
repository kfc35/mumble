package sessions;

public class CS5300PROJ2SessionId {
	
	private int sessionNum;
	private CS5300PROJ2IPP originIPP;
	
	public CS5300PROJ2SessionId(int sNum, CS5300PROJ2IPP originIPP) {
		super();
		this.sessionNum = sNum;
		this.originIPP = originIPP;
	}
	
	public CS5300PROJ2SessionId(String s) {
		String args[] = s.split("~");
		sessionNum = Integer.parseInt(args[0]);
		args = args[1].split("_");
		originIPP = new CS5300PROJ2IPP(args[0], args[1]);
	}
	

	public int getSessionNum() {
		return sessionNum;
	}

	public void setSessionNum(int sessionNum) {
		this.sessionNum = sessionNum;
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
		sb.append(sessionNum).append("~");
		sb.append(originIPP.toString());
		return sb.toString();
	}
	
	

}
