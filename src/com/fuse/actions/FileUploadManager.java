package com.fuse.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.AuditLog;
import com.fuse.dao.Files;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;

@Namespace("/portal")
@Result(name="input", location="/WEB-INF/jsp/uploadError.jsp")
public class FileUploadManager extends FSActionSupport{
	
	
	private String name;
	private Long apid;
	private String delid;
	private InputStream stream;
	private File file_data;
	private String file_dataContentType;
	private String file_dataFileName;
	
	
	/// These are file deleted from assessment pages
	@Action(value="DeleteFile", 
			results = {@Result(
				name="json",type = "stream"
				, params = {
						"contentType", "application/json", 
				        "inputName", "stream"})})
	public String deleteFile() {
		User user = this.getSessionUser();
		if(user == null)
			return LOGIN;
		
		if(!(this.isAcassessor() || this.isAcmanager())) {
				AuditLog.notAuthorized(
						this, "User attempted delete a file", true);
			
			return LOGIN;
		}
		
		
		Files f = (Files)em
				.createQuery("from Files where name = :name and entityId = :eid and type = :type and uuid = :id")
				.setParameter("id", Long.parseLong(delid))
				.setParameter("name", name)
				.setParameter("eid", apid)
				.setParameter("type", "assessment")
				.getResultList().stream().findFirst().orElse(null);
		if(f != null){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(f);
			HibHelper.getInstance().commit();
		}
		stream = new ByteArrayInputStream( "{}".toString().getBytes());
		return "json";
	}
	
	// Gets files from the session when creating an engagement.
	@Action(value="GetEngFile", 
			results = {@Result(
					name="data",type = "stream"
					, params = {
							"contentType", "${file_dataContentType}",
							"contentDisposition" , "attachment; filename=${file_dataFileName}",
					        "inputName", "stream"})})
	public String getEngFile() {
		if(!(this.isAcengagement() || this.isAcassessor() || this.isAcmanager())) {
			AuditLog.notAuthorized(
					this, "User attempted access a file", true);
			
			return LOGIN;
		}
		User user = this.getSessionUser();
		
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		Files f = (Files) em.createQuery("from Files where uuid = :id").setParameter("id", name).getResultList().stream().findFirst().orElse(null);
		file_dataContentType=f.getContentType();
		file_dataFileName = f.getName();

		stream = new ByteArrayInputStream( f.getRealFile());
		return "data";
	}
	
	// Gets files from the session when creating an engagement.
	@Action(value="GetEngFile2", 
			results = {@Result(
					name="data",type = "stream"
					, params = {
							"contentType", "${file_dataContentType}",
							"contentDisposition" , "attachment; filename=${file_dataFileName}",
					        "inputName", "stream"})})
	public String getSessionFile() {
		if(!(this.isAcengagement() )) {
			AuditLog.notAuthorized(
					this, "User attempted to access a file", true);
			
			return LOGIN;
		}
		User user = this.getSessionUser();
		
		Map<String,Files> files = (Map<String,Files>) ServletActionContext
				.getRequest().getSession().getAttribute("Files");
		Files f = files.get(name);
		
		file_dataContentType=f.getContentType();
		file_dataFileName = f.getName();

		stream = new ByteArrayInputStream( f.getRealFile());
		return "data";
	}
	
	// These are files deleted from Engagement Edit pages.
	@Action(value="DeleteEngFile", 
			results = {@Result(
					name="json",type = "stream"
					, params = {
							"contentType", "application/json", 
					        "inputName", "stream"})})
	public String deleteEngFile() {
		if(!(this.isAcengagement() || this.isAcassessor() || this.isAcmanager())) {
			AuditLog.notAuthorized(
					this, "User attempted to delete a file", true);
			return LOGIN;
		}User user = this.getSessionUser();
		

		Files f = (Files)em
				.createQuery("from Files where name = :name and entityId = :eid and type = :type")
				.setParameter("name", name)
				.setParameter("eid", apid)
				.setParameter("type", "assessment")
				.getResultList().stream().findFirst().orElse(null);
		if(f != null){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(f);
			HibHelper.getInstance().commit();
			
		}
		stream = new ByteArrayInputStream( "{}".toString().getBytes());
		return "json";
		
	}
	
	// These are files deleted from Engagement Create pages.
	@Action(value="DeleteEngFile2", 
			results = {@Result(
					name="json",type = "stream"
					, params = {
							"contentType", "application/json", 
					        "inputName", "stream"})})
	public String deleteFromSession() {
		
		if(!(this.isAcengagement())) {
			AuditLog.notAuthorized(
					this, "User attempted to delete a file", true);
			return LOGIN;
		}
		User user = this.getSessionUser();
		
		Map<String,Files> files = (Map<String,Files>) ServletActionContext
				.getRequest().getSession().getAttribute("Files");
		files.remove(name);
		stream = new ByteArrayInputStream( "{}".toString().getBytes());
		return "json";
	}
	
	@Action(value="UploadFile", 
			results = {@Result(
				name="json",type = "stream"
				, params = {
						"contentType", "application/json", 
				        "inputName", "stream"})})
	public String uploadFile() throws FileNotFoundException, IOException {
		
		if(!(this.isAcengagement() || this.isAcassessor() || this.isAcmanager())) {
			AuditLog.notAuthorized(
					this, "User attempted to upload a file with name " + this.file_dataFileName, true);
			return LOGIN;
		}
		User user = this.getSessionUser();
		
		String uuid = UUID.randomUUID().toString();
		
		Files f = new Files();
		f.setUuid(uuid);
		f.setContentType(this.file_dataContentType);
		f.setName(this.file_dataFileName);
		f.setType(Files.ASSESSMENT);
		FileInputStream fis = new FileInputStream(file_data);
		byte[] bytesArray = new byte[(int) file_data.length()];
		fis.read(bytesArray); //read file into bytes[]
		fis.close();
		f.setRealFile(bytesArray);
		
		if(apid != null && !apid.equals("")){  // if there is an apId then we can add this to the current application
			f.setEntityId(apid);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(f);
			HibHelper.getInstance().commit();
			
		}else{ // otherwise lets save it in the session for now. 
			Map<String,Files> files = (Map<String,Files>) request.getSession().getAttribute("Files");
			if(files == null){
				files = new HashMap<String, Files>();
				//System.out.println("Files is null");
			}
			files.put(uuid, f);
			ServletActionContext.getRequest().getSession().setAttribute("Files", files);
			
		}
	
		String uid = "A"+ UUID.randomUUID().toString();
		String json = "{";
		
		
		if(apid != null && !apid.equals("")){ 
			json += " \"initialPreview\" : [ \"GetEngFile?name=" + uuid + "\"],";
			json += "\"initialPreviewConfig\" : [{ \"caption\": \""+ f.getName()+"\", \"width\" : \"160px\", \"height\": \"160px\", "
					+ "\"key\" : \""+ f.getUuid() +"\",\"filename\" : \""+f.getName()+"\","
					+ "\"url\" : \"DeleteEngFile?delid=" + f.getUuid() +"&apid=" + f.getEntityId() + "&name=" + f.getName() +"\", " 
					+ "\"filetype\": \""+ f.getContentType() +"\", \"type\":\"image\"}]";
		}else {
			json += " \"initialPreview\" : [ \"GetEngFile2?name=" + uuid + "\"],";
			json += "\"initialPreviewConfig\" : [{ \"caption\": \""+ f.getName()+"\", \"width\" : \"160px\", \"height\": \"160px\", "
					+ "\"key\" : \""+ f.getUuid() +"\",\"filename\" : \""+f.getName()+"\","
					+ "\"url\" : \"DeleteEngFile2?name=" + uuid + "\","
					+ "\"filetype\": \""+ f.getContentType() +"\", \"type\":\"image\"}]";	
		}
		json += "}";
		stream = new ByteArrayInputStream( json.toString().getBytes());
		return "json";
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setApid(Long apid) {
		this.apid = apid;
	}


	public void setDelid(String delid) {
		this.delid = delid;
	}


	public void setFile_data(File file_data) {
		this.file_data = file_data;
	}


	public void setFile_dataContentType(String file_dataContentType) {
		this.file_dataContentType = file_dataContentType;
	}


	public void setFile_dataFileName(String file_dataFileName) {
		this.file_dataFileName = file_dataFileName;
	}


	public InputStream getStream() {
		return stream;
	}

	public String getFile_dataContentType() {
		return file_dataContentType;
	}

	public String getFile_dataFileName() {
		return file_dataFileName;
	}
	



	
		

}
