package com.fuse.actions.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Namespace;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.Image;
import com.fuse.dao.PeerReview;
import com.fuse.dao.ReportTemplates;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.utils.ImageBorderUtil;

import java.util.Base64;

import javax.imageio.ImageIO;

@Namespace("/portal")
public class GetImage extends FSActionSupport {
	private String id;
	private InputStream stream;
	private String file_dataContentType;

	@Action(value = "getImage", results = {
			@Result(name = "success", type = "stream", params = { "contentType", "${file_dataContentType}", "inputName",
					"stream", "bufferSize", "1024", "contentDisposition", "attachment;filename=\"image\"" }) })
	public String getImage() throws IOException {
		String assessmentId = id.split(":")[0];
		String imageId = id.split(":")[1];
		Assessment assessment = AssessmentQueries.getAssessment(em, getSessionUser(), Long.parseLong(assessmentId));
		if(assessment==null) {
			return ERROR;
		}
		try {
			//Image image = assessment.getImages().stream().filter(i -> i.getGuid().equals(imageId)).findAny().orElse(null);
			Image image = (Image) em.createQuery("from Image where guid = :id")
					.setParameter("id", imageId).getResultList().stream().findFirst().orElse(null);

			String[] parts = image.getBase64Image().split(",");
			file_dataContentType = parts[0].split(";")[0].replace("data:", "");

			byte[] imageData = Base64.getDecoder().decode(parts[1]);
			imageData = ImageBorderUtil.addBorder(imageData, 1, Color.GRAY);

			stream = new ByteArrayInputStream(imageData);
			return SUCCESS;
		}catch(Exception ex) {
			ex.printStackTrace();
			return ERROR;
		}
	}

	public void setId(String id) {
		this.id = id;
	}

	public InputStream getStream() {
		return stream;
	}

	public String getFile_dataContentType() {
		return this.file_dataContentType;
	}

}
