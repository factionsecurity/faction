package com.fuse.authentication.oauth;

import org.pac4j.oauth.client.GitHubClient;

/**
 * A {@link GitHubClient} pointed at a GitHub Enterprise Server appliance instead of
 * github.com. pac4j's {@code GitHubClient.clientInit()} unconditionally installs the
 * github.com API endpoints and profile definition, so this swaps them out after
 * {@code super.clientInit()} runs; pac4j builds the OAuth service from the
 * configuration on every request, so the late swap is picked up everywhere.
 */
public class GitHubEnterpriseClient extends GitHubClient {

	private final String baseUrl;

	public GitHubEnterpriseClient(String key, String secret, String baseUrl) {
		super(key, secret);
		this.baseUrl = baseUrl;
	}

	/**
	 * REST API root on an enterprise appliance ({base}/api/v3, vs https://api.github.com
	 * for github.com).
	 */
	public String getApiBaseUrl() {
		return baseUrl + "/api/v3";
	}

	@Override
	protected void clientInit() {
		super.clientInit();
		getConfiguration().setApi(new GitHubEnterpriseApi(baseUrl));
		getConfiguration().setProfileDefinition(new GitHubEnterpriseProfileDefinition(getApiBaseUrl()));
	}
}
