package com.fuse.actions;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/upgrade/Upgrade.jsp")
public class Upgrade extends FSActionSupport {

	@Action(value = "Upgrade")
	public String execute() {
		return SUCCESS;
	}
}
