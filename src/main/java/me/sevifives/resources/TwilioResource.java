package me.sevifives.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

import io.dropwizard.hibernate.UnitOfWork;
import me.sevifives.HelloWorldConfiguration;
import me.sevifives.api.TwilioAPI;
import me.sevifives.core.Task;
import me.sevifives.db.PersonDAO;
import me.sevifives.db.TaskDAO;

@Path("/twilio")
public class TwilioResource {
	final static Logger logger = LoggerFactory.getLogger(TwilioResource.class);
	
	private HelloWorldConfiguration hwConfig;
	
	private final TaskDAO taskDao;
    private final PersonDAO personDao;
    
    private void blockPublic(HttpServletRequest req) {
		StringBuffer uriB = req.getRequestURL();
		
		String uri = new String(uriB);
		logger.info("Url: {}", uri);
		if (uri.startsWith("http://localhost")) { return; }
		
		throw new NotAllowedException("Nope.");
    }
    
	public TwilioResource(
			HelloWorldConfiguration cfg,
    			TaskDAO tDao,
    			PersonDAO pDao
			) {
		this.hwConfig = cfg;
        this.taskDao = tDao;
        this.personDao = pDao;
	}
	
	@GET
    @Timed
    @Path("simple")
    @Produces(MediaType.TEXT_PLAIN)
    public String saySimpleHello(@QueryParam("name") Optional<String> name) {
    		return "Hello World";
    }
    
    @POST
    @Timed
    @Path("send")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendToTwilio(
    		@QueryParam("phone") Optional<String> phone,
    		@QueryParam("message") Optional<String> message,
    		@QueryParam("viaService") Optional<Boolean> viaService
    		) throws URISyntaxException {
    		
    		this.blockPublic(request);
    		
    		if (!phone.isPresent()) {
    			return "Phone is required.";
    		}
    		
    		TwilioAPI api = new TwilioAPI();
    		
    		String baseUrl = hwConfig.getExternalDomain();
    		
    		String completeStatus = String.format("%s%s",baseUrl, "/twilio/sms/todo/status");

    		com.twilio.rest.api.v2010.account.Message resp;
    		
    		if (viaService.isPresent() && viaService.get()) {
    			resp = api.sendMessageService(phone.get(), message.get(), completeStatus, hwConfig.getCopilotServiceSid());
    		} else {
    			resp = api.sendMessage(phone.get(), message.get(), completeStatus);
    		}
		return resp.getSid();
    }
    
    @POST
    @Timed
    @Path("sendSpam")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendToSpam(
    		@QueryParam("phones") Optional<String> phones,
    		@QueryParam("message") Optional<String> message,
    		@QueryParam("spamCount") Optional<Integer> spamCount
    		) throws URISyntaxException {
    		
    		this.blockPublic(request);
    		
    		if (!phones.isPresent()) {
    			return "Phone is required.";
    		}
    		String baseUrl = hwConfig.getExternalDomain();
    		
    		String completeStatus = String.format("%s%s",baseUrl, "/twilio/sms/todo/status");
    		ArrayList<String> rets = new ArrayList<String>();
    		
    		String[] _phones = phones.get().split(",");
    		
    		Integer ct = spamCount.orElse(1);
    		
    		TwilioAPI api = new TwilioAPI();
    		
    		for (String phone : _phones) {
    			for (int i=0;i<ct;i+=1) {
        			com.twilio.rest.api.v2010.account.Message resp =
        					api.sendMessageService(phone, i + ": " + message.get() , completeStatus, hwConfig.getCopilotServiceSid());
        			rets.add(phone + "::" + resp.getSid());
        		}
    		}
    		
		return StringUtils.join(rets,",");
    }
    
    @POST
    @Timed
    @Path("sms")
    public String handleSms(String msg) throws IOException {
    		this.blockPublic(request);
    		
    		Body body = new Body
                    .Builder(msg)
                    .build();
    		
        Message sms = new Message
                .Builder()
                .body(body)
                .build();
        
        MessagingResponse twiml = new MessagingResponse
                .Builder()
                .message(sms)
                .build();

        return twiml.toXml();
    }
    
    @POST
    @Timed
    @Path("sms/todo")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_PLAIN)
    @UnitOfWork
    public Response handleComplexSms(@FormParam("Body") String body) throws IOException, URISyntaxException {
    		return this._handleTodo(body);
    }
    
    
    @Context
    HttpServletRequest request;
    
    @POST
    @Timed
    @Path("sms/todo/status")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_PLAIN)
    @UnitOfWork
    public Response handleTodoStatus(
    		MultivaluedMap<String, String> form
    		) {
    	
    		logger.info("Submitted: {}", form);
    		
    		Enumeration<String> headers = request.getHeaderNames();
    		while (headers.hasMoreElements()) {
    			String h = headers.nextElement();
    			logger.info("Header: {} => {}", h, request.getHeader(h));
    		}
    		String msgSid = form.getFirst("MessageSid");
    		String msgStatus = form.getFirst("MessageStatus");
    	
    		logger.info("Components: {} {}\n{}", msgSid, msgStatus);
    		
    		return Response.ok(msgSid).build();
    }
    
    
    private Response _handleTodo(String body) throws URISyntaxException {
	    	ArrayList<String> e = new ArrayList<String>(Arrays.asList(body.split(" ")));
	    	
	    	String action = e.remove(0).toLowerCase();
	    	
	    	switch(action.toLowerCase()) {
	    		case "add":
	    			return this._addTask( String.join(" ",e) );
	    		case "list":
	    			return this._listTasks();
	    		case "remove":
	    			return this._removeTask( String.join(" ",e) );
	    		default:
	    			return Response.notModified().build();
	    	}
    }
    
    private Response _addTask(String taskName) {
    		Task t = new Task();
    		t.setTitle(taskName);
    		t.setDescription("-na-");
    		t.setPersonId(0L);
    		
    		Task r = taskDao.create(t);
    		
    		Body body = new Body
                    .Builder(r.getTitle())
                    .build();
    		
        Message sms = new Message
                .Builder()
                .body(body)
                .build();
        
        MessagingResponse twiml = new MessagingResponse
                .Builder()
                .message(sms)
                .build();

    		return Response.ok(twiml.toXml()).build();
    }
    
    private Response _listTasks() throws URISyntaxException {
    		List<Task> all = taskDao.findAllForPersonId(0L);
    		
    		String r = "";
    		int i = 0;
    		for (Task t : all) {
    			r+= (i+1) + ". " + t.getTitle() + " ";
    			i+=1;
    		}
    		
	    	Body body = new Body
	                .Builder(r)
	                .build();
			
	    Message sms = new Message
	            .Builder()
	            .body(body)
	            .build();
	    
	    MessagingResponse twiml = new MessagingResponse
	            .Builder()
	            .message(sms)
	            .build();

    		return Response.ok(twiml.toXml()).build();
    }
    
    private Response _removeTask(String itemIndex) throws URISyntaxException {
    		
    		Integer idx = Integer.valueOf(itemIndex); 
    		
    		List<Task> all = taskDao.findAllForPersonId(0L);
    		
    		if (all == null) { return this._listTasks(); }
    		
    		Task t = all.get(idx-1);
    		
    		taskDao.delete(t);
    		
    		return this._listTasks();
    }
}
