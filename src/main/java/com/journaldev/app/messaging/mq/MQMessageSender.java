/**
 * 
 */
package com.journaldev.app.messaging.mq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;

/**
 * @author rprasad017
 * <p>Sends messages to WebSphere MQ</p>
 */
public class MQMessageSender {
	
	private MQQueueManager qMgr;
	private int openOptions;
	private String queueName;
	private String queuemanagerName;
	
	private static Random randomGenerator = new Random();
	
	/**
	 * Initialize MQMessageSender
	 * @throws MQException
	 * @throws IOException
	 */
	public MQMessageSender() throws MQException, IOException {
		init();
	}
	
	/**
	 * Initialize Queue manager
	 * @throws IOException
	 * @throws MQException
	 */
	public void init() throws IOException, MQException {
		// Load configuration from properties file
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	InputStream input = classLoader.getResourceAsStream("config.properties");
		Properties config = new Properties();
		config.load(input);
		
		queueName = config.getProperty("env.queue");
		queuemanagerName = config.getProperty("env.queue.manager");

		// As values set in the MQEnvironment class take effect when the 
        // MQQueueManager constructor is called, you must set the values 
        // in the MQEnvironment class before you construct an MQQueueManager object.
		MQEnvironment.hostname = config.getProperty("env.hostName");	
		MQEnvironment.port = Integer.valueOf(config.getProperty("env.port"));
		MQEnvironment.channel = config.getProperty("env.channel");
		
		// Open a connection to the queue manager
		qMgr = new MQQueueManager(queuemanagerName);
		
		// Set up the options on the queue we wish to open...
		// MQOO_OUTPUT - Open the queue to put messages.
		// MQOO_INPUT_AS_Q_DEF - Open the queue to get messages using the queue-defined default.
	    openOptions = CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_OUTPUT ;
		
//		openOptions = CMQC.MQOO_INQUIRE + CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_INPUT_SHARED;
	}
	
	/**
	 * Send message to MQ
	 */
	public void sendMessage(String message) throws MQException {
		System.out.println("Send Message Process started");
				
		// Now specify the queue that we wish to open, and the open options...
		MQQueue destQueue = qMgr.accessQueue(queueName, openOptions);
		
		try {
			// Construct input message
			MQMessage inputMsg = new MQMessage();
			inputMsg.characterSet = 1208;
			inputMsg.writeString(message);

			long randomMessageID = randomGenerator.nextLong() >>> 1;
			inputMsg.messageId = String.valueOf(randomMessageID).getBytes();

			MQPutMessageOptions pmo = new MQPutMessageOptions(); // Accept
																	// defaults

			destQueue.put(inputMsg, pmo); // Put message to queue
			System.out.println("Message sent with messageId: " + randomMessageID);

		} catch (IOException e) {
			e.printStackTrace();
		}
//	    System.out.println("OUTPUTQ1 size:" + destQueue.getCurrentDepth());
	    destQueue.close();
	    System.out.println("Send Message Process ended");
	}
	
	/**
	 * Close queue manager
	 * @throws MQException
	 */
	public void close() throws MQException {
		if(qMgr != null) {
			qMgr.disconnect();
			qMgr.close();
		}
		qMgr = null;
	}
}
