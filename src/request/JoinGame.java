package request;

import java.io.Serializable;

public class JoinGame implements Serializable {

	private Long playerID;
	private String gameURL;
	
	public JoinGame(){
		
	}
	
	public JoinGame(Long playerID, String gameURL){
		this.playerID = playerID;
		this.gameURL = gameURL;
	}

	public Long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(Long playerID) {
		this.playerID = playerID;
	}

	public String getGameURL() {
		return gameURL;
	}

	public void setGameURL(String gameURL) {
		this.gameURL = gameURL;
	}
}
