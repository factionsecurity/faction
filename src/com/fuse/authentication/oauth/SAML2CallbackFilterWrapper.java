package com.fuse.authentication.oauth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.pac4j.jee.filter.CallbackFilter;

public class SAML2CallbackFilterWrapper extends CallbackFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        this.setConfigOnly(SecurityConfigFactory.getCurrentConfig());

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String contextPath = httpRequest.getContextPath();
            setDefaultUrl(contextPath + "/saml2"); 
        }
        this.setRenewSession(true);
        this.setMultiProfile(true);

        super.doFilter(request, response, chain);
    }
}