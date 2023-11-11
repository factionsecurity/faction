package com.fuse.utils;

import org.apache.struts2.ServletActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class CachingHeadersInterceptor extends AbstractInterceptor {
private static final long serialVersionUID = 1L;

public String intercept(ActionInvocation invocation) throws Exception {
	//System.out.println("Invoked the Interceptor");
    if (ServletActionContext.getResponse() != null) {
        ServletActionContext.getResponse().setHeader("Cache-control","no-cache, no-store, must-revalidate");
        ServletActionContext.getResponse().setHeader("Pragma", "no-cache");
        ServletActionContext.getResponse().setHeader("Expires", "-1");
        ServletActionContext.getResponse().setHeader("Vary", "*");
    }
    
    return invocation.invoke();
}

}