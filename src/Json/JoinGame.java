package Json;

import java.io.Serializable;

public class JoinGame implements Serializable {

	public String playerName;
	public String gameURL;
	public boolean isAI;
	
	public JoinGame(){
		
	}
	
	public JoinGame(String playerName, boolean isAI, String gameURL){
		this.setPlayerName(playerName);
		this.gameURL = gameURL;
		this.isAI = isAI;
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
