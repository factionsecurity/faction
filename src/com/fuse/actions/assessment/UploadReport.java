package com.fuse.actions.assessment;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.FinalReport;
import com.fuse.dao.FinalReportVariant;
import com.fuse.dao.HibHelper;
import com.fuse.dao.query.AssessmentQueries;

@Namespace("/portal")
public class UploadReport extends FSActionSupport {

	private File uploadReport;
	private String uploadReportContentType;
	private String uploadReportFilename;

	@Action(value = "UploadFinalReport")
	public String uploadReport() throws Exception {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		com.fuse.dao.User user = this.getSessionUser();
		Long asmtId = (Long) this.getSession("asmtid");
		if (asmtId == null) {
			this._message = "No assessment selected.";
			return this.ERRORJSON;
		}

		Assessment asmt;
		if (this.isAcmanager()) {
			asmt = AssessmentQueries.getAssessmentById(em, asmtId);
		} else {
			asmt = AssessmentQueries.getAssessmentByUserId(em, user.getId(), asmtId, AssessmentQueries.All);
		}

		if (!this.testToken(false))
			return this.ERRORJSON;

		if (asmt == null || asmt.getCompleted() != null) {
			this._message = "Assessment not found or already finalized.";
			return this.ERRORJSON;
		}

		if (!this.isAcmanager() && asmt.getAssessor().stream().noneMatch(u -> u.getId() == user.getId())) {
			this._message = "You are not an assessor on this assessment.";
			return this.ERRORJSON;
		}

		if (uploadReport == null) {
			this._message = "No file uploaded.";
			return this.ERRORJSON;
		}

		String ct = uploadReportContentType == null ? "" : uploadReportContentType.toLowerCase();
		String fn = uploadReportFilename == null ? "" : uploadReportFilename.toLowerCase();
		boolean isPdf = ct.contains("pdf") || fn.endsWith(".pdf");
		boolean isDocx = ct.contains("wordprocessingml") || fn.endsWith(".docx");

		if (!isPdf && !isDocx) {
			this._message = "Only .docx and .pdf files are allowed.";
			return this.ERRORJSON;
		}

		byte[] fileBytes = Files.readAllBytes(uploadReport.toPath());
		String b64 = Base64.encodeBase64String(fileBytes);
		String fileType = isPdf ? "pdf" : "docx";

		HibHelper.getInstance().preJoin();
		em.joinTransaction();

		FinalReport fr;
		if (asmt.getFinalReport() == null) {
			fr = new FinalReport();
			fr.setRetest(false);
			fr.setFilename(UUID.randomUUID().toString());
			fr.setGentime(new Date());
			em.persist(fr);
			asmt.setFinalReport(fr);
		} else {
			fr = asmt.getFinalReport();
			fr.getVariants().clear();
			fr.setGentime(new Date());
			if (!isPdf) {
				fr.setEncryptedReportPassword(null);
			}
		}

		FinalReportVariant variant = new FinalReportVariant();
		variant.setFileType(fileType);
		variant.setBase64Content(b64);
		fr.getVariants().add(variant);
		fr.setVariantCount(1);

		em.persist(asmt);
		HibHelper.getInstance().commit();

		AuditLog.audit(this, "Report uploaded for assessment " + asmt.getName(), AuditLog.CompAssessment, false);
		return this.SUCCESSJSON;
	}

	public void setUploadReport(File uploadReport) {
		this.uploadReport = uploadReport;
	}

	public void setUploadReportContentType(String uploadReportContentType) {
		this.uploadReportContentType = uploadReportContentType;
	}

	public void setUploadReportFilename(String uploadReportFilename) {
		this.uploadReportFilename = uploadReportFilename;
	}
}
