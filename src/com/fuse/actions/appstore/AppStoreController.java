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
import java.util.stream.Collectors;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AppStore;
import com.fuse.dao.AuditLog;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;

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
	
	
	@SuppressWarnings("unchecked")
	@Action(value = "AppStoreDashboard")
	public String execute() {
		return SUCCESS;
	}
	
	@Action(value = "EnableApp")
	public String addApp() {
		AppStore app = (AppStore) em.createQuery("from AppStore where id = :id")
				.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		if(app != null) {
			app.setEnabled(true);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(app);
			HibHelper.getInstance().commit();
		}
		
		return SUCCESS;
	}
	@Action(value = "GetDetails", results = {
	     @Result(name = "json", location = "/WEB-INF/jsp/appstore/appJSON.jsp", params = { "contentType", "application/json" })
	})
	public String getDetails() {
		app = (AppStore) em.createQuery("from AppStore where id = :id")
				.setParameter("id", id).getResultList().stream().findFirst().orElse(null);
		return "json";
	}
	
	@Action(value = "DeleteApp")
	public String deleteApp() {
		List<AppStore> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	
	@Action(value = "DisableApp")
	public String disableApp() {
		List<AppStoreController> apps = em.createQuery("from AppStore where enabled = true order by order").getResultList();
		
		return SUCCESS;
	}
	@Action(value = "GetApps", results = {
	     @Result(name = "json", location = "/WEB-INF/jsp/appstore/appsJSON.jsp", params = { "contentType", "application/json" })
	})
	public String getApps() {
		List<AppStore> apps = em.createQuery("from AppStore").getResultList();
		assessmentApps = apps.stream()
				.filter( app -> app.getEnabled() && app.getAssessmentEnabled())
				.collect(Collectors.toList());
		vulnerabilityApps = apps.stream()
				.filter( app -> app.getEnabled() && app.getVulnerabilityEnabled())
				.collect(Collectors.toList());
		verificationApps = apps.stream()
				.filter( app -> app.getEnabled() && app.getVerificationEnabled())
				.collect(Collectors.toList());
		inventoryApps = apps.stream()
				.filter( app -> app.getEnabled() && app.getInventoryEnabled())
				.collect(Collectors.toList());
		disabledApps = apps.stream()
				.filter( app -> !app.getEnabled())
				.collect(Collectors.toList());
		
		return "json";
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
	
	
}
