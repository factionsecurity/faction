package com.fuse.dao;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringEscapeUtils;

import org.apache.commons.codec.binary.Base64;

@Entity
public class Files implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "fileGen")
    @TableGenerator(
        name = "fileGen",
        table = "fileGenseq",
        pkColumnValue = "file",
        valueColumnName = "nextFile",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	@ManyToOne
	private User creator;
	private Long creatorId;
	private String type; //assessment, verification, engagement
	private Long entityId;
	private String contentType;
	private String file;
	private String name;
	private String uuid;
	@Transient
	public static String ASSESSMENT = "assessment";
	@Transient
	public static String VERIFICATION = "verification";
	@Transient
	public static String ENGAGEMENT = "engagement";
	@Transient
	public static String VULNERABILITY = "vulnerability";
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public Long getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(Long creatorId) {
		this.creatorId = creatorId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getEntityId() {
		return entityId;
	}
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public byte [] getRealFile(){
		try {
			return Base64.decodeBase64(this.file.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public void setRealFile(byte [] file){
		
		this.file = Base64.encodeBase64String(file).replace("\r", "").replace("\n", "");
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFileStr(){
		return new String(this.getRealFile());
	}
	public String getFileStrJs(){
		return new StringEscapeUtils().escapeJava(new String(this.getRealFile()));
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getFileExtType() {
		if(this.contentType.contains("image"))
			return "image";
		else
			return "text";
	}
	
	

}
