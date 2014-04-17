package request;

public class TurnFinished {
	
	private long playerID;
	private int newScore;
	
	public TurnFinished(){
		
	}
	
	public TurnFinished(long playerID, int newScore){
		this.playerID = playerID;
		this.newScore = newScore;
	}

	public long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(long playerID) {
		this.playerID = playerID;
	}

	public int getNewScore() {
		return newScore;
	}

	public void setNewScore(int newScore) {
		this.newScore = newScore;
	}
	
}
