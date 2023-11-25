package com.fuse.authentication.oauth;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.pac4j.j2e.filter.SecurityFilter;
 
public class SecurityFilterWrapper extends SecurityFilter{
	
    private static SecurityFilter filter;
    
	public static SecurityFilter getInstance() {
		return filter;
	}
	
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        filter = this;
	}
 
}