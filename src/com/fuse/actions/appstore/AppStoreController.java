package com.fuse.actions.appstore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AppStore;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.opensymphony.xwork2.interceptor.annotations.Before;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/appstore/AppStoreDashboard.jsp", params = { "contentType", "text/html" })
public class AppStoreController extends FSActionSupport{
	
	private InputStream stream;
	private File file_data;
	private String file_dataContentType;
	private String file_dataFileName;
	private List<AppStore> assessmentApps;
	private List<AppStore> vulnerabilityApps;
	private List<AppStore> verificationApps;
	private List<AppStore> inventoryApps;
	private List<AppStore> disabledApps;
	private Long id; 
	private AppStore app;
	private String appList;
	private String appType;
	private String configs;
	
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
	
	
	@SuppressWarnings("unchecked")
	@Action(value = "AppStoreDashboard")
	public String execute() {
		return SUCCESS;
	}
	
	@Action(value = "EnableApp")
	public String enableApp() {
		AppStore app = (AppStore) em.createQuery("from AppStore where id = :id")
				.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		if(app != null) {
			app.setEnabled(true);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(app);
			HibHelper.getInstance().commit();
		}
		_result="success";
		return MESSAGEJSON;
	}
	
	@Action(value = "DisableApp")
	public String disableApp() {
		AppStore app = (AppStore) em.createQuery("from AppStore where id = :id")
				.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		if(app != null) {
			app.setEnabled(false);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(app);
			HibHelper.getInstance().commit();
			_result="success";
			return MESSAGEJSON;
		}else {
			_result="error";
			_message="App Not Found";
			return MESSAGEJSON;
			
		}
	}
	
	@Action(value = "GetDetails", results = {
	     @Result(name = "jsonApp", location = "/WEB-INF/jsp/appstore/appJSON.jsp", params = { "contentType", "application/json" })
	})
	public String getDetails() {
		app = (AppStore) em.createQuery("from AppStore where id = :id")
				.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		return "jsonApp";
	}
	
	@Action(value = "DeleteApp")
	public String deleteApp() {
		AppStore app = (AppStore) em.createQuery("from AppStore where id = :id")
				.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		if(app != null) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(app);
			HibHelper.getInstance().commit();
			_result="success";
			return MESSAGEJSON;
		}else {
			_result="error";
			_message="App Not Found";
			return MESSAGEJSON;
			
		}
	}
	
	@Action(value = "UpdateConfigs")
	public String updateConfigs() throws ParseException {
		AppStore app = (AppStore) em.createQuery("from AppStore where id = :id")
				.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		if(app != null) {
			JSONParser parse = new JSONParser();
			JSONObject jsonConfigs = (JSONObject) parse.parse(configs);
			for(Object key : jsonConfigs.keySet()) {
				app.updateJSONConfig(key.toString(), jsonConfigs.get(key).toString());
			}
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(app);
			HibHelper.getInstance().commit();
			_result="success";
			return MESSAGEJSON;
		}else {
			_result="error";
			_message="App Not Found";
			return MESSAGEJSON;
			
		}
	}
	
	
	@Action(value = "ChangeOrder")
	public String changeOrder() {
		List<AppStore> apps = em.createQuery("from AppStore").getResultList();
		List<String> appIdsString = Arrays.asList(appList.split(","));
		List<Long> appIds = appIdsString
				.stream()
				.mapToLong( v -> Long.parseLong(v))
				.boxed()
				.collect(Collectors.toList());
		
		
		Integer [] index = {0};
		for(Long id : appIds) {
			AppStore app = apps.stream().filter( a -> a.getId().equals(id)).findFirst().orElse(null);
			switch (appType) {
				case "assessment":
					app.setAssessmentOrder(index[0]);
					break;
				case "verification":
					app.setVerificationOrder(index[0]);
					break;
				case "vulnerability":
					app.setVulnerabilityOrder(index[0]);
					break;
				case "inventory":
					app.setInventoryOrder(index[0]);
					break;

				default:
					break;
			}
			index[0]++;
		}
		
		apps
		.stream()
		.filter( a -> !appIds.contains(a.getId()))
		.forEach( (a) -> {  
			switch (appType) {
				case "assessment":
					a.setAssessmentOrder(index[0]);
					break;
				case "verification":
					a.setVerificationOrder(index[0]);
					break;
				case "vulnerability":
					a.setVulnerabilityOrder(index[0]);
					break;
				case "inventory":
					a.setInventoryOrder(index[0]);
					break;
				default:
					break;
			}
			index[0]++;
		}
		);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		apps.forEach(a -> em.persist(a));
		HibHelper.getInstance().commit();
		
		_result="success";
		return MESSAGEJSON;
	}
	
	
	@Action(value = "GetApps", results = {
	     @Result(name = "jsonApps", location = "/WEB-INF/jsp/appstore/appsJSON.jsp", params = { "contentType", "application/json" })
	})
	public String getApps() {
		List<AppStore> apps = em.createQuery("from AppStore").getResultList();
		assessmentApps = apps.stream()
				.filter( app -> app.getEnabled() && (app.getAssessmentEnabled() || app.getReportEnabled()))
				.sorted((a1,a2)-> Integer.compare(a1.getAssessmentOrder(), a2.getAssessmentOrder()))
				.collect(Collectors.toList());
		vulnerabilityApps = apps.stream()
				.filter( app -> app.getEnabled() && app.getVulnerabilityEnabled())
				.sorted((a1,a2)-> Integer.compare(a1.getVulnerabilityOrder(), a2.getVulnerabilityOrder()))
				.collect(Collectors.toList());
		verificationApps = apps.stream()
				.filter( app -> app.getEnabled() && app.getVerificationEnabled())
				.sorted((a1,a2)-> Integer.compare(a1.getVerificationOrder(), a2.getVerificationOrder()))
				.collect(Collectors.toList());
		inventoryApps = apps.stream()
				.filter( app -> app.getEnabled() && app.getInventoryEnabled())
				.sorted((a1,a2)-> Integer.compare(a1.getInventoryOrder(), a2.getInventoryOrder()))
				.collect(Collectors.toList());
		disabledApps = apps.stream()
				.filter( app -> !app.getEnabled())
				.collect(Collectors.toList());
		
		return "jsonApps";
	}
	public String getActiveAppStore() {
		return "active";
	}

	public List<AppStore> getAssessmentApps() {
		return assessmentApps;
	}

	public List<AppStore> getVulnerabilityApps() {
		return vulnerabilityApps;
	}

	public List<AppStore> getVerificationApps() {
		return verificationApps;
	}

	public List<AppStore> getInventoryApps() {
		return inventoryApps;
	}

	public List<AppStore> getDisabledApps() {
		return disabledApps;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public AppStore getApp() {
		return this.app;
	}

	public void setAppList(String appList) {
		this.appList = appList;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public void setConfigs(String configs) {
		this.configs = configs;
	}
	
	
	
}
