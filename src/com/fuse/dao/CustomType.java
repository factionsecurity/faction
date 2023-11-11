package com.fuse.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import com.fuse.dao.ReportMap.DataProperties;

@Entity
public class CustomType {
	
	@Id
	@GeneratedValue
	private Long id;
	private String key;
	private String variable;
	private int type;
	private Boolean readonly =  false;
	
	static public enum ObjType { ASMT(0), VULN(1), USER(2), VER(3);
		int value;
		ObjType(int v){
			value = v;
		}
		public  int getValue(){
			return value;
		}
		public static ObjType getProp(int value){

			switch(value){
				case 0: return ASMT;
				case 1: return VULN;
				case 2: return USER;
				case 3: return VER;
				default : return null;
			}
		}
		
	};
	public CustomType(){};
	
	public CustomType(String key, String variable, int type){
		this.key = key;
		this.variable = variable;
		this.type = type;
	}
	public Long getId() {
		return id;
	}
	public String getKey() {
		return key;
	}
	public String getVariable() {
		return variable;
	}
	public int getType() {
		return type;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Boolean getReadonly() {
		return readonly;
	}
	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}

	@Transient
	public String getTypeStr(){
		switch(this.type){
			case 0: return "Assessment";
			case 1: return "Vulnerability";
			case 2: return "User";
			case 3: return "Retest";
			default: return "Undefined";
		}
	}
	
	
	
	
	

}
