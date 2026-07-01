package com.fuse.authentication.oauth;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GitHub's OAuth profile (pac4j {@code GitHubProfileDefinition}) does not expose the
 * user's email, so it must be retrieved separately from the GitHub REST API using the
 * OAuth access token. This returns the verified primary email (falling back to any
 * verified email) so it can be matched against an existing FACTION user.
 */
public class GitHubEmailFetcher {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static String primaryVerifiedEmail(String accessToken) {
		if (accessToken == null || accessToken.trim().isEmpty()) {
			return null;
		}
		HttpURLConnection conn = null;
		try {
			URL url = new URL("https://api.github.com/user/emails");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "token " + accessToken);
			conn.setRequestProperty("Accept", "application/vnd.github+json");
			conn.setRequestProperty("User-Agent", "FACTION");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);

			if (conn.getResponseCode() != 200) {
				System.out.println("GitHub /user/emails returned status " + conn.getResponseCode());
				return null;
			}
			try (InputStream in = conn.getInputStream()) {
				JsonNode emails = MAPPER.readTree(in);
				String firstVerified = null;
				for (JsonNode node : emails) {
					String email = node.path("email").asText(null);
					if (email == null || email.isEmpty()) {
						continue;
					}
					boolean verified = node.path("verified").asBoolean(false);
					boolean primary = node.path("primary").asBoolean(false);
					if (verified && primary) {
						return email;
					}
					if (verified && firstVerified == null) {
						firstVerified = email;
					}
				}
				return firstVerified;
			}
		} catch (Exception e) {
			System.out.println("GitHub email fetch failed: " + e);
			return null;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
