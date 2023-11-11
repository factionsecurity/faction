package com.fuse.actions;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.Vulnerability;


@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/client/Client.jsp")
public class ClientPortal extends FSActionSupport{
	
	private String accessKey="";
	private List<Vulnerability>vulns = new ArrayList();
	private Long vid;
	private String notes;
	private Date start;
	private Date end;
	private List<vItem> vitems = new ArrayList();
	private String modal;
	
	
	
	@Action(value="ClientPortal", results={
			@Result(name="clientLogin",location="/WEB-INF/jsp/client/Access.jsp"),
			@Result(name="modal",location="/WEB-INF/jsp/client/modal.jsp")
		})
	public String execute(){
		
		if(accessKey.equals("")){
			return "clientLogin";
		}else if(modal != null){
			return "modal";
		}
		else{
			Assessment a = (Assessment)em.createQuery("from Assessment where guid = :id")
					.setParameter("id", accessKey)
					.getResultList()
					.stream()
					.findFirst()
					.orElse(null);
			if(a == null ){
				return "clientLogin";
			}
			if(a.getCompleted() == null || a.getCompleted().getTime() == 0l){
				return SUCCESS;
			}
			String mongo = "{ 'assessment_id' : " + a.getId() + "}";
			mongo = mongo.replace("'", "\"");
			
			List<Verification> verifications = em.createNativeQuery(mongo, Verification.class)
					.getResultList();
			
			for(Vulnerability v : a.getVulns()){
				if(v.getClosed() == null || v.getClosed().getTime() == 0l){
					if(vid != null && v.getId() == vid){  //vuln is getting scheduled
						for(Verification vvv : verifications){
							if(vvv.getVerificationItems().get(0).getVulnerability().getId() == vid){
								//issue is already assigned but you can change the date and notes
								HibHelper.getInstance().preJoin();
								em.joinTransaction();
								vvv.setStart(start);
								vvv.setEnd(end);
								vvv.setNotes(notes);
								em.persist(vvv);
								HibHelper.getInstance().commit();
								return "successJson";
							}
						}
						Verification ver = new Verification();
						ver.setAssessment(a);
						ver.setAssignedRemediation(a.getRemediation());
						ver.setAssessor(a.getAssessor().get(0));
						ver.setStart(start);
						ver.setEnd(end);
						ver.setNotes(notes);
						ver.setCompleted(new Date(0));
						
						VerificationItem item = new VerificationItem();
						item.setVulnerability(v);
						List<VerificationItem>items = new ArrayList();
						items.add(item);
						ver.setVerificationItems(items);
						HibHelper.getInstance().preJoin();
						em.joinTransaction();
						em.persist(item);
						em.persist(ver);
						HibHelper.getInstance().commit();
						return "successJson";
					}else{ /// we are just listing the findings
						vItem vitem = new vItem();
						vitem.v = v;
						for(Verification ver : verifications){
							if(ver.getVerificationItems().get(0).getVulnerability().getId() == v.getId()){
								if(ver.getVerificationItems().get(0).isPass())
									vitem.status = "Passed Verification";
								else if (ver.getCompleted()!=null && ver.getCompleted().getTime() != 0l && !ver.getVerificationItems().get(0).isPass())
									vitem.status = "Failed Verification";
								else
									vitem.status = "Pending Verification";
								break;
							}
						}
						if(vitem.status == null || vitem.status.equals(""))
							vitem.status = "Unassigned";
						vitems.add(vitem);
						
					}
				}
				
			}
			return SUCCESS;
			
			
			
			
			
		}
	}

	public List<Vulnerability> getVulns() {
		return vulns;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	
	
	public Long getVid() {
		return vid;
	}

	public void setVid(Long vid) {
		this.vid = vid;
	}

	public String getAccessKey() {
		return accessKey;
	}
	


	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setEnd(Date end) {
		this.end = end;
	}



	public class vItem{
		public Vulnerability v;
		public String status;
	}

	public List<vItem> getVitems() {
		return vitems;
	}

	public void setModal(String modal) {
		this.modal = modal;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	
	
	
	

}
