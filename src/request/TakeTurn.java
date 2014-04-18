package request;

public class TakeTurn {

	private int playerID;
	private int currentScore;
	
	public TakeTurn(){
		
	}
	
	public TakeTurn(int playerID, int currentScore){
		this.playerID = playerID;
		this.currentScore = currentScore;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getCurrentScore() {
		return currentScore;
	}

	public void setCurrentScore(int currentScore) {
		this.currentScore = currentScore;
	}
}
