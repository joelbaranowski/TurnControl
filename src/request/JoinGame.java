package request;

import java.io.Serializable;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class JoinGame implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6472887913423444316L;
	@Persistent
	@PrimaryKey
	private Long playerID;
	@Persistent
	private String gameURL;
	
	public JoinGame(){
		
	}
	
	public JoinGame(long playerID, String gameURL){
		this.playerID = playerID;
		this.gameURL = gameURL;
	}

	public long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(long playerID) {
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
