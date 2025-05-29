package com.fuse.authentication.oauth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.pac4j.jee.filter.SecurityFilter;

public class SecurityFilterWrapper extends SecurityFilter {

	private static SecurityFilter filter;

	public static SecurityFilter getInstance() {
		return filter;
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		filter = this;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// Set the latest config before each request
		setConfigOnly(SecurityConfigFactory.getCurrentConfig());

		super.doFilter(request, response, chain);
	}

}