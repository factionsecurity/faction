package com.fuse.actions.images;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;

@Namespace("/portal")
public class UploadImage extends FSActionSupport {
	private Long assessmentId;
	private String encodedImage;
	
	@Action(value = "UploadImage")
	public String uploadVulnImage() throws IOException {
		Image image = new Image();
		image.setBase64Image(encodedImage);
		Assessment assessment = AssessmentQueries.getAssessment(em, getSessionUser(), assessmentId);
		if(assessment != null) {
			//removeImages(assessment);
			assessment.getImages().add(image);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(assessment);
			HibHelper.getInstance().commit();
			this._message=""+assessment.getId() +":" +image.getGuid();
			this._result="success";
		}else {
			
			this._message="Not Authorized";
			this._result="error";
		}
		return this.MESSAGEJSON;
	}
	
	private void removeImages(Assessment assessment) {
		for(Image image : assessment.getImages()) {
			boolean found = false;
			String guid = image.getGuid();
			if(assessment.getSummary() != null && assessment.getSummary().contains(guid)) {
				found = true;
			}else if(assessment.getRiskAnalysis() != null && assessment.getRiskAnalysis().contains(guid)) {
				found = true;
			}else {
				for(Vulnerability vuln: assessment.getVulns()) {
					if(vuln.getDescription() != null && vuln.getDescription().contains(guid)) {
						found = true;
					}else if(vuln.getRecommendation() != null && vuln.getRecommendation().contains(guid)) {
						found = true;
					}else if(vuln.getDetails() != null && vuln.getDetails().contains(guid)) {
						found = true;
					}
				}
			}
			if(!found) {
				assessment.getImages().remove(image);
			}
		}
		
	}
	
	public void setEncodedImage(String encodedImage) {
		this.encodedImage = encodedImage;
	}
	public void setAssessmentId(Long assessmentId) {
		this.assessmentId = assessmentId;
	}
	
}
