package test2;

import javax.servlet.http.HttpServletResponse;

import request.JoinGame;
import request.MakePost;
import request.MethodWrapper;
import request.TakeTurn;

import com.google.gson.Gson;

/**
 * @author Joel Baranowski
 * 
 */
public class TestPost {

	Gson g = new Gson();
	
	public String run(MethodWrapper mw){
		MakePost mp = new MakePost("http://1-dot-utopian-hearth-533.appspot.com/test");
		try {
			return mp.execute(mw);
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }
		return "no return";
	}
}
