package com.fuse.actions.appstore;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/appstore/AppStore.jsp", params = { "contentType", "text/html" })
public class AppStore extends FSActionSupport{
	
	@Action(value = "AppStore")
	public String execute() {
		List<AppStore> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	@Action(value = "AddApp")
	public String addApp() {
		List<AppStore> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	@Action(value = "DeleteApp")
	public String deleteApp() {
		List<AppStore> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	@Action(value = "DisableApp")
	public String disableApp() {
		List<AppStore> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
}
