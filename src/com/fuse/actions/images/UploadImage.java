package com.fuse.actions.images;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	
	public void setEncodedImage(String encodedImage) {
		this.encodedImage = encodedImage;
	}
	public void setAssessmentId(Long assessmentId) {
		this.assessmentId = assessmentId;
	}
	
}
