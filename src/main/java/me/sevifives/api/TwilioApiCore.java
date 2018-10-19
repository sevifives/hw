package me.sevifives.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.converter.Promoter;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Conference;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import me.sevifives.HelloWorldConfiguration;

public class TwilioApiCore {
	private String ACCOUNT_SID = "";
	private String AUTH_TOKEN = "";
	private String TWILIO_PHONE = "";
	
	public TwilioApiCore(HelloWorldConfiguration cfg) {
		this.ACCOUNT_SID = cfg.getAccountSid();
		this.AUTH_TOKEN = cfg.getAccountToken();
		this.TWILIO_PHONE = cfg.getAccountPhone();
		
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
	}

	public Message sendMessage(String phoneNumber, String msg) {
		Message message = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(TWILIO_PHONE), msg).create();

		return message;
	}

	public Message sendMessage(String phoneNumber, String msg, String callbackUrl) throws URISyntaxException {
		Message message = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(TWILIO_PHONE), msg)
				.setStatusCallback(new URI(callbackUrl)).create();

		return message;
	}

	public Message sendMessageService(String phoneNumber, String msg, String callbackUrl, String serviceSid)
			throws URISyntaxException {
		Message message = Message.creator(new com.twilio.type.PhoneNumber(phoneNumber), serviceSid, msg)
				.setStatusCallback(new URI(callbackUrl)).create();

		return message;
	}

	public Message sendMessageFromPhone(String phone, String fromPhone, String msg, String completeStatus) throws URISyntaxException {
		Message message = Message.creator(
				new com.twilio.type.PhoneNumber(phone),
				new com.twilio.type.PhoneNumber(fromPhone), msg)
				.setStatusCallback(new URI(completeStatus)).create();
		
		return message;
	}
	
	public Call sendVoice(String toPhone, String fromPhone, String uriToSend, String callbackUrl) throws URISyntaxException {
		Call call = Call.creator(
				new PhoneNumber(toPhone),
				new PhoneNumber(fromPhone),
		        new URI(uriToSend)).setStatusCallback(new URI(callbackUrl)).create();
		
		return call;
	}

	public Map<String, com.twilio.rest.lookups.v1.PhoneNumber> phoneStatus(String[] phones) {
		Map<String, com.twilio.rest.lookups.v1.PhoneNumber> results = new HashMap<String, com.twilio.rest.lookups.v1.PhoneNumber>();
		
		for (String phone : phones) {
			
			try {
				com.twilio.rest.lookups.v1.PhoneNumber phoneNumber = com.twilio.rest.lookups.v1.PhoneNumber.fetcher(
		                new com.twilio.type.PhoneNumber(phone))
						.setType(Promoter.listOfOne("carrier")).fetch();
				
				results.put(phone,phoneNumber);
			} catch (ApiException ex) {
				results.put(phone,null);
			}
		}
		
		return results;
	}

	public Map<String, com.twilio.rest.lookups.v1.PhoneNumber> phoneStatusAddons(String[] phones, String[] addons) {
		Map<String, com.twilio.rest.lookups.v1.PhoneNumber> results = new HashMap<String, com.twilio.rest.lookups.v1.PhoneNumber>();
		
		List<String> _addons = Arrays.asList(addons);

		for (String phone : phones) {
			try {
				com.twilio.rest.lookups.v1.PhoneNumber phoneNumber =
						com.twilio.rest.lookups.v1.PhoneNumber.fetcher(
								new com.twilio.type.PhoneNumber(phone)
		                )
						.setAddOns(_addons)
						.fetch();
				
				results.put(phone,phoneNumber);
			} catch (ApiException ex) {
				results.put(phone,null);
			}
		}
		
		return results;
	}
	
	public ResourceSet<Conference> getConferences() {
		
		return  Conference.reader().read();
	}

}
