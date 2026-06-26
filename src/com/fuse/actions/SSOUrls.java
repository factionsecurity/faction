package com.fuse.actions;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.SystemSettings;

@Namespace("/sso")
public class SSOUrls extends FSActionSupport {

	private SystemSettings settings() {
		return (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
				.findFirst().orElse(null);
	}

	@Action( value= "saml",
			results = {
					@Result(name = "redirect_to_saml2", location = "/saml2"),
					@Result(name = "login", type = "redirect", location = "/")
			}
	)
	public String SAMLRedirect() {
		// Guard: if SAML2 isn't configured there is no client to redirect to and
		// forwarding to /saml2 would 404. Send the user back to the login page.
		SystemSettings ss = settings();
		if (ss == null || ss.getSaml2MetaUrl() == null || ss.getSaml2MetaUrl().trim().isEmpty()) {
			return "login";
		}
		return "redirect_to_saml2";
	}

	@Action( value= "oidc",
			results = {
					@Result(name = "redirect_to_oauth", location = "/oauth"),
					@Result(name = "login", type = "redirect", location = "/")
			}
	)
	public String OIDCRedirect() {
		// Guard: if OIDC isn't configured, avoid forwarding to /oauth (no client -> 404).
		SystemSettings ss = settings();
		if (ss == null || ss.getOauthDiscoveryURI() == null || ss.getOauthDiscoveryURI().trim().isEmpty()) {
			return "login";
		}
		return "redirect_to_oauth";
	}

	@Action( value= "github",
			results = {
					@Result(name = "redirect_to_github", location = "/github"),
					@Result(name = "login", type = "redirect", location = "/")
			}
	)
	public String GitHubRedirect() {
		// Guard: if GitHub isn't configured, avoid forwarding to /github (no client -> 404).
		SystemSettings ss = settings();
		if (ss == null || ss.getGithubClientId() == null || ss.getGithubClientId().trim().isEmpty()) {
			return "login";
		}
		return "redirect_to_github";
	}

}
