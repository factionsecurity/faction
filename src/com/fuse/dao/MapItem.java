package com.fuse.dao;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fuse.dao.ReportMap.DataProperties;

@Entity
public class MapItem {

	
	@Id
	@GeneratedValue
	private Long id;
	private String param;
	private Long prop;
	private boolean base64;
	private boolean recursive;
	public MapItem(){};
	
	public MapItem(String param, DataProperties prop, boolean recurse, boolean base64){
		this.param = param;
		this.base64 = base64;
		this.recursive = recurse;
		this.prop = prop.getValue();
	}
	public Long getId() {
		return id;
	}

	public boolean isBase64() {
		return base64;
	}
	public boolean isRecursive() {
		return recursive;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setBase64(boolean base64) {
		this.base64 = base64;
	}
	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public void setProp(DataProperties prop) {
		this.prop = prop.getValue();
	}
	public DataProperties getProp(){
		return DataProperties.getProp(this.prop);
	}
	
	
	
	
}
