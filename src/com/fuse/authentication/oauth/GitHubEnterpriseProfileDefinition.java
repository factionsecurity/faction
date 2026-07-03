package com.fuse.authentication.oauth;

import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.profile.github.GitHubProfileDefinition;

import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * pac4j's {@link GitHubProfileDefinition} hardcodes https://api.github.com/user.
 * On GitHub Enterprise Server the REST API lives under {base}/api/v3.
 */
public class GitHubEnterpriseProfileDefinition extends GitHubProfileDefinition {

	private final String apiBaseUrl;

	public GitHubEnterpriseProfileDefinition(String apiBaseUrl) {
		this.apiBaseUrl = apiBaseUrl;
	}

	@Override
	public String getProfileUrl(OAuth2AccessToken accessToken, OAuth20Configuration configuration) {
		return apiBaseUrl + "/user";
	}
}
