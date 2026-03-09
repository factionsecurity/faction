package com.fuse.actions.assessment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Notification;
import com.fuse.dao.PeerReview;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Verification;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/dashboard/Dashboard.jsp")
public class Dashboard extends FSActionSupport {

	private List<Assessment> current;
	private List<Notification> notifications = new ArrayList();
	private List<PeerReview> pr_complete = new ArrayList<PeerReview>();
	private String action = "";
	private List<RiskLevel> levels = new ArrayList();
	private Long nid;

	@Action(value = "Dashboard", results = { @Result(name = "successJson", location = "/WEB-INF/jsp/successJson.jsp") })
	public String execute() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		User user = this.getSessionUser();
		if (action.equals("")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");

			String query = "{\"assessor\" : " + user.getId() + ", \"completed\" : {$exists: false},"
					+ " \"start\" : { $lte : ISODate(\"" + sdf.format(new Date()) + "\")},"
					+ "\"end\": { $gte : ISODate(\"" + sdf.format(new Date()) + "\")}}";

			current = (List<Assessment>) em.createNativeQuery(query, Assessment.class).getResultList();

			notifications = (List<Notification>) em.createQuery("from Notification where assessorId = :id")
					.setParameter("id", user.getId()).getResultList();
			levels = em.createQuery("from RiskLevel order by riskId").getResultList();

		} else if (action.equals("gotIt")) {
			Notification notification = (Notification) em
					.createQuery("from Notification where assessorId = :id and id = :nid")
					.setParameter("id", user.getId()).setParameter("nid", nid).getResultList().stream().findFirst()
					.orElse(null);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(notification);
			HibHelper.getInstance().commit();
			return SUCCESSJSON;
		}

		return SUCCESS;
	}

	@Action(value = "clearNotifications", results = {
			@Result(name = "successJson", location = "/WEB-INF/jsp/successJson.jsp") })
	public String clearNotifications() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		User user = this.getSessionUser();
		List<Notification> notifications = (List<Notification>) em.createQuery("from Notification where assessorId = :id")
				.setParameter("id", user.getId()).getResultList();
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		for(Notification n : notifications) {
			em.remove(n);
		}
		HibHelper.getInstance().commit();
		return "successJson";
	}

	public String getActiveDB() {
		return "active";
	}

	public List<Assessment> getCurrent() {
		return current;
	}

	public void setCurrent(List<Assessment> current) {
		this.current = current;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setNid(Long nid) {
		this.nid = nid;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}

}
