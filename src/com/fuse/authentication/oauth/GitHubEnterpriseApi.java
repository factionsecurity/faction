package com.fuse.authentication.oauth;

import com.github.scribejava.apis.GitHubApi;

/**
 * scribejava's {@link GitHubApi} hardcodes the github.com OAuth endpoints. GitHub
 * Enterprise Server exposes the same OAuth flow on the appliance host
 * ({base}/login/oauth/*), so this only swaps the endpoint URLs and inherits the
 * verb/token-extractor behavior.
 */
public class GitHubEnterpriseApi extends GitHubApi {

	private final String baseUrl;

	public GitHubEnterpriseApi(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	public String getAccessTokenEndpoint() {
		return baseUrl + "/login/oauth/access_token";
	}

	@Override
	protected String getAuthorizationBaseUrl() {
		return baseUrl + "/login/oauth/authorize";
	}
}
