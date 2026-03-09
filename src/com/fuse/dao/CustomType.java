package com.fuse.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
	private String defaultValue = "";
	private Integer fieldType = 0;
	private Boolean deleted = false;
	@OneToMany(fetch = FetchType.EAGER)
	private List<AssessmentType> assessmentTypes = new ArrayList<>();
	static public enum FieldType { STRING(0), BOOLEAN(1), LIST(2);
		int value;
		FieldType(int v){
			value = v;
		}
		public  int getValue(){
			return value;
		}
		public static FieldType getProp(int value){

			switch(value){
				case 0: return STRING;
				case 1: return BOOLEAN;
				case 2: return LIST;
				default : return STRING;
			}
		}
	};
	
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
	public int getFieldType() {
		return fieldType == null? 0 : fieldType;
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
	public void setFieldType(Integer fieldType) {
		this.fieldType = fieldType;
	}
	public Boolean getReadonly() {
		return readonly;
	}
	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public void setAssessmentTypes(List<AssessmentType>types) {
		this.assessmentTypes = types;
	}
	public List<AssessmentType> getAssessmentTypes() {
		return this.assessmentTypes;
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
	@Transient
	public String getFieldTypeStr(){
		if(this.fieldType == null) {
			return "String";
		}else {
			switch(this.fieldType){
				case 0: return "String";
				case 1: return "Boolean";
				case 2: return "List";
				default: return "String";
			}
		}
	}
	
	
	
	
	

}
