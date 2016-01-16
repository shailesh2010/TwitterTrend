package Model;

public class TweetModel {
	
	String UserName;
	String TweetText;
	String HashTags;
	Double Latitude;
	Double Longitude;
	Long TweetId;
	Long UserID;
	
	public TweetModel() {
		super();		
	}

	public TweetModel(String userName, String tweetText, String hashTags, Double latitude, Double longitude, Long tweetId,
			Long userID) {
		super();
		UserName = userName;
		TweetText = tweetText;
		HashTags = hashTags;
		Latitude = latitude;
		Longitude = longitude;
		TweetId = tweetId;
		UserID = userID;
	}

	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getTweetText() {
		return TweetText;
	}
	public void setTweetText(String tweetText) {
		TweetText = tweetText;
	}
	public String getHashTags() {
		return HashTags;
	}
	public void setHashTags(String hashTags) {
		HashTags = hashTags;
	}
	public Double getLatitude() {
		return Latitude;
	}
	public void setLatitude(Double latitude) {
		Latitude = latitude;
	}
	public Double getLongitude() {
		return Longitude;
	}
	public void setLongitude(Double longitude) {
		Longitude = longitude;
	}
	public Long getTweetId() {
		return TweetId;
	}
	public void setTweetId(Long tweetId) {
		TweetId = tweetId;
	}
	public Long getUserID() {
		return UserID;
	}
	public void setUserID(Long userID) {
		UserID = userID;
	}


}
