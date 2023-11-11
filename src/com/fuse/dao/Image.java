package com.fuse.dao;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class Image {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "iimgGen")
    @TableGenerator(
        name = "iimgGen",
        table = "iimgGenseq",
        pkColumnValue = "iimg",
        valueColumnName = "nextIimg",
        initialValue = 1,
        allocationSize = 1
    )
	private long id;
	private String base64Image;
	private String name;
	private String contentType;
	private String guid;
	
	public Image(){
		UUID uuid = UUID.randomUUID();
		this.guid = uuid.toString();
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getBase64Image() {
		return base64Image;
	}
	public void setBase64Image(String base64Image) {
		this.base64Image = base64Image;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		UUID uuid = UUID.randomUUID();
		this.guid = uuid.toString();
	}
	
	
	
	
	
	

}