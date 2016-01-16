package Model;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;

import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;



public class TweetGet extends HttpServlet {
	public static ArrayList<TweetModel> NewTweets = new ArrayList<>();
	public static int tweetssofar =  0; 
	public static SQSHelper sqsHelperAlchemy = new SQSHelper();
	public static SQSHelper sqsHelperDatabase = new SQSHelper();
	//public static SQSHelper sqsHelperAlchemy = new SQSHelper();
	
	public static SNSHelper snsHelperNewTweets =  new SNSHelper();
	
	public void init() throws ServletException{	
		try {
			CollectTweets();
		} catch (TwitterException e) {			
		}
	}
	
	public static void CollectTweets() throws TwitterException {   	
    	 ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true)
           .setOAuthConsumerKey("consumerkey")
           .setOAuthConsumerSecret("ConsumerSecret")
           .setOAuthAccessToken("AccessToken")
           .setOAuthAccessTokenSecret("AccessTokenSecret");
          
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
            	if(status.getGeoLocation() != null && status.getGeoLocation().getLatitude() != 0.0 && status.getGeoLocation().getLongitude() != 0.0){
            		TweetModel T = new TweetModel();
            		T.setTweetId(status.getId());
            		T.setUserID(status.getUser().getId());
            		T.setUserName(status.getUser().getName());
            		T.setTweetText(status.getText());
            		T.setLatitude(status.getGeoLocation().getLatitude());
            		T.setLongitude(status.getGeoLocation().getLongitude());
          		
            		String tags = ""; 
            		for (HashtagEntity tag : status.getHashtagEntities()) {
            			tags += "#" + tag.getText();
					}
            		T.setHashTags(tags);            		
            		NewTweets.add(T);
            		tweetssofar++;
            		
            		Gson gson=new Gson();
    		        JsonElement element= gson.toJsonTree(T);    		        
    		        sqsHelperAlchemy.sendNewMessage("AlchemyNewTweets",element.toString());
    		        sqsHelperDatabase.sendNewMessage("DatabaseNewTweets",element.toString());
    		        snsHelperNewTweets.publishToTopic("NewTweets", "got new tweet");
    		        
//    		        if (NewTweets.size() > 2) {
//    		        	List<Message> messages = sqsHelper.getAllMessages("Ashish");
//    		        	System.out.println("Getting tweets from queue");
//    		          for (Message message : messages) {    		          	
//    		          	System.out.println("  Message");
//    		              System.out.println("    MessageId:     " + message.getMessageId());
//    		              System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
//    		              System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
//    		              System.out.println("    Body:          " + message.getBody());
//    		              for (Entry<String, String> entry : message.getAttributes().entrySet()) {
//    		                  System.out.println("  Attribute");
//    		                  System.out.println("    Name:  " + entry.getKey());
//    		                  System.out.println("    Value: " + entry.getValue());
//    		              }
//    		          }
//    		        }    		        
    		        
            		System.out.println("Got Tweet: " + tweetssofar);
            		System.out.println("Tweet Details: User Name: " + T.getUserName() + " TweetText: " + T.getTweetText());
            		if (NewTweets.size() > 2) {
						DatabaseLayer D =  new DatabaseLayer();
						D.InsertTweets(NewTweets);
						D.CloseDatabase();
						NewTweets.clear();
					}
            	}               
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub
				
			}
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
    }    
    
}