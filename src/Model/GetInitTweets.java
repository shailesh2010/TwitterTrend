package Model;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import netscape.javascript.JSObject;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;

/**
 * Servlet implementation class GetInitTweets
 */
//@WebServlet("/GetInitTweets")
public class GetInitTweets extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetInitTweets() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DatabaseLayer D =  new DatabaseLayer();		
		String intiflag = request.getParameter("initval");
		ArrayList<TweetModel> NewTweets = null;
		if (intiflag != null){
			if (intiflag.equals("yes")) {
				NewTweets = D.GetAllTweets();	
			} else if (intiflag.equals("no")){
				String query = request.getParameter("queryval");
				if (query != null) {
					if (query.equals("All")) {
						NewTweets = D.GetAllTweets();
					} else {
						NewTweets = D.GetLikeTweets(query);		
					}
				}				
			} else if(intiflag.equals("live")){
				String query = request.getParameter("queryval");
				if (query != null) {
					if (query.equals("All")) {
						NewTweets = (ArrayList<TweetModel>) TweetGet.NewTweets.clone();						
					} else {
						NewTweets = new ArrayList<>();
						for (TweetModel tweetModel : TweetGet.NewTweets) {
							if (tweetModel.getTweetText().contains(query)) {
								NewTweets.add(tweetModel);
							} else if(tweetModel.getTweetText().contains(query)){
								NewTweets.add(tweetModel);
							}
						}	
					}
				}
			}
			if (NewTweets != null && NewTweets.size() != 0) {
				Gson gson=new Gson();
		        JsonElement element= gson.toJsonTree(NewTweets, new TypeToken<ArrayList<TweetModel>>() {}.getType());
		        JsonArray jsonArray = element.getAsJsonArray();
		        response.setContentType("application/json");
		        response.getWriter().print(jsonArray);	
			} else {
				response.getWriter().write("No Data");
			}
		}
		D.CloseDatabase();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
