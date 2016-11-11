/**
 * 
 */
package com.journaldev.app.messaging.mq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.gson.Gson;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.journaldev.app.constant.AppConstants;
import com.journaldev.mongodb.dao.DAOException;
import com.journaldev.mongodb.dao.MongoDBPersonDAO;
import com.journaldev.mongodb.model.Person;

/**
 * @author rprasad017
 * <p>Receives messages from WebSphere MQ</p>
 */
public class MQMessageReceiver {
	
	private MQQueueManager queueManager;
	private String queueName;
	private String queuemanagerName;
	private int openOptions;
	
	private MongoDBPersonDAO messageDao;
	private MQQueue queue;
	
	public MQMessageReceiver() throws IOException, MQException {
		init();
		System.out.println("MQ Receiver is ready");
	}
	
	/**
	 * Initialize Environment
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
		
		queueManager = new MQQueueManager(queuemanagerName);
	    
		// Set up the options on the queue we wish to open...
		// MQOO_OUTPUT = Open the queue to put messages.
		// MQOO_INPUT_AS_Q_DEF = Open the queue to get messages using the queue-defined default.
		// MQOO_INQUIRE = Open the object to query attributes.
	    openOptions = CMQC.MQOO_INPUT_AS_Q_DEF | CMQC.MQOO_OUTPUT | CMQC.MQOO_INQUIRE;
	    
	    // Establish access to an WebSphere MQ queue on this queue manager 
 		// using default queue manager name and alternative user ID values.
 	    queue = queueManager.accessQueue(queueName,openOptions);
	}
	
	/**
	 * Receives messages from MQ
	 * @throws MQException
	 */
	public void readMessageFromQueue() throws MQException {
	    
	    // Check if there are messages in the MQ
	    int depth = queue.getCurrentDepth(); 
	    if (depth == 0) { 
	    	return; 
	    } 
	    System.out.println("Current depth: " + depth); 
	    
	    MQMessage retrievedMessage = new MQMessage();
	    retrievedMessage.characterSet = AppConstants.CHARSET_ENCODING_UTF8;
	    MQGetMessageOptions gmo = new MQGetMessageOptions(); // Accept defaults

	    boolean thereAreMessages=true;
//	    try {
	    	// Repeats until all message in MQ are read
		    while(thereAreMessages){
		        try {
		        	queue.get(retrievedMessage, gmo);		// Retrieve message from MQ
		    		processMessage(retrievedMessage);		// Read and process message
		    		clearMessageBody(retrievedMessage);		// Clear Retrieved Message
		        } catch(MQException e){
			        if(e.reasonCode == CMQC.MQRC_NO_MSG_AVAILABLE) {
			            System.err.println("No more message available");
			        }
			        // All messages are read
			        thereAreMessages = false;
		        } catch (IOException e) {
					e.printStackTrace();
				} catch (DAOException e) {
					e.printStackTrace();
				}
		    } 
		    /*} finally {
			 queue.close();
			 queueManager.disconnect();    // Ends the connection to the queue manager.
		}*/
	}
	
	/**
	 * Clear MQMessage
	 * @param retrievedMessage
	 * @throws IOException
	 * @throws MQException
	 */
	private void clearMessageBody(MQMessage retrievedMessage) throws IOException, MQException {
		retrievedMessage.correlationId = null;
    	retrievedMessage.messageId = null;
    	retrievedMessage.clearMessage();
    	retrievedMessage.deleteProperty("fileName");
	}

	/**
	 * Read received message and write to destination folder
	 * @param retrievedMessage
	 * @throws MQException
	 * @throws IOException
	 * @throws DAOException 
	 */
	private void processMessage(MQMessage retrievedMessage) throws MQException, IOException, DAOException {
		// Get file name
    	byte[] bytes = new byte[retrievedMessage.getMessageLength()];
    	retrievedMessage.readFully(bytes);
    	String msgText = new String(bytes, AppConstants.UTF_8);
    	System.out.println("Message received from MQ with messageId = "+ retrievedMessage.messageId);
    	
    	Gson gson = new Gson();
    	Person p = gson.fromJson(msgText, Person.class);
    	
		messageDao.deletePerson(p);
		System.out.println("Person deleted successfully.");
	}
	
	/**
	 * Close queue manager
	 * @throws MQException
	 */
	public void close() throws MQException {
		queue.close();
		if(queueManager != null) {
			queueManager.disconnect();
			queueManager.close();
		}
		queueManager = null;
	}
	
	/**
	 * @return the messageDao
	 */
	public MongoDBPersonDAO getMessageDao() {
		return messageDao;
	}

	/**
	 * @param messageDao the messageDao to set
	 */
	public void setMessageDao(MongoDBPersonDAO messageDao) {
		this.messageDao = messageDao;
	}

	public static void main(String[] args) throws IOException, MQException {
		MQMessageReceiver receiver = new MQMessageReceiver();
		int i=0;
		while(i<10) {
			receiver.readMessageFromQueue();
			i++;
		}
		receiver.close();
	}
}
