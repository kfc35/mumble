package sessions;

public class CS5300PROJ2SessionId {
	
	private String uId;
	private CS5300PROJ2IPP originIPP;
	
	public CS5300PROJ2SessionId(String sessionId, CS5300PROJ2IPP originIPP) {
		super();
		this.uId = sessionId;
		this.originIPP = originIPP;
	}
	
	public CS5300PROJ2SessionId(String s) {
		String args[] = s.split("~");
		uId = args[0];
		originIPP = new CS5300PROJ2IPP(args[1], args[2]);
	}
	
	public String getUId() {
		return uId;
	}

	public void setUId(String uId) {
		this.uId = uId;
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
		sb.append(uId).append("~");
		sb.append(originIPP.toString());
		return sb.toString();
	}
	
	

}
