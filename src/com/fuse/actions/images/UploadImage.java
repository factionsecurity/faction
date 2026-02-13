package com.fuse.actions.images;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.utils.ImageBorderUtil;

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
	
    private String outlineImages(String encodedImage) {
    	if(encodedImage == null || encodedImage.isEmpty())
    		return encodedImage;
    	
		try {
			String[] parts = encodedImage.split(",");
			String file_dataContentType = parts[0].split(";")[0].replace("data:", "");
			byte[] imageData = Base64.getDecoder().decode(parts[1]);
			imageData = ImageBorderUtil.addBorder(imageData, 1, Color.GRAY);
			String borderedImage = Base64.getEncoder().encodeToString(imageData);
			borderedImage = "data:" + file_dataContentType + ";base64," + borderedImage;
			return borderedImage;

		} catch (IOException e) {
			e.printStackTrace();
			return encodedImage;
		}
    }
	
	
	public void setEncodedImage(String encodedImage) {
		this.encodedImage = outlineImages(encodedImage);
	}
	public void setAssessmentId(Long assessmentId) {
		this.assessmentId = assessmentId;
	}
	
}
