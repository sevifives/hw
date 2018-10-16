package me.sevifives.resources;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import me.sevifives.api.Saying;

@Path("/hello-world")
public class HelloWorldResource {
	
	final static Logger logger = LoggerFactory.getLogger(HelloWorldResource.class);
	
	
	private final String template;
    private final String defaultName;
    private final AtomicLong counter;
    

    public HelloWorldResource(
    		String template,
    		String defaultName) {
        this.template = template;
        this.defaultName = defaultName;        
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.orElse(defaultName));
        return new Saying(counter.incrementAndGet(), value);
    }
}
