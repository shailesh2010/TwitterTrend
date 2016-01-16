package Model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

public class SQSHelper {

	public SQSHelper() {
		
	}

	private AmazonSQS sqs = null;
	private AWSCredentials credentials = null;

	/**
	 * Gets Amazon credentials and SQS Queue client if not available already
	 */
	private void getCredentials() {
		try {
			if (credentials == null) {
				credentials = new ProfileCredentialsProvider("default").getCredentials();
			}
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials", e);
		}
		if (credentials != null && sqs == null) {
			sqs = new AmazonSQSClient(credentials);
			Region usWest2 = Region.getRegion(Regions.US_WEST_2);
			sqs.setRegion(usWest2);
		}
	}

	/**
	 * Creates a new SQS Queue or return the existing Queue URL
	 * 
	 * @param queueName
	 *            Name of the Queue to be created
	 * @return URL of existing or newly created Queue
	 */
	public String createQueue(String queueName) {
		// Initialize credential and SQS client if not already exists
		getCredentials();
		String newQueueUrl = "";
		if (credentials != null && sqs != null) {
			try {
				// checking if queue queueName already exists
				for (String queueUrl : sqs.listQueues(queueName).getQueueUrls()) {
					if (queueUrl.endsWith(queueName)) {
						return queueUrl;
					}
				}
				// Creating new Queue
				CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
				newQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
			} catch (AmazonServiceException ase) {
				System.out.println("AmazonServiceException: " + "\nError Message: " + ase.getMessage()
						+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
						+ "\nRequest ID: " + ase.getRequestId());
			} catch (AmazonClientException ace) {
				System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
			}
		}
		return newQueueUrl;
	}

	/**
	 * Sends a new message to specified Queue
	 * 
	 * @param queueName
	 *            Name of queue to send message to
	 * @param message
	 *            String message to be sent to queue
	 * @return Message id of message sent to this queue
	 */
	public String sendNewMessage(String queueName, String message) {
		return sendNewMessage(queueName, message, null);
	}

	/**
	 * Sends a new message to specified Queue with message attribute set
	 * 
	 * @param queueName
	 *            Name of queue to send message to
	 * @param message
	 *            String message to be sent to queue
	 * @param messageAttributes
	 *            Attributes in form of Hashmap of string and
	 *            MessageAttributevalue
	 * @return Message id of message sent to this queue
	 */
	public String sendNewMessage(String queueName, String message,
			Map<String, MessageAttributeValue> messageAttributes) {
		String queueUrl = createQueue(queueName);
		SendMessageResult Result = null;
		try {
			if (sqs != null) {
				SendMessageRequest request = new SendMessageRequest();
				request.withMessageBody(message);
				request.withQueueUrl(queueUrl);
				if (messageAttributes != null) {
					request.withMessageAttributes(messageAttributes);
				}
				Result = sqs.sendMessage(request);
				return Result.getMessageId();
			}
		} catch (AmazonServiceException ase) {
			System.out.println("AmazonServiceException: " + "\nError Message: " + ase.getMessage()
					+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
					+ "\nRequest ID: " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
		}
		return "";
	}

	/**
	 * Get all the messages contained in this queue
	 * 
	 * @param queueName
	 *            Name of the queue to get messages from
	 * @return List of all the messages
	 */
	public List<Message> getAllMessages(String queueName) {
		String queueUrl = createQueue(queueName);
		List<Message> polledMessages = null;
		Map<String, Message> Messages = new HashMap<String, Message>();

		try {
			GetQueueAttributesResult result = sqs.getQueueAttributes(queueUrl,
					Arrays.asList("ApproximateNumberOfMessages"));
			int numofmessages = Integer.parseInt(result.getAttributes().get("ApproximateNumberOfMessages"));
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl).withWaitTimeSeconds(20);
			System.out.println("Polling for " + numofmessages + " messages");
			for (; Messages.size() < numofmessages;) {
				polledMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
				for (Message message : polledMessages) {
					if (!Messages.containsKey(message.getMessageId())) {
						Messages.put(message.getMessageId(), message);
					}
				}
			}
		} catch (AmazonServiceException ase) {
			System.out.println("AmazonServiceException: " + "\nError Message: " + ase.getMessage()
					+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
					+ "\nRequest ID: " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
		}
		System.out.println("Number of messages polled: " + Messages.size());
		return new ArrayList<Message>(Messages.values());
	}

	/**
	 * Get all the messages contained in this queue that has matching message
	 * IDs
	 * 
	 * @param queueName
	 *            Name of the queue to get messages from
	 * @param messageID
	 *            String ArrayList of message IDs
	 * @return List of all the messages
	 */
	public List<Message> getMessageByID(String queueName, ArrayList<String> messageID) {
		List<Message> messages = getAllMessages(queueName);
		for (Iterator<Message> messageIterator = messages.iterator(); messageIterator.hasNext();) {
			Message message = messageIterator.next();
			if (!messageID.contains(message.getMessageId())) {
				messages.remove(message);
			}
		}
		return messages;
	}
	
	/**
	 * Delete messages from the given SQS queue
	 * @param queueName Name of the Queue to delete messages from 
	 * @param messages List of messages to be deleted
	 */
	public void deleteMessages(String queueName, List<Message> messages) {
		String queueUrl = createQueue(queueName);
		try {
			if (sqs != null) {
				for (Message message : messages) {
					String messageRecieptHandle = message.getReceiptHandle();
					sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
				}
			}

		} catch (AmazonServiceException ase) {
			System.out.println("AmazonServiceException: " + "\nError Message: " + ase.getMessage()
					+ "\nAWS Error Code: " + ase.getErrorCode() + "\nError Type: " + ase.getErrorType()
					+ "\nRequest ID: " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("AmazonClientException" + "\nError Message: " + ace.getMessage());
		}
	}

	// public static void initQueue() throws Exception {
	//
	// sqs = new AmazonSQSClient(credentials);
	// Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	// sqs.setRegion(usWest2);
	// try {
	//
	// for (String queueUrl : sqs.listQueues().getQueueUrls()) {
	// System.out.println(" QueueUrl: " + queueUrl);
	// }
	// System.out.println();
	//
	//
	// CreateQueueRequest createQueueRequest = new
	// CreateQueueRequest("NewTweets");
	// myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

	// Receive messages
	// System.out.println("Receiving messages from MyQueue.\n");
	// ReceiveMessageRequest receiveMessageRequest = new
	// ReceiveMessageRequest(myQueueUrl);
	// List<Message> messages =
	// sqs.receiveMessage(receiveMessageRequest).getMessages();
	// for (Message message : messages) {
	// System.out.println(" Message");
	// System.out.println(" MessageId: " + message.getMessageId());
	// System.out.println(" ReceiptHandle: " + message.getReceiptHandle());
	// System.out.println(" MD5OfBody: " + message.getMD5OfBody());
	// System.out.println(" Body: " + message.getBody());
	// for (Entry<String, String> entry : message.getAttributes().entrySet()) {
	// System.out.println(" Attribute");
	// System.out.println(" Name: " + entry.getKey());
	// System.out.println(" Value: " + entry.getValue());
	// }
	// }
	// System.out.println();

	// Delete a message
	// System.out.println("Deleting a message.\n");
	// String messageRecieptHandle = messages.get(0).getReceiptHandle();
	// sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl,
	// messageRecieptHandle));
	//
	// // Delete a queue
	// System.out.println("Deleting the test queue.\n");
	// sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
	// } catch (AmazonServiceException ase) {
	// System.out.println("Caught an AmazonServiceException, which means your
	// request made it " +
	// "to Amazon SQS, but was rejected with an error response for some
	// reason.");
	// System.out.println("Error Message: " + ase.getMessage());
	// System.out.println("HTTP Status Code: " + ase.getStatusCode());
	// System.out.println("AWS Error Code: " + ase.getErrorCode());
	// System.out.println("Error Type: " + ase.getErrorType());
	// System.out.println("Request ID: " + ase.getRequestId());
	// } catch (AmazonClientException ace) {
	// System.out.println("Caught an AmazonClientException, which means the
	// client encountered " +
	// "a serious internal problem while trying to communicate with SQS, such as
	// not " +
	// "being able to access the network.");
	// System.out.println("Error Message: " + ace.getMessage());
	// }
	// }

}
