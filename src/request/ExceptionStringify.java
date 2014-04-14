package request;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionStringify {

	private Exception e;
	
	public ExceptionStringify(Exception e){
		this.e = e;
	}
	
	public String run(){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
