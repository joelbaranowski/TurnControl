package request;

public class TurnFinished {
	
	private int playerID;
	private int newScore;
	
	public TurnFinished(){
		
	}
	
	public TurnFinished(int playerID, int newScore){
		this.playerID = playerID;
		this.newScore = newScore;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getNewScore() {
		return newScore;
	}

	public void setNewScore(int newScore) {
		this.newScore = newScore;
	}
	
}
