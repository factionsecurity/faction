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
		return SUCCESS;
	}

	@Action(value = "PreviewApp", results = { @Result(name = "json", type = "stream", params = { "contentType",
			"application/json", "inputName", "stream" }),
			@Result(name = "input", location = "/WEB-INF/jsp/uploadError.jsp") })
	public String uploadFile() throws IOException {

		/*
		 * if(!this.isAcadmin()) { AuditLog.notAuthorized( this,
		 * "User attempted to upload a app with name " + this.file_dataFileName, true);
		 * return LOGIN; }
		 */
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
		ByteArrayOutputStream logo = new ByteArrayOutputStream();
		ByteArrayOutputStream description = new ByteArrayOutputStream();
		while ((entry = jarStream.getNextJarEntry()) != null) {
			if (!entry.isDirectory() && entry.getName().endsWith("description.md")) {
				while(jarStream.available() == 1) {
					byte[] data = new byte[512];
					int size = jarStream.read(data);
					if(size == -1)
						break;
					description.write(data, 0, size);
				}
			}
			if (!entry.isDirectory() && entry.getName().endsWith("logo.png")) {
				while(jarStream.available() == 1) {
					byte[] data = new byte[512];
					int size = jarStream.read(data);
					if(size == -1)
						break;
					logo.write(data, 0, size);
				}
			}
		}
		fis.close();
		
		String base64Description =URLEncoder.encode(Base64.getEncoder().encodeToString(description.toByteArray()),"UTF-8");
		String base64Logo = URLEncoder.encode(Base64.getEncoder().encodeToString(logo.toByteArray()),"UTF-8");


		String json = "{";
		json += " \"extension_info\": { \"title\": \"" + title +"\", "
				+ "\"version\": \"" +version + "\", "
				+ "\"author\": \"" + author + "\", "
				+ "\"url\": \"" + url + "\", "
				+ "\"logo\": \"" + base64Logo+ "\", "
				+ "\"description\": \"" + base64Description + "\"}"
				+ "}";
		ServletActionContext.getRequest().getSession().setAttribute("Extension_Info", json);
		byte [] jarBytes = Files.readAllBytes(Paths.get(file_data.toURI()));
		String b64Jar = Base64.getEncoder().encodeToString(jarBytes);
		ServletActionContext.getRequest().getSession().setAttribute("Extension", b64Jar);
		stream = new ByteArrayInputStream(json.toString().getBytes());
		return "json";
	}
	@Action(value = "InstallApp", results = { @Result(name = "json", type = "stream", params = { "contentType",
			"application/json", "inputName", "stream" }),
			@Result(name = "input", location = "/WEB-INF/jsp/uploadError.jsp") })
	public String installApp() throws IOException, ParseException {
		String infoString = (String) ServletActionContext.getRequest().getSession().getAttribute("Extension_Info");
		String jarFile = (String) ServletActionContext.getRequest().getSession().getAttribute("Extension");
		JSONParser parser = new JSONParser();
		JSONObject extensionInfo = (JSONObject) parser.parse(infoString);
		JSONObject info = (JSONObject) extensionInfo.get("extension_info");
		
		AppStore app = new AppStore();
		app.setApproved(false);
		app.setAssessmentEnabled(false);
		app.setVerificationEnabled(false);
		app.setVulnerabilityEnabled(false);
		app.setInventoryEnabled(false);
		app.setAuthor(info.get("author").toString());
		app.setBase64JarFile(jarFile);
		app.setBase64Logo(info.get("logo").toString());
		app.setName(info.get("title").toString());
		app.setDescription(info.get("description").toString());
		app.setEnabled(false);
		app.setVersion(info.get("version").toString());
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(app);
		HibHelper.getInstance().commit();
		JSONObject success = new JSONObject();
		success.put("message", "success");
		stream = new ByteArrayInputStream(success.toJSONString().getBytes());
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
