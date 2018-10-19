package me.sevifives.resources;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twilio.base.ResourceSet;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.conference.Participant;
import com.twilio.rest.api.v2010.account.conference.Participant.Status;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Conference;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Reject;
import com.twilio.twiml.voice.Say;

import me.sevifives.HelloWorldConfiguration;
import me.sevifives.api.TwilioAPI;

@Path("twilio/conference")
public class TwilioConferenceResource {
	@Context HttpServletRequest request;
	@Context HttpServletResponse response;
	
	private void blockPublic(HttpServletRequest req) {
		StringBuffer uriB = req.getRequestURL();
		
		String uri = new String(uriB);
		logger.info("Url: {}", uri);
		if (uri.startsWith("http://localhost")) { return; }
		
		throw new NotAllowedException("Nope.");
    }
	
	final static Logger logger = LoggerFactory.getLogger(TwilioConferenceResource.class);
	
	private HelloWorldConfiguration hwConfig;
	
	public TwilioConferenceResource (HelloWorldConfiguration cfg) {
		this.hwConfig = cfg;
	}
	
	private String completeUrl(String path) {
		String baseUrl = hwConfig.getExternalDomain();
		String completeStatus = String.format("%s%s%s",baseUrl, "/twilio/conference", path);
		
		return completeStatus.replaceFirst("http:","https:");
	}
	
	private String statusCallbackLocation() {
		return this.completeUrl("/status");
	}
	
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("application/x-www-form-urlencoded")
	@Path("/error")
	public Response error(String body) {
		logger.info("title='CONFERENCE ERROR FROM TWILIO', query='{}', message: {}", request.getQueryString(), body);
		return Response.ok("OK").build();
	}
	
	
	@POST
	@Path("/status")
	@Produces("application/xml")
	@Consumes("application/x-www-form-urlencoded")
	public Response status(String body) {
		Enumeration<String> headers = request.getHeaderNames();
		Map<String, Object> hs = new HashMap<String, Object>();
		while (headers.hasMoreElements()) {
			String h = headers.nextElement();
			hs.put(h, request.getHeader(h));
		}
		
		logger.info("title='CONFERENCE STATUS CALL', queryParams='{}', \n headers={},\n body={}",request.getQueryString(), hs, body);
		
		return Response.ok("OK").build();
	}


	@POST
	@Path("/join")
	@Produces("application/xml")
	@Consumes("application/x-www-form-urlencoded")
	public Response join(
			@FormParam("Caller") String callerNumber,
			String body) {
		
		logger.info("Join Inbound names: caller={}, queryString='{}'\n{}", callerNumber, request.getQueryString(), body);
		
		Say message = new Say.Builder("Hello and welcome to the waiting room! Please press 1 to join.").build();
		
		Gather gather = new Gather.Builder()
				.action(this.completeUrl("/connect"))
				.method(HttpMethod.POST)
				.say(message)
			.build();
		
		VoiceResponse vResp = new VoiceResponse.Builder()
					.gather(gather)
				.build();				
				
		return Response.ok(vResp.toXml()).build();
	}
	
	
	@POST
	@Path("/connect")
	@Produces("application/xml")
	@Consumes("application/x-www-form-urlencoded")
	public Response connect(
			@FormParam("Caller") String callerNumber,
			String body
		) {
		Boolean isMod = callerNumber.replaceAll("\\+","").equals(hwConfig.getModeratorNumber());
		
		logger.info("Connect Inbound names: isMod={} modNumber={}, callerNumber={} \n queryString='{}'\n{}", isMod, hwConfig.getModeratorNumber(), callerNumber, request.getQueryString(), body);
		
		Say greeting = new Say.Builder("Welcome to the room! The meeting will start once the mod joins.").build();
		
		Conference conf = new Conference.Builder("TwilioQuestRoom")
				.muted(!isMod)
				.startConferenceOnEnter(isMod)
				.endConferenceOnExit(isMod)
				.statusCallback(this.statusCallbackLocation())
				.maxParticipants(2)
			.build();
		
		Dial dial = new Dial.Builder().conference(conf).build();
		
		VoiceResponse voiceResponse = new VoiceResponse.Builder().say(greeting).dial(dial).build();
		
		return Response.ok(voiceResponse.toXml()).build();
	}
	
	@POST
	@Path("/selective")
	@Produces("application/xml")
	@Consumes("application/x-www-form-urlencoded")
	public Response selectiveConnect(
			@FormParam("From") String callerNumber,
			String body
		) {
		Boolean isMod = callerNumber.replaceAll("\\+","").equals(hwConfig.getModeratorNumber());
		
		List<String> approved = Arrays.asList(hwConfig.getModeratorNumber(), "+15017250604");
		logger.info("Selective Connect Inbound names: isMod={} modNumber={}, callerNumber={} \n queryString='{}'\n{}", isMod, hwConfig.getModeratorNumber(), callerNumber, request.getQueryString(), body);

		if (!approved.contains(callerNumber)) {
			logger.info("Rejected: {}", callerNumber);
			Reject reject = new Reject.Builder().addText("You shall not pass!").build();

			return Response.ok(reject.toXml()).build();
		}
				
		Say greeting = new Say.Builder("Welcome to the room! The meeting will start once the mod joins.").build();
		
		Conference conf = new Conference.Builder("TwilioQuestRoom")
				.muted(!isMod)
				.startConferenceOnEnter(isMod)
				.endConferenceOnExit(isMod)
				.statusCallback(this.statusCallbackLocation())
				.maxParticipants(2)
			.build();
		
		Dial dial = new Dial.Builder().conference(conf).build();
		
		VoiceResponse voiceResponse = new VoiceResponse.Builder().say(greeting).dial(dial).build();
		
		return Response.ok(voiceResponse.toXml()).build();
	}
	
	@POST
	@Path("/simple")
	@Produces("application/xml")
	@Consumes("application/x-www-form-urlencoded")
	public Response simpleConnect(
			@FormParam("From") String callerNumber,
			String body
		) {
		Boolean isMod = callerNumber.replaceAll("\\+","").equals(hwConfig.getModeratorNumber());
		
		logger.info("simple Connect Inbound names: isMod={} modNumber={}, callerNumber={} \n queryString='{}'\n{}", isMod, hwConfig.getModeratorNumber(), callerNumber, request.getQueryString(), body);
				
		Say greeting = new Say.Builder("Welcome to the room! The meeting will start once the mod joins.").build();
		
		Conference conf = new Conference.Builder("TwilioQuestRoomSimple")
				.startConferenceOnEnter(true)
				.statusCallback(this.statusCallbackLocation())
			.build();
		
		Dial dial = new Dial.Builder().conference(conf).build();
		
		VoiceResponse voiceResponse = new VoiceResponse.Builder().say(greeting).dial(dial).build();
		
		return Response.ok(voiceResponse.toXml()).build();
	}
	
	@GET
	@Path("/activeConferences")
	@Produces(MediaType.APPLICATION_JSON)
	public Response activeConferences() {
		this.blockPublic(request);
		
		TwilioAPI api = new TwilioAPI();
		ResourceSet<com.twilio.rest.api.v2010.account.Conference> confs = api.getConferences();
		
		logger.info("Fetching conferences");
		for(com.twilio.rest.api.v2010.account.Conference record : confs) {
			switch(record.getStatus()) {
				case COMPLETED:
					break;
				case INIT:
				case IN_PROGRESS:
				default:
					logger.info("Conference record sid: date:{} sid: {} status: {}", record.getDateCreated(), record.getSid(), record.getStatus() );
					break;
			
			}
        }
		
		return Response.ok(api.getConferences()).build();
	}
	
	@POST
	@Path("/injected")
	@Produces("application/xml")
	@Consumes(MediaType.TEXT_XML)
	public Response injectMessage(
			@QueryParam("conferenceSid") Optional<String> conferenceSid
			) {
		this.blockPublic(request);

		new TwilioAPI();
		
		ResourceSet<Participant> participants = 
	            Participant.reader(conferenceSid.get())
	            .read();

		Participant firstParticipant = null;
		
	    for ( Participant p : participants) {
	    		if (firstParticipant == null) { firstParticipant = p; }
	    }
		
	    if (firstParticipant == null) { return Response.ok().build(); }
	    
	    
        Call call = Call.updater(firstParticipant.getCallSid())
        			.setMethod(HttpMethod.POST)
        			.setUrl("https://www.twilio.com/quest/7K00UBN5QYEMIKR.mp3")
        		.update();
        
        return Response.ok(call.getTo()).build();
	}
}
