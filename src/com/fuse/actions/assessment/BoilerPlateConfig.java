package com.fuse.actions.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.BoilerPlate;
import com.fuse.dao.HibHelper;
import com.fuse.utils.FSUtils;

@Namespace("/portal")
public class BoilerPlateConfig extends FSActionSupport {
	private List<BoilerPlate> boilers;
	private String term;
	private Long tmpId;
	private String summary;
	private Boolean exploit;
	private Boolean global;
	private String type;
	private Boolean active;

	@Action(value = "tempSearch", results = {
			@Result(name = "tempSearchJson", location = "/WEB-INF/jsp/assessment/tempSearchJSON.jsp") })
	public String searchTemplate() {


		List<BoilerPlate> plates = em.createQuery("from BoilerPlate where user = :user and title like :title and type = :type and active = true")
				.setParameter("user", this.getSessionUser())
				.setParameter("title", "%" + this.term.trim() + "%")
				.setParameter("type", this.type.trim())
				.getResultList();
		//List<BoilerPlate> plates = em.createNativeQuery(query, BoilerPlate.class).getResultList();
		boilers = new ArrayList<BoilerPlate>();
		if (exploit != null && exploit) {
			for (BoilerPlate bp : plates) {
				if (bp.getExploit() != null && bp.getExploit())
					boilers.add(bp);
			}
		} else {
			for (BoilerPlate bp : plates) {
				if (bp.getExploit() == null || bp.getExploit() == false)
					boilers.add(bp);
			}
		}

		return "tempSearchJson";
	}

	@Action(value = "tempDelete")
	public String tempDelete() {
		BoilerPlate bp = (BoilerPlate) em.createQuery("from BoilerPlate where id = :id")
				.setParameter("id", this.tmpId).getResultList()
				.stream().findFirst().orElse(null);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.remove(bp);
		HibHelper.getInstance().commit();

		return this.SUCCESSJSON;
	}
	@Action(value = "tempActive")
	public String tempActive() {
		BoilerPlate bp = (BoilerPlate) em.createQuery("from BoilerPlate where id = :id")
				.setParameter("id", this.tmpId).getResultList()
				.stream().findFirst().orElse(null);
		HibHelper.getInstance().preJoin();
		bp.setActive(this.active);
		em.joinTransaction();
		em.persist(bp);
		HibHelper.getInstance().commit();

		return this.SUCCESSJSON;
	}

	@Action(value = "tempSearchDetail", results = {
			@Result(name = "tempSearchDetailJson", location = "/WEB-INF/jsp/assessment/tempSearchDetailJSON.jsp") })
	public String searchTemplateDetail() {
		BoilerPlate bp = em.find(BoilerPlate.class, this.tmpId);
		boilers = new ArrayList();
		boilers.add(bp);

		return "tempSearchDetailJson";
	}
	@Action(value = "globalSave", results = {
			@Result(name = "tempSearchJson", location = "/WEB-INF/jsp/assessment/tempSearchJSON.jsp") })
	public String globalSaveTemplate() {
		BoilerPlate bp = (BoilerPlate) em
				.createQuery(
						"from BoilerPlate where id=:id and global=true")
				.setParameter("id", this.tmpId)
				.getResultList().stream()
				.findFirst().orElse(null);
		if (bp == null) {
			this._message ="Invalid Boilerplate";
			return this.ERRORJSON;
		}
		bp.setText(FSUtils.sanitizeHTML(this.summary));
		bp.setUser(this.getSessionUser());
		bp.setCreated(new Date());
		bp.setActive(this.active);

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(bp);
		HibHelper.getInstance().commit();
		boilers = new ArrayList<BoilerPlate>();
		boilers.add(bp);

		return "tempSearchJson";
	}

	@Action(value = "tempSave", results = {
			@Result(name = "tempSearchJson", location = "/WEB-INF/jsp/assessment/tempSearchJSON.jsp") })
	public String saveTemplate() {
		if(global == null)
			global = false;
		if(active == null)
			active=true;
		BoilerPlate bp = (BoilerPlate) em
				.createQuery(
						"from BoilerPlate where user= :user and title = :title and type = :type and global = :global")
				.setParameter("user", this.getSessionUser()).setParameter("title", this.term.trim())
				.setParameter("type", this.type.trim()).setParameter("global", this.global).getResultList().stream()
				.findFirst().orElse(null);
		if (bp == null)
			bp = new BoilerPlate();
		if (this.term.trim().equals("")) {
			this._message = "Cannot save a template with a blank name.";
			return this.ERRORJSON;
		}
		if (!(this.type.trim().equals("risk") || this.type.trim().equals("summary")
				|| this.type.trim().equals("exploit") || this.type.trim().equals("custom field") )) {
			this._message = "Not a valid type";
			return this.ERRORJSON;
		}
		bp.setTitle(this.term);
		bp.setText(FSUtils.sanitizeHTML(this.summary));
		bp.setUser(this.getSessionUser());
		bp.setType(this.type.trim());
		bp.setGlobal(this.global);
		bp.setCreated(new Date());
		bp.setActive(this.active);

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(bp);
		HibHelper.getInstance().commit();
		boilers = new ArrayList<BoilerPlate>();
		boilers.add(bp);

		return "tempSearchJson";
	}

	public List<BoilerPlate> getBoilers() {
		return boilers;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public void setTmpId(Long tmpId) {
		this.tmpId = tmpId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Boolean isExploit() {
		return exploit;
	}

	public void setExploit(Boolean exploit) {
		this.exploit = exploit;
	}

	public void setGlobal(Boolean global) {
		this.global = global;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}
	public Boolean getActive() {
		return this.active;
	}

}
