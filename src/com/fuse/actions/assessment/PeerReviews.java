package com.fuse.actions.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Comment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.AccessControl;
import com.fuse.utils.FSUtils;


@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/peerreviews/PeerReviews.jsp")
public class PeerReviews extends FSActionSupport{
	
	private List<PeerReview> reviews = new ArrayList<PeerReview>();
	private String comment;
	private String action;
	private Long id;
	private List<RiskLevel>levels;
	
	
	@Action(value="PeerReview")
	public String execute(){
		
		if(!(this.isAcassessor() || this.isAcmanager())) {
			return LOGIN;
		}
		User user = this.getSessionUser();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		
		//Session session = HibHelper.getSessionFactory().openSession();

		
		if(action==null){
			 List<PeerReview> tmp = em.createQuery("from PeerReview where completed = :date").setParameter("date", new Date(0)).getResultList();
			 //Add only PRs that are from the same team. 
			 //TODO: make this a more granular control later on
			 for(PeerReview pr : tmp){ 
				 if(pr.getAssessment().getAssessor().get(0).getTeam() == null ) {
					 reviews.add(pr);
				 }
				 else if(pr.getAssessment().getAssessor().get(0).getTeam().getId().longValue() == user.getTeam().getId().longValue()
						 && !pr.getAssessment().getAssessor().stream().anyMatch(uid -> uid.getId() == user.getId())){
					 reviews.add(pr);
				 }
			 }
		}/*else if(this.action != null && this.action.equals("save")&& this.id != -1){
			//PeerReview pr = (PeerReview)session.createQuery("from PeerReview where id = :id").setLong("id",this.id).uniqueResult();
			
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			PeerReview pr = em.find(PeerReview.class, this.id);
			Comment comment = new Comment();
			comment.setComment(FSUtils.sanitizeHTML(this.comment));
			comment.setDateOfComment(new Date());
			comment.setCommenter(user);
			em.persist(comment);
			//session.getTransaction().begin();
			//session.save(comment);
			if(pr.getComments() == null){
				pr.setComments(new ArrayList<Comment>());
				pr.getComments().add(comment);	
			}else
				pr.getComments().add(comment);
			pr.setCompleted(new Date());
			em.persist(pr);
			HibHelper.getInstance().commit();
			
			
			String email = "<b>Peer Review Completed by <i>" + user.getFname() + " " + user.getLname() + "</i></b><br>" 
					+ "Please Review the following Comments:<br><br>" 
					+ this.comment
					+ "<br>";
			String subject = "Peer Review Complete for " + pr.getAssessment().getName() + " [ "+ pr.getAssessment().getAppId() + " ] ";
			//SendEmail.send(pr, email, subject,em);
			EmailThread emailThread = new EmailThread(pr, subject, email);
			TaskQueueExecutor.getInstance().execute(emailThread);
			return SUCCESSJSON;
		}*/
		
		
		//session.close();
		return SUCCESS;
	}
	public String getActivePR() {
		return "active";
	}

	public List<PeerReview> getReviews() {
		return reviews;
	}

	public void setReviews(List<PeerReview> reviews) {
		this.reviews = reviews;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public List<RiskLevel> getLevels() {
		return levels;
	}
	
	
	

}
