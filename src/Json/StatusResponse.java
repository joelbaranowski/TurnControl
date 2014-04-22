package Json;

import com.google.gson.Gson;

public class StatusResponse {

	public String status;
	public String msg;
	
	public StatusResponse(String statusIn, String msgIn) {
		this.status = statusIn;
		this.msg = msgIn;
	}
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this, StatusResponse.class);
	}
}
