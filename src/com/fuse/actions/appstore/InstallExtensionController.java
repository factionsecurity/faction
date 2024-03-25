package com.fuse.actions.appstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.hibernate.Session;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.python.bouncycastle.util.Arrays;

import java.util.Base64;
import java.util.List;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AppStore;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.utils.FSUtils;
import com.opensymphony.xwork2.interceptor.annotations.Before;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/appstore/InstallExtension.jsp", params = { "contentType",
		"text/html" })
public class InstallExtensionController extends FSActionSupport {

	private InputStream stream;
	private File file_data;
	private String file_dataContentType;
	private String file_dataFileName;
	private String uuid;
	private AppStore app;
	
	@Before(priority=1)
	public String authorization() {
		 if(this.isAppStoreEnabled() && !this.isAcadmin()) { 
			 AuditLog.notAuthorized( this,
				 "Invalid Access to App Store", true);
			 return LOGIN; 
		 }else {
			 return null;
		 }
	}

	@Action(value = "InstallExtension")
	public String execute() {
		return SUCCESS;
	}
	
	@Action(value = "UpdateExtension", results = {
			@Result(name = "update", location = "/WEB-INF/jsp/appstore/UpdateExtension.jsp") })
	public String updateExtension() {
		
		app = (AppStore) em.createQuery("from AppStore where uuid = :uuid")
				.setParameter("uuid", uuid)
				.getResultList()
				.stream()
				.findFirst()
				.orElse(null);
		
		return "update";
	}
	
	@Action(value = "PreviewUpdate", results = { @Result(name = "json", type = "stream", params = { "contentType",
			"application/json", "inputName", "stream" }),
			@Result(name = "input", location = "/WEB-INF/jsp/uploadError.jsp") })
	public String uploadUpdate() throws IOException, ParseException {
		
		AppStore app = (AppStore) em.createQuery("from AppStore where uuid = :uuid")
				.setParameter("uuid", uuid)
				.getResultList()
				.stream()
				.findFirst()
				.orElse(null);
		
		FileInputStream fis = new FileInputStream(file_data);
		app.updateApp(fis);
		String json = app.getMeta();
		ServletActionContext.getRequest().getSession().setAttribute("PreviewApp", app);
		stream = new ByteArrayInputStream(json.toString().getBytes());
		return "json";
	}
	
	

	@Action(value = "PreviewApp", results = { @Result(name = "json", type = "stream", params = { "contentType",
			"application/json", "inputName", "stream" }),
			@Result(name = "input", location = "/WEB-INF/jsp/uploadError.jsp") })
	public String uploadFile() throws IOException, ParseException {
		
		FileInputStream fis = new FileInputStream(file_data);
		AppStore preview = new AppStore();
		preview.parseJar(fis);

		String json = preview.getMeta();
		ServletActionContext.getRequest().getSession().setAttribute("PreviewApp", preview);
		stream = new ByteArrayInputStream(json.toString().getBytes());
		return "json";
	}
	
	@Action(value = "InstallApp")
	public String installApp() throws IOException, ParseException {
		AppStore app = (AppStore) ServletActionContext.getRequest().getSession().getAttribute("PreviewApp");
		Boolean alreadyInstalled =em.createQuery("from AppStore where hash = :hash")
			.setParameter("hash", app.getHash())
			.getResultList()
			.stream()
			.findAny()
			.isPresent();
		if(alreadyInstalled) {
			_message="Already Installed";
			_result="error";
			return MESSAGEJSON;
		}
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(app);
		HibHelper.getInstance().commit();
		
		_result="success";
		return MESSAGEJSON;
		
	}
	@Action(value = "UpdateApp")
	public String updateApp() throws IOException, ParseException {
		AppStore app = (AppStore) ServletActionContext.getRequest().getSession().getAttribute("PreviewApp");
		List<AppStore> apps =em.createQuery("from AppStore where hash = :hash or uuid = :uuid")
			.setParameter("hash", app.getHash())
			.setParameter("uuid", app.getUuid())
			.getResultList();
		
		Boolean alreadyInstalled = apps.stream()
				.filter( a -> a.getHash().equals(app.getHash()))
				.findAny()
				.isPresent();
			
			
		if(alreadyInstalled) {
			_message="Same as Installed Extension";
			_result="error";
			return MESSAGEJSON;
		}
		
		Boolean isInstalled = apps.stream()
				.filter( a -> a.getUuid().equals(app.getUuid()))
				.findAny()
				.isPresent();
		
		if(!isInstalled) {
			_message="Can't find and existing installation";
			_result="error";
			return MESSAGEJSON;
		}
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.merge(app);
		HibHelper.getInstance().commit();
		
		_result="success";
		return MESSAGEJSON;
		
	}
	
	public String getActiveAppStore() {
		return "active";
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public File getFile_data() {
		return file_data;
	}

	public void setFile_data(File file_data) {
		this.file_data = file_data;
	}

	public String getFile_dataContentType() {
		return file_dataContentType;
	}

	public void setFile_dataContentType(String file_dataContentType) {
		this.file_dataContentType = file_dataContentType;
	}

	public String getFile_dataFileName() {
		return file_dataFileName;
	}

	public void setFile_dataFileName(String file_dataFileName) {
		this.file_dataFileName = file_dataFileName;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public AppStore getApp() {
		return this.app;
	}
}
