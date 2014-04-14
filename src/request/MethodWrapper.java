package request;

public class MethodWrapper {

	private String method;
	private String data;
	
	public MethodWrapper(){
		
	}
	
	public MethodWrapper(String method, String data){
		this.method = method;
		this.data = data;
	}
	
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
