package com.journaldev.app.messaging.asb;
/**
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.Gson;
import com.journaldev.mongodb.dao.MongoDBPersonDAO;
import com.journaldev.mongodb.model.Person;


/**
 * @author rprasad017
 *
 */
public class MessageReceiver implements MessageListener, ExceptionListener {

	private Connection connection;
    private Session receiveSession;
    private MessageConsumer receiver;
    private ConnectionFactory cf;
    private Destination queue;
    
    private MongoDBPersonDAO personDAO;
    
	/**
	 * @return the personDAO
	 */
	public MongoDBPersonDAO getPersonDAO() {
		return personDAO;
	}

	/**
	 * @param personDAO the personDAO to set
	 */
	public void setPersonDAO(MongoDBPersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	/**
	 * @throws IOException 
	 * @throws NamingException 
	 * @throws JMSException 
	 * @throws SecretKeyInitException 
	 * 
	 */
	public MessageReceiver() throws IOException, NamingException, JMSException {
		// Configure JNDI environment
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	InputStream input = classLoader.getResourceAsStream("servicebus.properties");
    	
    	Properties properties = new Properties();
    	properties.load(input);
        Context context = new InitialContext(properties);

        // Lookup ConnectionFactory and Queue
        cf = (ConnectionFactory) context.lookup("SBCF");
        queue = (Destination) context.lookup("QUEUE");
        
        initializeConnection();
        
        System.out.println("Message receiver is ready");
	}

	@Override
	public void onMessage(Message message) {
		try {
            processMsg(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public synchronized void processMsg(Message message) throws JMSException,
			 IOException {
		System.out.println("Received message from ASB with JMSMessageID = "
				+ message.getJMSMessageID());
		
		if (message instanceof TextMessage) {
			TextMessage msg = (TextMessage) message;
			
			Gson gson = new Gson();
			
			Person person = gson.fromJson(msg.getText(), Person.class);
			if(person.getOperation() != null) {
				switch (person.getOperation()) {
				case SAVE:
					savePerson(person);
					break;
				case EDIT:
					updatePerson(person);
					break;
				case DELETE:
					deletePerson(person);
					break;
				default:
					break;
				}
			}
			
			System.out.println("Received: "+ msg.getText());

			message.acknowledge();
		}
	}
	
	private void savePerson(Person person) {
		personDAO.createPerson(person);
		System.out.println("Person added successfully with id=" + person.getId());
	}

	private void updatePerson(Person person) {
		personDAO.updatePerson(person);
		System.out.println("Person updated successfully with id=" + person.getId());
	}
	
	private void deletePerson(Person person) {
		personDAO.deletePerson(person);
		System.out.println("Person deleted successfully with id=" + person.getId());
	}

	public void close() throws JMSException {
		if(connection!= null) {
    		connection.stop();
    	}
		if(receiver != null) {
			receiver.close();
        }
        if(receiveSession != null) {
        	receiveSession.close();
        }
    	if(connection!= null) {
    		connection.stop();
    		connection.close();
    	}
    	connection = null;
    	receiveSession = null;
    	receiver = null;
	}

	private void initializeConnection() throws JMSException {
		// Create Connection
        connection = cf.createConnection();
		
		// Create receiver-side Session, MessageConsumer,and MessageListener
        receiveSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        receiver = receiveSession.createConsumer(queue);
        receiver.setMessageListener(this);
        connection.start();
	}

	@Override
	public void onException(JMSException arg0) {
		System.err.println("Error in receiver connection, Retrying to connect...");
		/*try {
			close();
		} catch (JMSException ex) {
			System.err.println(ex.getLocalizedMessage());
		}*/
		try {
			initializeConnection();
		} catch (JMSException e1) {
			e1.printStackTrace();
		}
	}
	
}
