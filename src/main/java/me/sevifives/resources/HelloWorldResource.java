package me.sevifives.resources;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

import me.sevifives.HelloWorldConfiguration;
import me.sevifives.api.Saying;
import me.sevifives.api.TwilioAPI;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
	
	final static Logger logger = LoggerFactory.getLogger(HelloWorldResource.class);
	
	private HelloWorldConfiguration hwConfig;
	
	private final String template;
    private final String defaultName;
    private final AtomicLong counter;

    public HelloWorldResource(String template, String defaultName, HelloWorldConfiguration cfg) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
        this.hwConfig = cfg;
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
}
