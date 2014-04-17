package request;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class TakeTurn {

	@Persistent
	@PrimaryKey
	private long playerID;
	
	@Persistent
	private int currentScore;
	
	public TakeTurn(){
		
	}
	
	public TakeTurn(long playerID, int currentScore){
		this.playerID = playerID;
		this.currentScore = currentScore;
	}

	public long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(long playerID) {
		this.playerID = playerID;
	}

	public int getCurrentScore() {
		return currentScore;
	}

	public void setCurrentScore(int currentScore) {
		this.currentScore = currentScore;
	}
}
