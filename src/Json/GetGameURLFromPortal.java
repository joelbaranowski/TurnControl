package Json;

public class GetGameURLFromPortal {

	private Long portalID;
	
	public GetGameURLFromPortal(){
		
	}
	
	public GetGameURLFromPortal(Long portalID){
		this.portalID = portalID;
	}

	public Long getPortalID() {
		return portalID;
	}

	public void setPortalID(Long portalID) {
		this.portalID = portalID;
	}
}
