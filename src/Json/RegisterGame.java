package Json;

import java.util.ArrayList;

public class RegisterGame {

	private String url;
	private ArrayList<Portal> portals;
	
	public RegisterGame(){
		
	}
	
	public RegisterGame(String url, ArrayList<Portal> portals){
		this.url = url;
		this.portals = portals;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ArrayList<Portal> getPortals() {
		return portals;
	}

	public void setPortals(ArrayList<Portal> portals) {
		this.portals = portals;
	}

}
