package Json;

public class GetGameURLFromPortalResponse {

	private String status;
	private String gameURL;
	private Long inboundPortalID;
	private Long playerID;
	
	public GetGameURLFromPortalResponse(){
		
	}
	
	public GetGameURLFromPortalResponse(String status, String gameURL,
			Long inboundPortalID, Long playerID) {
		this.status = status;
		this.gameURL = gameURL;
		this.inboundPortalID = inboundPortalID;
		this.playerID = playerID;
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

	public Long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(Long playerID) {
		this.playerID = playerID;
	}
	
}
