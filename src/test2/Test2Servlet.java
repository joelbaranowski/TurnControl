package test2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import request.ExceptionStringify;
import request.MethodWrapper;
import servlets.DeleteGameListServlet;
import servlets.DeletePlayerScoresServlet;
import servlets.DeletePlayersServlet;
import servlets.DeleteStatusServlet;
import servlets.EndGameServlet;
import servlets.GetGameListServlet;
import servlets.GetPlayerListServlet;
import servlets.InitServlet;
import servlets.JoinGameServlet;
import servlets.RegisterGameServlet;
import servlets.StartGameServlet;
import servlets.TurnFinishedServlet;
import Json.JoinGame;
import Json.PlayerJoined;
import Json.RegisterGame;
import Json.TakeTurn;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.*;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("serial")
public class Test2Servlet extends HttpServlet {
	
	private Gson g = new Gson();
	private MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String method = req.getParameter("method");
		String data = req.getParameter("data");
		if(method == null || data == null)
			return;
		this.execute(req.getParameter("method"), URLDecoder.decode(req.getParameter("data"), "UTF-8"), req, resp);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		MethodWrapper mw = g.fromJson(req.getReader(), MethodWrapper.class);
		try{
			this.execute(mw.getMethod(), mw.getData(), req, resp);
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().println(es.run());
			return;
		}
		//end of doPost
	}
	
	private void execute(String method, String data, HttpServletRequest req, HttpServletResponse resp) throws IOException{
		switch(method){
			case "joinGame":{
				JoinGameServlet jgs = new JoinGameServlet();
				JoinGame jg = (JoinGame) g.fromJson(data, JoinGame.class);
				//jgs.doModPost(jg, req, resp);
				break;
			}
			case "turnFinished":{
				TurnFinishedServlet tfs = new TurnFinishedServlet();
				TakeTurn tf = (TakeTurn) g.fromJson(data, TakeTurn.class);
				tfs.doModPost(tf, req, resp);
				break;
			}
			case "startGame":{
				StartGameServlet sgs = new StartGameServlet();
				sgs.doGet(req, resp);
				break;
			}
			case "endGame":{
				EndGameServlet egs = new EndGameServlet();
				egs.doPost(req, resp);
				break;
			}
			case "registerGame":{
				RegisterGameServlet rgs = new RegisterGameServlet();
				RegisterGame rg = (RegisterGame) g.fromJson(data, RegisterGame.class);
				rgs.doModPost(rg, req, resp);
				break;
			}
			case "getGameList":{
				GetGameListServlet ggls = new GetGameListServlet();
				ggls.doPost(req, resp);
				break;
			}
			case "getPlayerList":{
				GetPlayerListServlet gpl = new GetPlayerListServlet();
				gpl.doGet(req, resp);
				break;
			}
			case "deleteGameList":{
				DeleteGameListServlet dgls = new DeleteGameListServlet();
				dgls.doPost(req, resp);
				break;
			}
			case "deletePlayers":{
				DeletePlayersServlet dps = new DeletePlayersServlet();
				dps.doPost(req, resp);
				break;
			}
			case "deletePlayerScores":{
				DeletePlayerScoresServlet dpss = new DeletePlayerScoresServlet();
				dpss.doPost(req, resp);
				break;
			}
			case "deleteStatus":{
				DeleteStatusServlet dss = new DeleteStatusServlet();
				dss.doPost(req, resp);
				break;
			}
			case "init":{
				InitServlet is = new InitServlet();
				is.doPost(req, resp);
				break;
			}
		//end of switch
		}
	//end of method
	}
	
//end of class
}
