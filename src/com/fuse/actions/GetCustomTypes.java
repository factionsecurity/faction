package com.fuse.actions;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.AuditLog;
import com.fuse.dao.CustomType;

import org.apache.struts2.convention.annotation.Namespace;

@Namespace("/portal")
public class GetCustomTypes extends FSActionSupport{
	
	private Integer variableType;
	private List<CustomType> types;
	private Long assessmentType;

	@Action(value = "getCustomTypes", results={
			@Result(name="typeJson",location="/WEB-INF/jsp/engagement/customTypesJSON.jsp")
		})
	public String execute() {
		if(!(this.isAcengagement() || this.isAcmanager())){
			return AuditLog.notAuthorized(this, "User is not Engagment or Manager", true);
		}
		types = em
				.createQuery("from CustomType where type = :variableType and (deleted IS NULL or deleted = false)")
				.setParameter("variableType", variableType)
				.getResultList();
		
		if(CustomType.FieldType.values().length > 3) {
			types = types
				.stream()
				.filter( vType -> 
					vType.getAssessmentTypes()
						.stream()
						.anyMatch( aType -> 
							aType.getId().equals(this.assessmentType)
						))
				.collect(Collectors.toList());
		}
		
		return "typeJson";
		
	}
	
	public List<CustomType> getTypes(){
		return this.types;
	}
	public void setAssessmentType(Long assessmentType) {
		this.assessmentType = assessmentType;
	}
	public void setVariableType(Integer variableType) {
		this.variableType = variableType;
	}

}
