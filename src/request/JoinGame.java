package request;

import java.io.Serializable;

public class JoinGame implements Serializable {

	private int playerID;
	private String gameURL;
	
	public JoinGame(){
		
	}
	
	public JoinGame(int playerID, String gameURL){
		this.playerID = playerID;
		this.gameURL = gameURL;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public String getGameURL() {
		return gameURL;
	}

	public void setGameURL(String gameURL) {
		this.gameURL = gameURL;
	}
	
	public String toJson(){
		return "";
	}
}
