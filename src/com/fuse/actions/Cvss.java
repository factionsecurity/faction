package com.fuse.actions;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/cvss.jsp")
public class Cvss extends FSActionSupport{ 

	@Action(value="CVSS")
	public String execute(){
		return SUCCESS;
		
	}
	
	@Action(value="CVSS40", results={
			@Result(name="success",location="/WEB-INF/jsp/cvss40.jsp")
	})
	public String cvss40(){
		return SUCCESS;
		
	}
}
