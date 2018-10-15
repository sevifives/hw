package me.sevifives;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;

public class HelloWorldConfiguration extends Configuration {
	
	@NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";
    
    @NotEmpty
    private String accountSid;
    
    @NotEmpty
    private String accountToken;
    
    @NotEmpty
    private String accountPhone;

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }

    @JsonProperty
	public String getAccountSid() {
		return accountSid;
	}

    @JsonProperty
	public void setAccountSid(String accountSid) {
		this.accountSid = accountSid;
	}

    @JsonProperty
	public String getAccountToken() {
		return accountToken;
	}

    @JsonProperty
	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
	}

    @JsonProperty
	public String getAccountPhone() {
		return accountPhone;
	}

	@JsonProperty
	public void setAccountPhone(String accountPhone) {
		this.accountPhone = accountPhone;
	}
}
