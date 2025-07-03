package com.fuse.actions;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

@Namespace("/sso")
public class SSOUrls extends FSActionSupport {
	
	@Action( value= "saml", 
			results = {
					@Result(name = "redirect_to_saml2", location = "/saml2")
			}
	)
	public String SAMLRedirect() {
		return "redirect_to_saml2";
	}
	
	@Action( value= "oidc", 
			results = {
					@Result(name = "redirect_to_oauth", location = "/oauth"),
			}
	)
	public String OIDCRedirect() {
		return "redirect_to_oauth";
	}

}
