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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.python.bouncycastle.util.Arrays;

import java.util.Base64;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AppStore;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.utils.FSUtils;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/appstore/InstallExtension.jsp", params = { "contentType",
		"text/html" })
public class InstallExtensionController extends FSActionSupport {

	private InputStream stream;
	private File file_data;
	private String file_dataContentType;
	private String file_dataFileName;

	@Action(value = "InstallExtension")
	public String execute() {
		//TODO: Add AuthZ
		return SUCCESS;
	}

	@Action(value = "PreviewApp", results = { @Result(name = "json", type = "stream", params = { "contentType",
			"application/json", "inputName", "stream" }),
			@Result(name = "input", location = "/WEB-INF/jsp/uploadError.jsp") })
	public String uploadFile() throws IOException {
		//TODO: Add AuthZ

		/*
		 * if(!this.isAcadmin()) { AuditLog.notAuthorized( this,
		 * "User attempted to upload a app with name " + this.file_dataFileName, true);
		 * return LOGIN; }
		 */
		User user = this.getSessionUser();

		FileInputStream fis = new FileInputStream(file_data);
		AppStore preview = new AppStore();
		preview.parseJar(fis);

		String json = "{";
		json += " \"extension_info\": { \"title\": \"" + preview.getName() +"\", "
				+ "\"version\": \"" + preview.getVersion() + "\", "
				+ "\"author\": \"" + preview.getAuthor() + "\", "
				+ "\"url\": \"" + preview.getUrl() + "\", "
				+ "\"logo\": \"" + preview.getBase64Logo()+ "\", "
				+ "\"assessment\": " + preview.getAssessmentEnabled() + ", "
				+ "\"verification\": " + preview.getVerificationEnabled() + ", "
				+ "\"vulnerability\": " + preview.getVulnerabilityEnabled() + ", "
				+ "\"inventory\": " + preview.getInventoryEnabled() + ", "
				+ "\"description\": \"" + preview.getDescription() + "\"}"
				+ "}";
		ServletActionContext.getRequest().getSession().setAttribute("PreviewApp", preview);
		//byte [] jarBytes = Files.readAllBytes(Paths.get(file_data.toURI()));
		//String b64Jar = Base64.getEncoder().encodeToString(jarBytes);
		//ServletActionContext.getRequest().getSession().setAttribute("Extension", b64Jar);
		stream = new ByteArrayInputStream(json.toString().getBytes());
		return "json";
	}
	
	@Action(value = "InstallApp")
	public String installApp() throws IOException, ParseException {
		//TODO: Add AuthZ
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
