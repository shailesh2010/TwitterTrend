package Model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseLayer {
	private connect C = null;
	private Statement S = null;
	
	public DatabaseLayer() {
	    C = new connect();
	    if (C.ErrorFlag) {
	    	S = C.getQuery();	
		}	
	}
	
	public ArrayList<TweetModel> GetAllTweets(){
		ArrayList<TweetModel> AllTweets = new ArrayList<>();
		try {
			ResultSet Result = null;
			Result = S.executeQuery("SELECT * FROM TwitterTweets");								
			while (Result.next()) {
				TweetModel T = new TweetModel();
				T.setTweetId(Result.getLong("TweetId"));
				T.setUserID(Result.getLong("UserID"));
				T.setUserName(Result.getString("UserName"));
				T.setTweetText(Result.getString("TweetText"));
				T.setLatitude(Result.getDouble("Latitude"));
				T.setLongitude(Result.getDouble("Longitude"));
				T.setHashTags(Result.getString("HashTags"));
				AllTweets.add(T);
			}			
		} catch (SQLException e) {
			C.closeConnection();	
			return null;
		}
		return AllTweets;
	}

	public ArrayList<TweetModel> GetLikeTweets(String Query){
		ArrayList<TweetModel> AllTweets = new ArrayList<>();
		try {
			ResultSet Result = null;
			Result = S.executeQuery("SELECT * FROM TwitterTweets WHERE TweetText like '%" + Query + "%' OR HashTags like '%" + Query + "%'");								
			while (Result.next()) {
				TweetModel T = new TweetModel();
				T.setTweetId(Result.getLong("TweetId"));
				T.setUserID(Result.getLong("UserID"));
				T.setUserName(Result.getString("UserName"));
				T.setTweetText(Result.getString("TweetText"));
				T.setLatitude(Result.getDouble("Latitude"));
				T.setLongitude(Result.getDouble("Longitude"));
				T.setHashTags(Result.getString("HashTags"));
				AllTweets.add(T);
			}			
		} catch (SQLException e) {
			C.closeConnection();	
			return null;
		}
		return AllTweets;
	}	
	
	public void InsertTweets(ArrayList<TweetModel> Tweets){
		for (TweetModel T : Tweets) {
			String HashTags = "";
			if (T.getHashTags() == "") {
				HashTags = "null";
			} else {
				HashTags = "'" + T.getHashTags().replace("'", "''") + "'";
			}
			String query = "";
			try {
				query = "INSERT INTO TwitterTweets (TweetId,UserID,UserName,TweetText,Latitude,Longitude,HashTags) VALUES ("
						+ T.getTweetId() + ","
						+ T.getUserID() + ",'"
						+ T.getUserName().replace("'", "''")+ "','"
						+ T.getTweetText().replace("'", "''") + "',"
						+ T.getLatitude() + ","
						+ T.getLongitude() + ","
						+ HashTags + ")";				
				S.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}	
	
	public void CloseDatabase(){
		C.closeConnection();
	}
}
