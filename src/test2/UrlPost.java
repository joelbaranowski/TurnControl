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
public class UrlPost {

	Gson g = new Gson();
	
	public String run(MethodWrapper mw, String url){
		MakePost mp = new MakePost(url);
		try {
			return mp.execute(mw);
	    } 
		catch (Exception exception) {
			exception.printStackTrace();
	    }
		return "no return";
	}
}
