package com.fuse.dao;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class Integrations {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "codeGen")
    @TableGenerator(
        name = "codeGen",
        table = "codeGenseq",
        pkColumnValue = "code",
        valueColumnName = "nextCode",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String code;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String>returnValues;
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String>arguments;
	private String name;
	private Boolean enabled;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getArguments() {
		return arguments;
	}
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	public void setReturnValues(List<String> returnValues) {
		this.returnValues = returnValues;
	}
	public List<String> getReturnValues() {
		return returnValues;
	}
	public Boolean isEnabled() {
		if(enabled == null)
			enabled = false;
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	
	

	

}
