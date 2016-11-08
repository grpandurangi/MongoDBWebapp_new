package com.journaldev.app.servlets;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.ibm.mq.MQException;
import com.journaldev.app.messaging.asb.MessageReceiver;
import com.journaldev.app.messaging.asb.MessageSender;
import com.journaldev.app.messaging.mq.MQMessageReceiver;
import com.journaldev.app.messaging.mq.MQMessageSender;
import com.journaldev.mongodb.dao.MongoDBPersonDAO;
import com.journaldev.mongodb.model.Person;
import com.journaldev.mongodb.model.Person.OPERATION;
import com.mongodb.MongoClient;

/**
 * Servlet implementation class FrontController
 */

public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private MessageSender messageSender;
	private MQMessageSender mqSender;
	private MongoDBPersonDAO personDAO;
	private Gson gson = new Gson();
       
    /**
     * @throws JMSException 
     * @throws NamingException 
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() throws IOException, NamingException, JMSException {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	super.init();
    	//Initialize ASB sender
    	try {
			messageSender = new MessageSender();
		} catch (IOException | NamingException | JMSException e) {
			throw new ServletException(e.getLocalizedMessage(), e);
		}
        
        ServletContext ctx = getServletContext();
        MongoClient mongo = (MongoClient) ctx.getAttribute("MONGO_CLIENT");
		personDAO = new MongoDBPersonDAO(mongo);
		
		//Initialize ASB receiver
		MessageReceiver receiver;
		try {
			receiver = new MessageReceiver();
			receiver.setPersonDAO(personDAO);
		} catch (IOException | NamingException | JMSException e) {
			throw new ServletException(e.getLocalizedMessage(), e);
		}
		
		//Initialize MQ sender
		try {
			mqSender = new MQMessageSender();
		} catch (MQException | IOException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			//Initialize MQ receiver
			final MQMessageReceiver mqReceiver = new MQMessageReceiver();
			mqReceiver.setMessageDao(personDAO);
			Thread receiverTask = new Thread(new Runnable() {
				
				@Override
				public void run() {
					synchronized (mqReceiver) {
						while(true) {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								System.err.println(e.getLocalizedMessage());
							}
							try {
								mqReceiver.readMessageFromQueue();
							} catch (MQException e) {
								System.err.println(e.getLocalizedMessage());
							}
						}
					}
				}
			});
			
			receiverTask.start();
			
		} catch (IOException | MQException e) {
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		String target = "index.jsp";
		
		if(uri.endsWith("addPersonPage.do")) {
			target = "/addPerson.jsp";
		}
		if(uri.endsWith("addPerson.do")) {
			try {
				target = sendAddPersonDetail(request, response);
			} catch (NamingException | JMSException e) {
				e.printStackTrace();
				target = "/addPerson.jsp";
				request.setAttribute("error", "Unable to add person.");
			}
		}
		if(uri.endsWith("deletePerson.do")) {
			try {
				target = sendDelPersonDetail(request, response);
			} catch (NamingException | JMSException e) {
				e.printStackTrace();
				request.setAttribute("error", "Unable to delete person.");
			}
		}
		if(uri.endsWith("viewPersonPage.do")) {
			List<Person> persons = personDAO.readAllPerson();
			request.setAttribute("persons", persons);
			
			target = "/viewPersons.jsp";	
		}
		if(uri.endsWith("searchPersonPage.do")) {
			target = "/searchPerson.jsp";
		}
		if(uri.endsWith("home.do")) {
			target = "/index.jsp";
		}
		if(uri.endsWith("updateCountryPage.do")) {
			target = "/updateCountryPage.jsp";
		}
		if(uri.endsWith("updateCountry.do")) {
			String country = request.getParameter("country");
			if (country == null || country.equals("")) {
				request.setAttribute("error", "Mandatory Parameters Missing");
				target = "/updateCountryPage.jsp";
			} else {
				List<Person> personList = personDAO.readAllPerson();
				for (Person person : personList) {
					person.setCountry(country);
					person.setOperation(OPERATION.EDIT);
					String msg = gson.toJson(person);
					try {
						messageSender.sendMessage(msg);
						request.setAttribute("success", "Country updated for all");
					} catch (JMSException e) {
						System.err.println(e.getLocalizedMessage());
						target = "/updateCountryPage.jsp";
						request.setAttribute("error", "Unable to update country for all.");
					}
				}
			}
		}
		if(uri.endsWith("search.do")) {
			Person p = new Person();
			p.setName(request.getParameter("name"));
			p = personDAO.searchPerson(p);
			if(p == null) {
				request.setAttribute("error", "No data found");
			} else {
				request.setAttribute("person", p);
			}
			target = "/searchPerson.jsp";
		}
		if(uri.endsWith("editPerson.do")) {
			try {
				target = sendEditPersonDetail(request, response);
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			
		}
		RequestDispatcher rd = request.getRequestDispatcher(target);
		rd.forward(request, response);
	}
	
	private String sendAddPersonDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NamingException, JMSException {
		String name = request.getParameter("name");
		String country = request.getParameter("country");
		if ((name == null || name.equals(""))
				|| (country == null || country.equals(""))) {
			request.setAttribute("error", "Mandatory Parameters Missing");
			return "/addPerson.jsp";
		} else {
			Person p = new Person();
			p.setCountry(country);
			p.setName(name);
			p.setOperation(OPERATION.SAVE);
			
			String msg = gson.toJson(p);
			messageSender.sendMessage(msg);
			
			System.out.println("Person will be Added shortly.");
			request.setAttribute("success", "Person will be Added shortly");

			return "/index.jsp";
		}
	}
	
	private String sendDelPersonDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NamingException, JMSException {
		String id = request.getParameter("id");
		if (id == null || "".equals(id)) {
			throw new ServletException("id missing for delete operation");
		}
		Person p = new Person();
		p.setId(id);
		p.setOperation(OPERATION.DELETE);
		
		String msg = gson.toJson(p);
		try {
			mqSender.sendMessage(msg);
		} catch (MQException e) {
			System.err.println(e.getLocalizedMessage());
			request.setAttribute("error", "Unable to send message to MQ");
			throw new ServletException(e.getLocalizedMessage(), e);
		}
		System.out.println("Person will be deleted shorlty with id=" + id);
		request.setAttribute("success", "Person will be deleted shorlty");

		return "/index.jsp";
	}

	private String sendEditPersonDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, NamingException, JMSException {
		
		String id = request.getParameter("id"); // keep it non-editable in UI
		if (id == null || "".equals(id)) {
			throw new ServletException("id missing for edit operation");
		}

		String name = request.getParameter("name");
		String country = request.getParameter("country");

		if ((name == null || name.equals(""))
				|| (country == null || country.equals(""))) {
			request.setAttribute("error", "Name and Country Can't be empty");
			Person p = new Person();
			p.setId(id);
			p.setName(name);
			p.setCountry(country);
			request.setAttribute("person", p);

			return "/editPerson.jsp";
		} else {
			MongoClient mongo = (MongoClient) request.getServletContext()
					.getAttribute("MONGO_CLIENT");
			MongoDBPersonDAO personDAO = new MongoDBPersonDAO(mongo);
			Person p = new Person();
			p.setId(id);
			p.setName(name);
			p.setCountry(country);
			p.setOperation(OPERATION.EDIT);
			
			String msg = gson.toJson(p);
			messageSender.sendMessage(msg);
			
			Person p2 = personDAO.readPerson(p);
			
			request.setAttribute("person", p2);
			System.out.println("Person details are sent for edit and will be updated shortly with id=" + id);
			request.setAttribute("success", "Person details are sent for edit and will be updated shortly");
			
			return "/searchPerson.jsp";
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
