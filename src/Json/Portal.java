package Json;

public class Portal {

	private Boolean isOutbound;
	
	public Portal(){
		
	}
	
	public Portal(Boolean isOutbound){
		this.isOutbound = isOutbound;
	}

	public Boolean getIsOutbound() {
		return isOutbound;
	}

	public void setIsOutbound(Boolean isOutbound) {
		this.isOutbound = isOutbound;
	}
}
