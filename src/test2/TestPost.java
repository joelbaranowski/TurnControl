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
	
	public String run(){
		MakePost mp = new MakePost("http://1-dot-utopian-hearth-531.appspot.com/test");
		TakeTurn jg = new TakeTurn(0, 0);
		String jgs = g.toJson(jg);
		MethodWrapper mw = new MethodWrapper("takeTurn", jgs);
		try {
			return mp.execute(mw);
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }
		return "test failed";
	}
}
