package com.fuse.actions.templates;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.BoilerPlate;
import com.fuse.dao.User;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/templates/Templates.jsp")
public class Templates extends FSActionSupport{
	
	List<BoilerPlate> templates;
	private boolean inActive = false;
	
	
	@Action(value="Templates", results={
		})
	public String execute() {
		User user = this.getSessionUser();
		if (user == null)
			return LOGIN;
		templates = em.createQuery("from BoilerPlate where global=true")
				.getResultList();
		
		return SUCCESS;
		
		
	}

	public String getActiveTemplates() {
		return "active";
	}

	public List<BoilerPlate> getTemplates() {
		return templates;
	}
	
	

}
