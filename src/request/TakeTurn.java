package request;

public class TakeTurn {

	private Long playerID;
	private Long currentScore;
	
	public TakeTurn(){
		
	}
	
	public TakeTurn(Long playerID, Long currentScore){
		this.playerID = playerID;
		this.currentScore = currentScore;
	}

	public Long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(Long playerID) {
		this.playerID = playerID;
	}

	public Long getCurrentScore() {
		return currentScore;
	}

	public void setCurrentScore(Long currentScore) {
		this.currentScore = currentScore;
	}
}
