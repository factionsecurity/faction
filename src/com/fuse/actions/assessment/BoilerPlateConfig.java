package com.fuse.actions.assessment;

import java.util.ArrayList;
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
public class BoilerPlateConfig extends FSActionSupport{
	private List<BoilerPlate> boilers;
	private String term;
	private Long tmpId;
	private String summary;
	private Boolean exploit;

	

	@Action(value="tempSearch", results={
			@Result(name="tempSearchJson",location="/WEB-INF/jsp/assessment/tempSearchJSON.jsp")
		})
	public String searchTemplate(){
		
		
		
		String query = "{ 'userid' : " + this.getSessionUser().getId() + " , "
				+ " 'title' : { $regex : '.*" + this.term + ".*', $options: 'ix' }}";
		
		
	
		
		List<BoilerPlate> plates  = em.createNativeQuery(query, BoilerPlate.class).getResultList();
		boilers = new ArrayList<BoilerPlate>();
		if(exploit != null && exploit){
			for(BoilerPlate bp : plates){
				if(bp.getExploit() != null && bp.getExploit())
					boilers.add(bp);
			}
		}else{
			for(BoilerPlate bp : plates){
				if(bp.getExploit() == null || bp.getExploit() == false)
					boilers.add(bp);
			}
		}
		
		return "tempSearchJson";
	}
	@Action(value="tempDelete")
	public String tempDelete(){
		BoilerPlate bp = (BoilerPlate) em.createQuery("from BoilerPlate where userid = :uid and id = :id")
				.setParameter("uid", this.getSessionUser().getId())
				.setParameter("id", this.tmpId)
				.getResultList().stream().findFirst().orElse(null);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.remove(bp);
		HibHelper.getInstance().commit();
		
		
		return this.SUCCESSJSON;
	}
	
	@Action(value="tempSearchDetail", results={
			@Result(name="tempSearchDetailJson",location="/WEB-INF/jsp/assessment/tempSearchDetailJSON.jsp")
		})
	public String searchTemplateDetail(){
		BoilerPlate bp = em.find(BoilerPlate.class, this.tmpId);
		boilers = new ArrayList();
		boilers.add(bp);
		
		return "tempSearchDetailJson";
	}
	
	@Action(value="tempSave")
	public String saveTemplate(){
		BoilerPlate bp = (BoilerPlate) em.createQuery("from BoilerPlate where userid= :uid and title = :title")
				.setParameter("uid", this.getSessionUser().getId())
				.setParameter("title", this.term.trim() )
				.getResultList().stream().findFirst().orElse(null);
		if(bp == null)
			bp = new BoilerPlate();
		if(this.term.trim().equals(""))
		{
			this._message = "Cannot save a template with a blank name.";
			return this.ERRORJSON;
		}
		bp.setTitle(this.term);
		bp.setText(FSUtils.sanitizeHTML(this.summary));
		bp.setUserid(this.getSessionUser().getId());
		if(exploit != null && exploit)
			bp.setExploit(true);
		else
			bp.setExploit(false);
		
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(bp);
		HibHelper.getInstance().commit();
		
		return this.SUCCESSJSON;
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
	
	
	

}
