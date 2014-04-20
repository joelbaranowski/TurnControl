package request;

import java.io.Serializable;

public class JoinGame implements Serializable {

	private String playerName;
	private String gameURL;
	
	public JoinGame(){
		
	}
	
	public JoinGame(String playerName, String gameURL){
		this.setPlayerName(playerName);
		this.gameURL = gameURL;
	}

	

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getGameURL() {
		return gameURL;
	}

	public void setGameURL(String gameURL) {
		this.gameURL = gameURL;
	}
}
