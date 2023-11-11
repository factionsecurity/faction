package com.fuse.actions;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/DownloadError.jsp")
public class DownloadError extends FSActionSupport{
	
	@Action(value="DownloadError")
	public String execute() throws IOException{
		return SUCCESS;
	}

}
