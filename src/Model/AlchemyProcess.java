package Model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.google.gson.Gson;

/**
 * Servlet implementation class AlchemyProcess
 */
//@WebServlet("/AlchemyProcess")
public class AlchemyProcess extends HttpServlet {
	
	public static SNSHelper snsHelperAlchemy = null;
	public static SQSHelper sqsHelperAlchemy = null;
	public static String QueueURL = "";
	
	@Override
	public void init() throws ServletException {
		snsHelperAlchemy = new SNSHelper();
		sqsHelperAlchemy = new SQSHelper();
		QueueURL = sqsHelperAlchemy.createQueue("AlchemyNewTweets");
		//try {
			System.out.println("Executing the system code for alchemy");
			System.out.println("Subscribing to: " + "http://158.222.238.198:8080/TwitterMap/AlchemyProcess");
			//snsHelperAlchemy.subscribeToTopic("NewTweets", "http", "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080//TwitterMap//AlchemyProcess" );
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
	}

	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AlchemyProcess() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String messagetype = request.getHeader("x-amz-sns-message-type");
		if (messagetype == null)
			return;
		Scanner scan = new Scanner(request.getInputStream());
	    StringBuilder builder = new StringBuilder();
	    while (scan.hasNextLine()) {
	      builder.append(scan.nextLine());
	    }	    
		Gson G = new Gson();
		SNSMessage msg = G.fromJson(builder.toString(), SNSMessage.class);
		if (messagetype.equals("Notification")) {
			// actual message here
			System.out.println("Got message from sns: " + msg.getMessage());
		} else if (messagetype.equals("SubscriptionConfirmation")){
			snsHelperAlchemy.confirmSubscription(msg.getTopicArn(),msg.getToken());
		}
		scan.close();
	}
}
