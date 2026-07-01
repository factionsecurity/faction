package com.fuse.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.ServletActionContext;

import com.fuse.dao.User;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
	
public class AccessControlInterceptor extends AbstractInterceptor{


	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		
		
		User user = (User) ServletActionContext.getRequest().getSession(true).getAttribute("user");
		
		if (user != null) {
			ActionContext.getContext().put("user", user);
			ActionContext.getContext().put("isAdmin", user.getPermissions().isAdmin());
			ActionContext.getContext().put("isManager", user.getPermissions().isManager());
			ActionContext.getContext().put("isAssessor", user.getPermissions().isAssessor());
			ActionContext.getContext().put("isEngagement", user.getPermissions().isEngagement());
			ActionContext.getContext().put("isRemediation", user.getPermissions().isRemediation());
		} else {
			// Allow unauthenticated access to the root namespace (login, reset, setup)
			// and to the /sso namespace (SSO entry points that redirect to the IdP).
			// All other namespaces require an active session.
			String namespace = invocation.getProxy().getNamespace();
			if (!"/".equals(namespace) && !"/sso".equals(namespace)) {
				return "login";
			}
		}
		return invocation.invoke();
	}
	

}
