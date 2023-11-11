package com.fuse.dao;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class ProfileImage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "pimgGen")
    @TableGenerator(
        name = "pimgGen",
        table = "pimgGenseq",
        pkColumnValue = "pimg",
        valueColumnName = "nextpImg",
        initialValue = 1,
        allocationSize = 1
    )
	private long id;
	private String base64Image;
	private String guid;
	private Long userid;
	private String contenType;
	
	public ProfileImage(){
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

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public String getContenType() {
		return contenType;
	}

	public void setContenType(String contenType) {
		this.contenType = contenType;
	}
	
	
	
	

}
