/**
 * 
 */
package com.journaldev.app.messaging.mq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

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
public class MQMessageSender implements ExceptionListener {
	
	private MQQueueManager qMgr;
	private int openOptions;
	private String queueName;
	private String queuemanagerName;
	private MQQueue destQueue;
	
	private static Random randomGenerator = new Random();
	
	/**
	 * Initialize MQMessageSender
	 * @throws MQException
	 * @throws IOException
	 */
	public MQMessageSender() throws MQException, IOException {
		init();
		System.out.println("MQ sender is ready");
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
		
		// Set up the options on the queue we wish to open...
		// MQOO_OUTPUT - Open the queue to put messages.
		// MQOO_INPUT_AS_Q_DEF - Open the queue to get messages using the queue-defined default.
	    openOptions = CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_OUTPUT ;
		
//		openOptions = CMQC.MQOO_INQUIRE + CMQC.MQOO_FAIL_IF_QUIESCING + CMQC.MQOO_INPUT_SHARED;
	    
	    initConnection();
	}
	
	private void initConnection() throws MQException {
		// Open a connection to the queue manager
		qMgr = new MQQueueManager(queuemanagerName);
		// Establish access to an WebSphere MQ queue on this queue manager 
 		// using default queue manager name and alternative user ID values.
 		destQueue = qMgr.accessQueue(queueName, openOptions);
	}

	/**
	 * Send message to MQ
	 */
	public void sendMessage(String message) throws MQException {
		
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
			System.out.println("Message sent to MQ with messageId: " + randomMessageID);

		} catch (IOException e) {
			e.printStackTrace();
		} /*finally {
			destQueue.close();
			qMgr.disconnect();	// Ends the connection to the queue manager.
		}*/
//	    System.out.println("OUTPUTQ1 size:" + destQueue.getCurrentDepth());
	    
	}
	
	/**
	 * Close queue manager
	 * @throws MQException
	 */
	public void close() throws MQException {
		destQueue.close();
		if(qMgr != null) {
			qMgr.disconnect();
			qMgr.close();
		}
		qMgr = null;
	}

	@Override
	public void onException(JMSException arg0) {
		System.err.println("Error in MQ Sender connection");
		try {
			initConnection();
			System.out.println("MQ connected");
		} catch (MQException e) {
			e.printStackTrace();
		}
	}
}
