package com.fuse.authentication.oauth;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pac4j.core.util.Pac4jConstants;

import java.io.IOException;

@WebFilter(filterName = "clearSavedRequestFilter")
public class ClearSavedRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false);

        if (session != null) {
            // Remove pac4j's saved original request so it won't form-POST replay it
            session.removeAttribute(Pac4jConstants.REQUESTED_URL);
        }

        chain.doFilter(req, res);
    }
}