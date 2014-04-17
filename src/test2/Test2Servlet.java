package test2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.datanucleus.store.types.sco.simple.Collection;

import request.ExceptionStringify;
import request.GameStatus;
import request.JoinGame;
import request.MethodWrapper;
import request.RegisterGame;
import request.TakeTurn;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("serial")
public class Test2Servlet extends HttpServlet {
	
	private Gson g = new Gson();
	private MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	
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
		PersistenceManager pm = pmf.getPersistenceManager();
		switch(method){
			case "joinGame":{
				JoinGame jg = (JoinGame) g.fromJson(data, JoinGame.class);
				
				Transaction tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    pm.makePersistent(jg);
				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close(  );
			    }
				
				resp.getWriter().println("{'return':'player added'}");
				break;
			}
			case "turnFinished":{
				try{
				GameStatus gs = new GameStatus(0L, false);
				Transaction tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    Query query = pm.newQuery(GameStatus.class);
				    
				    Collection result =  (Collection)query.execute();
				    gs = (GameStatus)result.iterator().next();
				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println("es: " + es.run());
				}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close();
			    }
					
				if(!gs.getIsStarted())
					break;
				
				TakeTurn tf = (TakeTurn) g.fromJson(data, TakeTurn.class);
				long currPlayer = tf.getPlayerID();
				int newScore = tf.getCurrentScore();
					
				tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    pm.makePersistent(tf);
				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close(  );
			    }
				
				
				long newPlayer = currPlayer + 1;
				String gameURL = "";
				
				List<JoinGame> value = new ArrayList<JoinGame>();
				tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    Query query = pm.newQuery(JoinGame.class);
				    value =  (List<JoinGame>)query.execute();

				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println("es: " + es.run());
				}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close();
			    }
				
				int numberOfPlayers = value.size();
				if(newPlayer >= numberOfPlayers)
					newPlayer = 0;
				
				for(JoinGame jog : value){
					if(jog.getPlayerID() == newPlayer)
						gameURL = jog.getGameURL();
				}
				
				int newPlayerScore = -1;
				tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    Query query = pm.newQuery(TakeTurn.class, "id == " + newPlayer);
				    Collection c = (Collection)query.execute();
				    newPlayerScore = (int)c.iterator().next();
				    
				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println("es: " + es.run());
				}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close();
			    }
				
				TakeTurn tt = new TakeTurn(newPlayer, newPlayerScore);
				
				String gtj = g.toJson(tt);
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TakeTurnPost ttp = new TakeTurnPost();
				ttp.run(mew, gameURL);
				break;
				}
				catch(Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println(es.run());
					return;
				}
			}
			case "startGame":{
				GameStatus gs = new GameStatus(0L, true);
				Transaction tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    pm.makePersistent(gs);
				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close();
			    }
				
				pm = pmf.getPersistenceManager();
				List<JoinGame> value = new ArrayList<JoinGame>();
				tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    Query query = pm.newQuery(JoinGame.class);
				    value =  (List<JoinGame>)query.execute();

				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println("es: " + es.run());
				}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close();
			    }
				
				pm = pmf.getPersistenceManager();
				String player0GameUrl = "";
				for(JoinGame jog : value){
					long currentPlayer = jog.getPlayerID();
					String gameURL = jog.getGameURL();
					TakeTurn turf = new TakeTurn(currentPlayer, 0);
					tx = pm.currentTransaction();
					try
					{
					    // Start the transaction
					    tx.begin();
					    pm.makePersistent(turf);
					    // Commit the transaction, flushing the object to the datastore
					    tx.commit();
					}
					catch (Exception e){}
					finally {
				        if( tx.isActive(  ) ) {
				            tx.rollback(  );
				        }
				    }
					if(currentPlayer == 0)
						player0GameUrl = gameURL;
				}
				
				if(!pm.isClosed())
					pm.close();
				TakeTurn tt = new TakeTurn(0, 0);
				String gtj = g.toJson(tt);
				MethodWrapper mew = new MethodWrapper("takeTurn", gtj);
				TakeTurnPost ttp = new TakeTurnPost();
				ttp.run(mew, player0GameUrl);
				resp.getWriter().println("{'return':'started game and sent take turn to player 0'}");
				break;
			}
			case "endGame":{
				GameStatus gs = new GameStatus(0L, false);
				Transaction tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    pm.makePersistent(gs);
				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close(  );
			    }
				resp.getWriter().println("{'return':'ended game'}");
				break;
			}
			case "registerGame":{
				RegisterGame rg = (RegisterGame) g.fromJson(data, RegisterGame.class);
				
				Transaction tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    pm.makePersistent(rg);
				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close(  );
			    }
				
				resp.getWriter().println("Added game");
				break;
			}
			case "getGameList":{
				List<RegisterGame> value = new ArrayList<RegisterGame>();
				Transaction tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    Query query = pm.newQuery(RegisterGame.class);
				    value =  (List<RegisterGame>)query.execute();

				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println("es: " + es.run());
				}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close();
			    }
				
				String ret = g.toJson(value);
				resp.getWriter().println(ret);
				break;
			}
			case "getPlayerList":{
				List<JoinGame> value = new ArrayList<JoinGame>();
				Transaction tx = pm.currentTransaction();
				try
				{
				    // Start the transaction
				    tx.begin();
				    Query query = pm.newQuery(JoinGame.class);
				    value =  (List<JoinGame>)query.execute();

				    // Commit the transaction, flushing the object to the datastore
				    tx.commit();
				}
				catch (Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println("es: " + es.run());
				}
				finally {
			        if( tx.isActive(  ) ) {
			            tx.rollback(  );
			        }
			        pm.close();
			    }
				try{
				ArrayList<String> playerResults = new ArrayList<String>();
				for(JoinGame jog : value){
					long playerID = jog.getPlayerID();
					String result = "Game has not started";
					if(syncCache.get("player" + playerID) == null)
						result = (String)syncCache.get("player" + playerID);
					playerResults.add(playerID + ": " + result);
				}
				resp.getWriter().println(g.toJson(playerResults));
				}
				catch(Exception e){
					ExceptionStringify es = new ExceptionStringify(e);
					resp.getWriter().println("es: " + es.run());
				}
				break;
			}
			case "deleteGameList":{
				deleteGames(pm, resp);
				resp.getWriter().println("Deleted gameList");
				break;
			}
			case "deletePlayers":{
				deletePlayers(pm, resp);
				resp.getWriter().println("{'result':'Deleted players'}");
				break;
			}
			case "init":{
				deletePlayers(pm, resp);
				deleteGames(pm, resp);
				resp.getWriter().println("{'result':'init'}");
				break;
			}
		//end of switch
		}
	//end of method
	}
	
	public void deletePlayers(PersistenceManager pm, HttpServletResponse resp) throws IOException{
		try{
		List<JoinGame> value = new ArrayList<JoinGame>();
		Transaction tx = pm.currentTransaction();
		try
		{
		    // Start the transaction
		    tx.begin();
		    Query query = pm.newQuery(JoinGame.class);
		    value =  (List<JoinGame>)query.execute();
		    pm.deletePersistentAll(value);
		    // Commit the transaction, flushing the object to the datastore
		    tx.commit();
		}
		catch (Exception e){

		}
		finally {
	        if( tx.isActive(  ) ) {
	            tx.rollback(  );
	        }
	        pm.close();
	    }
		
		if(value == null){
			return;
		}

		pm = pmf.getPersistenceManager();
		List<TakeTurn> tt = new ArrayList<TakeTurn>();
		tx = pm.currentTransaction();
		try
		{
		    // Start the transaction
		    tx.begin();
		    Query query = pm.newQuery(TakeTurn.class);
		    tt =  (List<TakeTurn>)query.execute();
		    pm.deletePersistentAll(tt);
		    // Commit the transaction, flushing the object to the datastore
		    tx.commit();
		}
		catch (Exception e){
		}
		finally {
	        if( tx.isActive(  ) ) {
	            tx.rollback(  );
	        }
	        pm.close();
	    }
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().println(es.run());
		}
	}
	
	public void deleteGames(PersistenceManager pm, HttpServletResponse resp) throws IOException{
		try{
		List<RegisterGame> value = new ArrayList<RegisterGame>();
		Transaction tx = pm.currentTransaction();
		try
		{
		    // Start the transaction
		    tx.begin();
		    Query query = pm.newQuery(RegisterGame.class);
		    value =  (List<RegisterGame>)query.execute();
		    pm.deletePersistentAll(value);
		    // Commit the transaction, flushing the object to the datastore
		    tx.commit();
		}
		catch (Exception e){
		}
		}
		catch(Exception e){
			ExceptionStringify es = new ExceptionStringify(e);
			resp.getWriter().println(es.run());
		}
	}
	
//end of class
}
