package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class ReportOptions {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "repOGen")
    @TableGenerator(
        name = "repOGen",
        table = "repOGenseq",
        pkColumnValue = "repO",
        valueColumnName = "nextrepO",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String font;
	private String size;
	private String bodyCss;
	private Boolean coverHeader=false;
	private Boolean coverFooter=false;
	private String headerSize="0mm";
	private String footerSize="0mm";
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFont() {
		return font;
	}
	public void setFont(String font) {
		this.font = font;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getBodyCss() {
		return bodyCss;
	}
	public void setBodyCss(String bodyCss) {
		this.bodyCss = bodyCss;
	}
	public Boolean getCoverHeader() {
		return coverHeader;
	}
	public void setCoverHeader(Boolean coverHeader) {
		this.coverHeader = coverHeader;
	}
	public Boolean getCoverFooter() {
		return coverFooter;
	}
	public void setCoverFooter(Boolean coverFooter) {
		this.coverFooter = coverFooter;
	}
	public String getHeaderSize() {
		return headerSize;
	}
	public void setHeaderSize(String headerSize) {
		this.headerSize = headerSize;
	}
	public String getFooterSize() {
		return footerSize;
	}
	public void setFooterSize(String footerSize) {
		this.footerSize = footerSize;
	}
	
	
	
	
	
}
