package com.fuse.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class FinalReport{
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "frepGen")
    @TableGenerator(
        name = "frepGen",
        table = "frepGenseq",
        pkColumnValue = "frep",
        valueColumnName = "nextfrep",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String filename;
	private String base64EncodedPdf;
	private Date gentime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getBase64EncodedPdf() {
		return base64EncodedPdf;
	}
	public void setBase64EncodedPdf(String base64EncodedPdf) {
		this.base64EncodedPdf = base64EncodedPdf;
	}
	public Date getGentime() {
		return gentime;
	}
	public void setGentime(Date gentime) {
		this.gentime = gentime;
	}
	
	
	

}
