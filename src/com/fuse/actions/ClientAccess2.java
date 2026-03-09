package com.fuse.actions;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/metrics/Metrics.jsp")
public class ClientAccess2 extends FSActionSupport{
	
}
