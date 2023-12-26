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
	
	@Action(value="PreviewApp", results = {
			@Result(name="json", type="stream", params={
						"contentType", "application/json", 
				        "inputName", "stream"}),
@Result(name="input", location="/WEB-INF/jsp/uploadError.jsp")
			})
	public String uploadFile() throws IOException {
		
		/*if(!this.isAcadmin()) {
			AuditLog.notAuthorized(
					this, "User attempted to upload a app with name " + this.file_dataFileName, true);
			return LOGIN;
		}*/
		User user = this.getSessionUser();
		
		
		FileInputStream fis = new FileInputStream(file_data);
		JarInputStream jarStream = new JarInputStream(fis);
		Manifest manifest = jarStream.getManifest();
		Attributes attr = manifest.getMainAttributes();
		String title = attr.getValue("Title");
		String author = attr.getValue("Author");
		String version = attr.getValue("Version");
		String url = attr.getValue("URL");
		JarEntry entry;
		String description = "";
		byte [] logo = new byte [1024*10];
		while ((entry = jarStream.getNextJarEntry()) != null) {
		    if (!entry.isDirectory() && entry.getName().endsWith("description.md")) {
		    	byte[] readme = new byte [300];
		    	jarStream.read(readme);
		    	description = new String(readme);
		    }
		    if (!entry.isDirectory() && entry.getName().endsWith("logo.png")) {
		    	jarStream.read(logo);
		    }
		}
		fis.close();
		
		//ServletActionContext.getRequest().getSession().setAttribute("Files", files);
			
		String uid = "A"+ UUID.randomUUID().toString();
		String json = "{";
		
		
		json += " \"initialPreview\" : [ ],";
		json += "\"initialPreviewConfig\" : [{ \"caption\": \""+ title +"\", \"width\" : \"160px\", \"height\": \"160px\", "
				+ "\"key\" : \""+ uid +"\",\"filename\" : \""+ title +"\","
				+ "\"url\" : \""+ uid + "\","
				+ "\"filetype\": \"application/java-archive\", \"type\":\"jar\"}]";	
		json += "}";
		stream = new ByteArrayInputStream( json.toString().getBytes());
		return "json";
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
	
	
}
