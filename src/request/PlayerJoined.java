package request;

public class PlayerJoined {

	private Long playerID;
	private String playerName;
	
	public PlayerJoined(){
		
	}
	
	public PlayerJoined(Long playerID, String playerName){
		this.playerID = playerID;
		this.playerName = playerName;
	}

	public Long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(Long playerID) {
		this.playerID = playerID;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
