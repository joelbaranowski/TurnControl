package Json;

public class GetGameURLFromPortal {

	private Long playerID;
	private Long portalID;
	
	public GetGameURLFromPortal(){
		
	}
	
	public GetGameURLFromPortal(Long portalID, Long playerID){
		this.portalID = portalID;
		this.playerID = playerID;
	}

	public Long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(Long playerID) {
		this.playerID = playerID;
	}

	public Long getPortalID() {
		return portalID;
	}

	public void setPortalID(Long portalID) {
		this.portalID = portalID;
	}
}
