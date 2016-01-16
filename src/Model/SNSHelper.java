package Model;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

public class SNSHelper {

	public SNSHelper() {
	}
	
	private AmazonSNSClient sns = null;
	private AWSCredentials credentials = null;

	/**
	 * Gets Amazon credentials and SNS client if not available already
	 */
	private void getCredentials() {
		try {
			if (credentials == null) {
				credentials = new ProfileCredentialsProvider("default").getCredentials();
			}
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials", e);
		}
		if (credentials != null && sns == null) {
			sns = new AmazonSNSClient(credentials);
			sns.setRegion(Region.getRegion(Regions.US_EAST_1));			
		}
	}
	
	
	/**
	 * Creates a new SNS Topic or return the existing Topic ARN
	 * 
	 * @param topicName
	 *            Name of the SNS Topic to be created
	 * @return ARN of existing or newly created SNS Topic
	 */
	public String createTopic(String topicName) {
		// Initialize credential and SNS client if not already exists
		getCredentials();
		String newTopicARN = "";
		if (credentials != null && sns != null) {
			try {
				// checking if queue Topic already exists
				for (Topic TopicARN : sns.listTopics().getTopics()) {
					if ( TopicARN.getTopicArn().endsWith(topicName)) {
						return TopicARN.getTopicArn();
					}
				}
				// Creating new topic
				CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
				CreateTopicResult createTopicResult = sns.createTopic(createTopicRequest);
				newTopicARN = createTopicResult.getTopicArn();
			} catch (AmazonServiceException ase) {
				System.out.println("AmazonServiceException: creating topic" + "\nError Message: " + ase.getMessage()
						+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
						+ "\nRequest ID: " + ase.getRequestId());
			} catch (AmazonClientException ace) {
				System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
			}
		}
		return newTopicARN;
	}
	
	/**
	 * Sends a new message to specified SNS Topic 
	 * 
	 * @param topicName
	 *            Name of SNS Topic to send message to
	 * @param message
	 *            String message to be sent to Topic
	 * @return Message id of message sent to this SNS Topic
	 */
	public String publishToTopic(String topicName, String message) {
		String topicARN = createTopic(topicName);
		System.out.println("publishing at" + topicARN);
		try {
			if (sns != null) {
				
				PublishRequest publishRequest = new PublishRequest(topicARN, message);
				PublishResult publishResult = sns.publish(publishRequest);
				return publishResult.getMessageId();
			}
		} catch (AmazonServiceException ase) {
			System.out.println("AmazonServiceException: publishing module" + "\nError Message: " + ase.getMessage()
					+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
					+ "\nRequest ID: " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
		}
		return "";
	}
	
	/**
	 * @param topicName name of SNS Topic to subscribe to
	 * @param protocool protocol The protocol you want to use eg. http, https, email, email-json, sms, sqs, application,
	 * @param endPoint The Endpoint that you want to receive notifications. eg. "http://", "https://", email address, email address, phone number, ARN of an Amazon SQS queue, EndpointArn
	 */
	public void subscribeToTopic(String topicName, String protocool, String endPoint){
		String topicARN = createTopic(topicName);
		try {
			if (sns != null) {
				SubscribeRequest subRequest = new SubscribeRequest (topicARN, protocool, endPoint);
				sns.subscribe(subRequest);
			}
		} catch (AmazonServiceException ase) {
			System.out.println("AmazonServiceException: " + "\nError Message: " + ase.getMessage()
					+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
					+ "\nRequest ID: " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
		}
	}
	
	
	/**
	 * Delete a SNS Topic
	 * @param topicName Name of the SNS Topic to be deleted  
	 */
	public void deleteTopic(String topicName){
		String topicARN = createTopic(topicName);
		try {
			if (sns != null) {
				DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(topicARN);
				sns.deleteTopic(deleteTopicRequest);
			}
		} catch (AmazonServiceException ase) {
			System.out.println("AmazonServiceException: " + "\nError Message: " + ase.getMessage()
					+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
					+ "\nRequest ID: " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
		}
	}
	
	
	/**
	 * Confirms subscription to a specific topic
	 * @param topicARN ARN of the topic to confirm subscribe to 
	 * @param Token Token passed in Subscribe request
	 */
	public void confirmSubscription(String topicARN, String Token){
		getCredentials();
		if(sns != null){
			ConfirmSubscriptionRequest confirmReq = new ConfirmSubscriptionRequest()
	                .withTopicArn(topicARN)
	                .withToken(Token);
	        sns.confirmSubscription(confirmReq);	
		}		
	}	
}
