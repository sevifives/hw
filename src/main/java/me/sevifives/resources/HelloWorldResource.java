package me.sevifives.resources;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

import io.dropwizard.hibernate.UnitOfWork;
import me.sevifives.HelloWorldConfiguration;
import me.sevifives.api.Saying;
import me.sevifives.api.TwilioAPI;
import me.sevifives.core.Task;
import me.sevifives.db.PersonDAO;
import me.sevifives.db.TaskDAO;

@Path("/hello-world")
public class HelloWorldResource {
	
	final static Logger logger = LoggerFactory.getLogger(HelloWorldResource.class);
	
	private HelloWorldConfiguration hwConfig;
	
	private final String template;
    private final String defaultName;
    private final AtomicLong counter;
    
    private final TaskDAO taskDao;
    private final PersonDAO personDao;

    public HelloWorldResource(
    		String template,
    		String defaultName,
    		HelloWorldConfiguration cfg,
    		TaskDAO tDao,
    		PersonDAO pDao) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
        this.hwConfig = cfg;
        
        this.taskDao = tDao;
        this.personDao = pDao;
    }

    @GET
    @Timed
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.orElse(defaultName));
        return new Saying(counter.incrementAndGet(), value);
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
    public String sendToTwilio(@QueryParam("phone") Optional<String> phone) {
    		if (!phone.isPresent()) {
    			return "Phone is required.";
    		}
    		
    		String ret = String.format("Greetings! The current time is: %s D495D8BPZK1GFS3", Instant.now().toString());
    		TwilioAPI api = new TwilioAPI();
    		
    		return ret;
//    		Message msg = api.sendMessage(phone.get(), ret);
//		return msg.getSid();
    }
    
    @POST
    @Timed
    @Path("sms")
    public String handleSms(String msg) throws IOException {
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
    @UnitOfWork
    public Response handleComplexSms(@FormParam("Body") String body) throws IOException {
    		return _handleTodo(body);
    }
    
    private Response _handleTodo(String body) {
	    	ArrayList<String> e = new ArrayList<String>(Arrays.asList(body.split(" ")));
	    	
	    	String action = e.remove(0).toLowerCase();
	    	
	    	switch(action) {
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
    		
    		taskDao.create(t);
    		
    		return Response.ok().build();
    }
    
    private Response _listTasks() {
    		return Response.ok(taskDao.findAllForPersonId(0L)).build();
    }
    
    private Response _removeTask(String taskName) {
    		return Response.ok(taskDao.findByTitleAndPersonId(taskName, 0L)).build();
    }
}
