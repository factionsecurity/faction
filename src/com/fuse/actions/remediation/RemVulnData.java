package com.fuse.actions.remediation;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.Assessment;
import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Files;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Verification;
import com.fuse.dao.VulnNotes;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.VulnerabilityQueries;
import com.fuse.utils.FSUtils;

@Namespace("/portal")
public class RemVulnData extends FSActionSupport {

	private String action = "";
	private String note = "";
	private Long vulnId = -1l;
	private List<VulnNotes> notes;
	private Long impact;
	private Long likelyhood;
	private Long severity;
	private String gid;
	private Long verId = -1l;
	private List<Files> files;
	private String fileJSON;
	private String scope="";

	@Action(value = "RemVulnData", results = {
			@Result(name = "notesJson", location = "/WEB-INF/jsp/remediation/notesJson.jsp"),
			@Result(name = "fileInfoJson", location = "/WEB-INF/jsp/remediation/fileInfoJson.jsp") })
	public String execute() {
		if (!this.isAcremediation()) {
			return LOGIN;
		}
		if (action.equals("insertNote") && vulnId != -1l) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			VulnNotes vn = new VulnNotes();
			vn.setUuid(UUID.randomUUID().toString());
			vn.setNote(note);
			vn.setVulnId(vulnId);
			vn.setCreated(new Date());
			vn.setCreator(this.getSessionUser().getId());
			vn.setCreatorObj(this.getSessionUser());
			em.persist(vn);
			HibHelper.getInstance().commit();

			return "successJson";

		} else if (action.equals("getNotes") && vulnId != -1l) {
			notes = (List<VulnNotes>) em.createQuery("from VulnNotes where vulnId = :vid order by created desc")
					.setParameter("vid", vulnId).getResultList();
			scope="";
			if(verId != -1l) {
				Verification verification = em.find(Verification.class, verId);
				scope = verification.getNotes();
			}else {
				Vulnerability vuln = em.find(Vulnerability.class, vulnId);
				Assessment assessment = em.find(Assessment.class, vuln.getAssessmentId());
				scope = assessment.getAccessNotes();
			}
			// session.close();
			return "notesJson";
		} else if (action.equals("changeSev") && vulnId != -1l) {

			HibHelper.getInstance().preJoin();
			em.joinTransaction();

			// Vulnerability v = (Vulnerability)session.createQuery("from Vulnerability
			// where id = :id" ).setLong("id", vulnId).uniqueResult();
			Vulnerability v = em.find(Vulnerability.class, vulnId);
			v.updateRiskLevels(em);
			String PrependNote = "<small class=\"label pull-left bg-blue\">Vulnerability Severity Changed</small><br><br>";

			PrependNote += "<table class=chSevTable ><tr><td></td><td><b>Previous</b></td><td><b>New</b></td></tr>";
			PrependNote += "<tr><td><b>Severity:</b></td><td>" + v.getOverallStr() + "</td><td>";
			v.setOverall(severity);
			PrependNote += v.getOverallStr() + "</td></tr>";
			PrependNote += "<tr><td><b>Impact:</b></td><td>" + v.getImpactStr() + "</td><td>";
			v.setImpact(impact);
			PrependNote += v.getImpactStr() + "</td></tr>";
			PrependNote += "<tr><td><b>Likelyhood:</b></td><td>" + v.getLikelyhoodStr() + "</td><td>";
			v.setLikelyhood(likelyhood);
			PrependNote += v.getLikelyhoodStr() + "</td></tr></table>";

			v.setImpact(impact);
			v.setLikelyhood(likelyhood);
			v.setOverall(severity);
			em.persist(v);
			// session.getTransaction().begin();
			// session.update(v);
			// PrependNote = StringEscapeUtils.escapeHtml3(PrependNote);

			VulnNotes vn = new VulnNotes();
			// vn.setUuid(UUID.randomUUID().toString());
			vn.setUuid("nodelete");
			vn.setNote(PrependNote + note);
			vn.setVulnId(vulnId);
			vn.setCreated(new Date());
			vn.setCreator(this.getSessionUser().getId());
			vn.setCreatorObj(this.getSessionUser());
			em.persist(vn);

			HibHelper.getInstance().commit();

			return "successJson";

		} else if ((action.equals("closeInProd") || action.equals("closeInDev")) && vulnId != -1l) {

			HibHelper.getInstance().preJoin();
			em.joinTransaction();

			Vulnerability v = em.find(Vulnerability.class, vulnId);
			Verification ver = null;
			if (verId != null && verId != -1l)
				ver = em.find(Verification.class, verId);
			else {
				ver = VulnerabilityQueries.getVerificationFromVuln(em, vulnId);
			}
			// ver.setCompleted(new Date());
			String env = "Production";
			if (action.equals("closeInProd")) {
				v.setClosed(new Date());
			} else {
				v.setDevClosed(new Date());
				env = "Development";
			}
			String PrependNote = "<small class=\"label pull-left bg-blue\">Closed in " + env + " by "
					+ this.getSessionUser().getFname() + " " + this.getSessionUser().getLname() + "</small><br><br>";

			VulnNotes vn = new VulnNotes();
			vn.setUuid("nodelete");
			vn.setNote(PrependNote + note);
			vn.setVulnId(vulnId);
			vn.setCreated(new Date());
			vn.setCreator(this.getSessionUser().getId());
			vn.setCreatorObj(this.getSessionUser());
			em.persist(v);
			em.persist(vn);
			if (ver != null) {
				ver.setRemediationCompleted(new Date());
				ver.setWorkflowStatus(Verification.RemediationCompleted);
				em.persist(ver);
			}

			HibHelper.getInstance().commit();

			return "successJson";
		} else if (action.equals("delete") && !gid.equals("")) {
			if (gid.equals("nodelete"))
				return "notAuthorizedJson";
			VulnNotes vn = (VulnNotes) em.createQuery("from VulnNotes where uuid = :uuid").setParameter("uuid", gid)
					.getResultList().stream().findFirst().orElse(null);
			if (vn.getCreator() == this.getSessionUser().getId()) {
				HibHelper.getInstance().preJoin();
				em.joinTransaction();
				em.remove(vn);
				HibHelper.getInstance().commit();

				return "successJson";
			} else {
				return "notAuthorizedJson";
			}

		} else if (action.equals("getFiles") && vulnId != -1l) {
			List<Files> files = (List<Files>) em.createQuery("from Files where entityId = :eid and type = :type")
					.setParameter("eid", this.vulnId).setParameter("type", Files.VERIFICATION).getResultList();
			String json = "{";
			if (files.size() > 0) {
				json += "\"initialPreview\" : [";
				for (Files f : files) {
					if (f.getContentType().contains("image")) {
						json += " \"<img src='../service/fileUpload?id=" + f.getUuid()
								+ "' class='file-preview-image' style='width:auto;height:auto;max-width:100%;max-height:100%;'/>\",";
					} else if (f.getContentType().contains("text")) {
						// try {
						json += " \"<pre class='file-preview-text' title='" + f.getName()
								+ "' style='width:auto;height:auto;max-width:100%;max-height:100%;' >"
								+ StringEscapeUtils.escapeJson(new String(f.getRealFile())) + "</pre>";
						json += "\",";

					} else {
						json += " \"<embed src='../service/fileUpload?id=" + f.getUuid() + "' type='"
								+ f.getContentType()
								+ "' class='file-preview-data' style='width:auto;height:auto;max-width:100%;max-height:100%;'></embed>\",";
					}

				}
				// remove the extra comma
				json = json.substring(0, json.length() - 1);
				json += "],";
				json += "\"initialPreviewConfig\" : [\n";
				for (Files f : files) {
					json += "{ \"caption\": \"" + f.getName() + "\",\"width\" : \"100%\", "
							+ "\"url\" : \"../service/fileUpload?delId=" + f.getUuid() + "\","
							+ "\"downloadUrl\": \"../service/fileUpload?id=" + f.getUuid() + "\"," + "\"key\" : 1},";
				}
				json = json.substring(0, json.length() - 1);
				json += "],";

			}
			json += "\"overwriteInitial\": false, \"uploadUrl\": \"../service/fileUpload?vid=" + vulnId + "\" ,"
					+ "\"uploadAsync\": true," + "\"minFileCount\": 0," + "\"maxFileCount\": 5,"
					+ "\"allowedFileExtensions\" : [\"jpg\",\"gif\",\"png\", \"pdf\", \"doc\",\"xls\", \"xlsx\", \"docx\", \"txt\", \"csv\", \"bmp\", \"jpeg\", \"xml\",\"zip\",\"rar\",\"tar\",\"gzip\",\"tar.gz\"]}";
			fileJSON = json;
			// session.close();
			return "fileInfoJson";

		} else if (action.equals("closeVerification") && vulnId != -1l && verId != -1l) {
			// A canceled verification will delete it and the verifciation items form the
			// queue

			HibHelper.getInstance().preJoin();
			em.joinTransaction();

			Verification ver = em.find(Verification.class, verId);
			Vulnerability v = ver.getVerificationItems().get(0).getVulnerability();
			VulnNotes vn = new VulnNotes();

			note = "<small class=\"label pull-left bg-blue\">" + this.getSessionUser().getFname() + " "
					+ this.getSessionUser().getLname() + " has cancelled the verification for this item. </small>"
					+ "<br><br>" + FSUtils.sanitizeHTML(this.note);

			vn.setNote(note);
			vn.setVulnId(v.getId());
			vn.setUuid("nodelete");
			vn.setCreated(new Date());
			vn.setCreator(this.getSessionUser().getId());
			vn.setCreatorObj(this.getSessionUser());
			if (ver.getWorkflowStatus().equals(Verification.AssessorCancelled))
				ver.setWorkflowStatus(Verification.RemdiationCancelled);
			else if (ver.getWorkflowStatus().equals(Verification.RemdiationCancelled))
				ver.setWorkflowStatus(Verification.RemdiationCancelled);
			else
				ver.setWorkflowStatus(Verification.RemediationCompleted);

			em.persist(vn);
			em.persist(ver);
			HibHelper.getInstance().commit();

			return "successJson";

		}
		return SUCCESS;

	}
	
	@Action( value="Reopen")
	public String reopen() {
		if (!this.isAcremediation()) {
			return LOGIN;
		}
		Vulnerability vuln = em.find(Vulnerability.class, vulnId);
		if(vuln!=null) {
			vuln.setClosed(null);
			vuln.setDevClosed(null);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(vuln);
			HibHelper.getInstance().commit();
		}
		return this.SUCCESSJSON;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getVulnId() {
		return vulnId;
	}

	public void setVulnId(Long vulnId) {
		this.vulnId = vulnId;
	}

	public List<VulnNotes> getNotes() {
		return notes;
	}

	public Long getImpact() {
		return impact;
	}

	public void setImpact(Long impact) {
		this.impact = impact;
	}

	public Long getLikelyhood() {
		return likelyhood;
	}

	public void setLikelyhood(Long likelyhood) {
		this.likelyhood = likelyhood;
	}

	public Long getSeverity() {
		return severity;
	}

	public void setSeverity(Long severity) {
		this.severity = severity;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public Long getVerId() {
		return verId;
	}

	public void setVerId(Long verId) {
		this.verId = verId;
	}

	public List<Files> getFiles() {
		return files;
	}

	public void setFiles(List<Files> files) {
		this.files = files;
	}

	public String getFileJSON() {
		return fileJSON;
	}
	public String getScope() {
		return scope;
	}

}
