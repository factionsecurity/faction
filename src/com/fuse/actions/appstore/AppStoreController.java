package com.fuse.actions.appstore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AuditLog;
import com.fuse.dao.User;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/appstore/AppStoreDashboard.jsp", params = { "contentType", "text/html" })
public class AppStoreController extends FSActionSupport{
	
	private InputStream stream;
	private File file_data;
	private String file_dataContentType;
	private String file_dataFileName;
	
	@Action(value = "AppStoreDashboard")
	public String execute() {
		List<AppStoreController> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	@Action(value = "AddApp")
	public String addApp() {
		List<AppStoreController> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	@Action(value = "DeleteApp")
	public String deleteApp() {
		List<AppStoreController> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	@Action(value = "DisableApp")
	public String disableApp() {
		List<AppStoreController> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	
	
}
