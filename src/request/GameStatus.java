package request;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class GameStatus {

	@Persistent
	@PrimaryKey
	private Long gameID;
	
	@Persistent
	private Boolean isStarted;
	
	public GameStatus(){
		
	}
	
	public GameStatus(Long gameID, Boolean isStarted){
		this.gameID = gameID;
		this.isStarted = isStarted;
	}

	public Long getGameID() {
		return gameID;
	}

	public void setGameID(Long gameID) {
		this.gameID = gameID;
	}

	public Boolean getIsStarted() {
		return isStarted;
	}

	public void setIsStarted(Boolean isStarted) {
		this.isStarted = isStarted;
	}
}
