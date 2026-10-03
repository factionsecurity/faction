package com.fuse.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Test;

/**
 * Emailed links (password reset, invitations) must not be built from the
 * request's Host header, which an attacker controls. They come from the
 * configured public base URL, with the request as a last resort.
 */
public class PublicBaseUrlTest {

	@After
	public void clearProps() {
		System.clearProperty("FACTION_BASE_URL");
		System.clearProperty("FACTION_OAUTH_CALLBACK");
	}

	private static HttpServletRequest request() {
		HttpServletRequest r = mock(HttpServletRequest.class);
		when(r.getRequestURL()).thenReturn(new StringBuffer("http://attacker.example/portal/sendReset"));
		when(r.getRequestURI()).thenReturn("/portal/sendReset");
		when(r.getContextPath()).thenReturn("");
		return r;
	}

	@Test
	public void configuredBaseUrlWinsOverTheHostHeader() {
		System.setProperty("FACTION_BASE_URL", "https://faction.example.com/");
		assertEquals("https://faction.example.com", FSUtils.publicBaseUrl(request()));
	}

	@Test
	public void oauthCallbackIsUsedWhenNoBaseUrlIsConfigured() {
		System.setProperty("FACTION_OAUTH_CALLBACK", "https://sso.example.com");
		assertEquals("https://sso.example.com", FSUtils.publicBaseUrl(request()));
	}

	@Test
	public void fallsBackToTheRequestOnlyWhenNothingIsConfigured() {
		assertEquals("http://attacker.example", FSUtils.publicBaseUrl(request()));
	}
}
