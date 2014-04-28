package Json;

public class GetGameURLFromPortalResponse {

	private String status;
	private String gameURL;
	private Long inboundPortalID;
	
	public GetGameURLFromPortalResponse(){
		
	}
	
	public GetGameURLFromPortalResponse(String status, String gameURL,
			Long inboundPortalID) {
		this.status = status;
		this.gameURL = gameURL;
		this.inboundPortalID = inboundPortalID;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGameURL() {
		return gameURL;
	}

	public void setGameURL(String gameURL) {
		this.gameURL = gameURL;
	}

	public Long getInboundPortalID() {
		return inboundPortalID;
	}

	public void setInboundPortalID(Long inboundPortalID) {
		this.inboundPortalID = inboundPortalID;
	}
	
}
