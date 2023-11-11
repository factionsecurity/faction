package com.fuse.utils;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class S2045Filter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String ct = request.getContentType();
		if(ct != null){
			System.out.println("Checking S2045: "+  ct);
			ct = Pattern.compile("[^a-zA-Z0-9:;= \\-_/\\\\]", Pattern.DOTALL).matcher(ct).replaceAll("S2045");
			
			if(ct.contains("S2045")){
				System.out.println("Found S2045");
				return;/// return... don't chain the filter
			}
		}
		
		
		chain.doFilter(request, response);
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
