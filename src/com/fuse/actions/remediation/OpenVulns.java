package com.fuse.actions.remediation;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.VulnerabilityQueries;
import com.fuse.utils.FSUtils;
import com.mongodb.BasicDBObject;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/remediation/OpenVulns.jsp")
public class OpenVulns extends FSActionSupport {

	private String appname = "";
	private String appId = "";
	private List<Combo> combos;
	private String crit = "";
	private String high = "";
	private String med = "";
	private String low = "";
	private String info = "";
	private String rec = "";
	private String action = "";
	private String open = "";
	private String closed = "";
	private String tracking = "";
	private List<String> risk = new ArrayList();
	private Integer start = 0;
	private Integer length = 10;
	private Long count;

	@Action(value = "OpenVulns", results = {
			@Result(name = "vulnsJson", location = "/WEB-INF/jsp/remediation/vulnsJson.jsp") })
	public String execute() {

		if (!(this.isAcassessor() || this.isAcmanager() || this.isAcremediation())) {
			return LOGIN;
		}
		User user = this.getSessionUser();

		// EntityManager em = HibHelper.getEM();
		if (action.equals("get")) {

			this.appname = FSUtils.sanitizeMongo(this.appname);
			this.appId = FSUtils.sanitizeMongo(this.appId);
			this.appname = FSUtils.sanitizeMongo(this.appname);
			this.tracking = FSUtils.sanitizeMongo(this.tracking);
			if (this.tracking != null && !this.tracking.equals("")) {

				List<Vulnerability> vulns = VulnerabilityQueries.matchTracking(em, this.tracking);

				combos = new ArrayList<Combo>();
				List<Verification> vers = em
						.createQuery("from Verification v where v.workflowStatus = :wf1 or v.workflowStatus = :wf2")
						.setParameter("wf1", Verification.InAssessorQueue)
						.setParameter("wf2", Verification.AssessorCompleted).getResultList();
				for (Vulnerability vuln : vulns) {
					vuln.updateRiskLevels(em);
					Combo c = new Combo();
					c.vuln = vuln;
					c.assessment = em.find(Assessment.class, vuln.getAssessmentId());

					for (Verification vr : vers) {
						if (vr.getVerificationItems().get(0).getVulnerability().equals(vuln)) {
							c.isVer = true;
							break;
						}
					}
					combos.add(c);

				}
				this.count = Integer.toUnsignedLong(combos.size());
				return "vulnsJson";

			} else {

				String newMongo = "db.Assessment.aggregate(" + "[ "
						+ "{ '$lookup': { 'from':'Vulnerability', 'localField': '_id', 'foreignField':'assessmentId', 'as': 'vuln' }},"
						+ "{'$unwind':'$vuln' }, " + "{'$match': { " + "  'completed' : {'$exists':true},";
				if (!this.appname.equals("")) {
					newMongo += " 'name' : {'$regex' : '.*" + this.appname + ".*', '$options': 'i' },";
				}
				if (!this.appId.equals("")) {
					newMongo += " 'appId' : {'$regex' : '.*" + this.appId + ".*', '$options': 'i' },";
				}

				if (this.closed.equals("") && !this.open.equals("")) {
					newMongo += " 'vuln.closed' : {'$exists':false}, ";
				} else if (!this.closed.equals("") && this.open.equals("")) { // show only closed items
					newMongo += " 'vuln.closed' : {'$exists':true}, ";
				}

				if (!this.tracking.equals("")) {
					newMongo += " 'vuln.tracking' : '" + this.tracking + "', ";
				}

				newMongo += createQuery();
				newMongo += "}}";

				String CountQuery = newMongo + ", { '$group': { '_id': '', 'count': { '$sum': 1 } }} ])";
				newMongo += "," + "{'$limit' : NumberLong('" + (this.start + this.length) + "')},"
						+ "{'$skip' : NumberLong('" + this.start + "')}" + "])";

				List query = em.createNativeQuery(CountQuery).getResultList();
				if (query.size() == 0) {
					this.count = 0l;
				} else {
					Object[] data = (Object[]) query.get(0);

					this.count = ((Integer) data[1]).longValue();
				}

				List<Assessment> asmts = em.createNativeQuery(newMongo, Assessment.class).getResultList();
				List<Object[]> raw = em.createNativeQuery(newMongo).getResultList();
				combos = new ArrayList();
				for (int i = 0; i < asmts.size(); i++) {

					Assessment asmt = asmts.get(i);
					if (asmt.getVulns() == null || asmt.getVulns().size() == 0)
						continue;

					Object[] objs = raw.get(i);

					Combo combo = new Combo();
					combo.assessment = asmt;
					Long vid = getVid(objs);
					for (Vulnerability v : asmt.getVulns()) {
						if (v.getId() == vid) {
							v.updateRiskLevels(em);
							combo.vuln = v;
							// combos.add(combo);
							break;
						}
					}

					combos.add(combo);

				}

				List<Verification> ver = em
						.createQuery("from Verification v where v.workflowStatus = :wf1 or v.workflowStatus = :wf2")
						.setParameter("wf1", Verification.InAssessorQueue)
						.setParameter("wf2", Verification.AssessorCompleted).getResultList();

				for (Verification v : ver) {
					if (v.getVerificationItems() == null || v.getVerificationItems().size() == 0) {
						System.out.println("Ummm... why does vid: " + v.getId() + " not any verification items.");
					}
					if (v.getVerificationItems().get(0).getVulnerability() == null) {
						System.out.println("Ummm... why does vid: " + v.getId() + " not any Vulnerability associated.");

					}
					for (Combo c : combos) {
						if (c.vuln == null) {
							System.out.println("Ummm... why does Combo not any Vulnerability associated.");
							continue;
						}
						if (c.vuln.equals(v.getVerificationItems().get(0).getVulnerability())) {
							c.isVer = true;
							break;
						}
					}
				}
				return "vulnsJson";
			}
		}

		// em.close();

		return SUCCESS;
	}

	private Long getVid(Object[] objs) {
		for (Object obj : objs) {
			if (obj.getClass().getName().contains("BasicDBObject")) {
				BasicDBObject bdo = (BasicDBObject) obj;
				return bdo.getLong("_id");
			}
		}
		return null;
	}

	private String createQuery() {

		List<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < risk.size(); i++) {
			if (risk.get(i) != null && risk.get(i).equals("true"))
				values.add(i);
		}

		String out = " 'vuln.overall' : { '$in' : [";
		boolean first = true;
		for (int value : values) {
			if (first) {
				first = false;
				out += "NumberLong('" + value + "')";
			} else {
				out += ", NumberLong('" + value + "')";
			}
		}
		out += "]} ";
		return out;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public List<Combo> getCombos() {
		return combos;
	}

	public class Combo {
		public Assessment assessment;
		public Vulnerability vuln;
		public boolean isVer = false;

	}

	public String getCrit() {
		return crit == null ? "" : crit;
	}

	public void setCrit(String crit) {
		this.crit = crit;
	}

	public String getHigh() {
		return high == null ? "" : high;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public String getMed() {
		return med == null ? "" : med;
	}

	public void setMed(String med) {
		this.med = med;
	}

	public String getLow() {
		return low == null ? "" : low;
	}

	public void setLow(String low) {
		this.low = low;
	}

	public String getInfo() {
		return info == null ? "" : info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getRec() {
		return rec == null ? "" : rec;
	}

	public void setRec(String rec) {
		this.rec = rec;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public String getClosed() {
		return closed;
	}

	public void setClosed(String closed) {
		this.closed = closed;
	}

	public void setTracking(String tracking) {
		this.tracking = tracking;
	}

	public List<String> getRisk() {
		return risk;
	}

	public Integer getStart() {
		return start;
	}

	public Integer getLength() {
		return length;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
