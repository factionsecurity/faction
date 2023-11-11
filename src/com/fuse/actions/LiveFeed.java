package com.fuse.actions;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.dao.Assessment;
import com.fuse.dao.Feed;
import com.fuse.dao.FeedComment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.utils.FSUtils;


@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/livefeed/LiveFeed.jsp")
public class LiveFeed extends FSActionSupport{
	private User user;
	private String action="";
	private TreeMap<Date,Feed> teamFeed =new TreeMap<Date,Feed>(Collections.reverseOrder());
	private TreeMap<Date,Feed> everyFeed= new TreeMap<Date,Feed>(Collections.reverseOrder());
	private TreeMap<Date,Feed> asmtFeed = new TreeMap<Date,Feed>(Collections.reverseOrder());
	private List<Assessment> assessments = new ArrayList<Assessment>();
	
	private String feedid;
	private String comment;
	private Long commentId;
	private Long asmtId;
	private Long teamId;
	private String type;
	private boolean isEveryone;
	private int everyNew = 0;
	private int teamNew = 0;
	private int asmtNew = 0;
	
	


	
	@Action(value="LiveFeed", results={
			@Result(name="checkFeedJson",location="/WEB-INF/jsp/livefeed/checkFeedJson.jsp")
		})
	public String execute(){
		user = this.getSessionUser();
		if(user == null)
			return LOGIN;
		//Session session = HibHelper.getSessionFactory().openSession();
		//EntityManager em = HibHelper.getExistingEM();
		assessments = (List<Assessment>)em.createNativeQuery(
				"{\"assessor\" : "+user.getId() +
				", \"completed\" : {$exists: false}}"
				, Assessment.class).getResultList();
		Feed feedHelper = new Feed();
		for(Assessment a : assessments){
			
			asmtFeed.putAll(feedHelper.getFeedsForAssessment(a, em));
		}
		teamFeed.putAll(feedHelper.getFeedsForTeam(user.getTeam().getId(),em));
		everyFeed.putAll(feedHelper.getFeedsForEveryone(em));
		
		
		if(action.equals("newpost")){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Feed f = new Feed();
			f.setMessage(FSUtils.sanitizeHTML(comment));
			f.setPostDate(new Date());
			f.setPoster(user);
			f.setUpdated(new Date());
			if(type.equals("every")){
				f.setViewRights(Feed.EVERYONE);
			}else if(type.equals("team")){
				List<Teams> teams = new ArrayList<Teams>();
				teams.add(user.getTeam());
				f.setTeams(teams);
				f.setViewRights(Feed.TEAM);
			}else if(type.equals("assessment")){
				f.setAssessmentId(this.asmtId);
				f.setViewRights(Feed.ASSESSMENT);
			}else{
				//session.close();
				//em.close();
				return "successJson";
			}
			em.persist(f);	
			HibHelper.getInstance().commit();

			return "successJson";
			
		}else if(action.equals("like")){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Feed feed = (Feed) em.createQuery("from Feed where guid = :uid").setParameter("uid", this.feedid).getResultList().stream().findFirst().orElse(null);
			if(feed.getLikes() == null)
				feed.setLikes(1);
			else
				feed.setLikes(feed.getLikes()+1);
			
			em.persist(feed);
			HibHelper.getInstance().commit();
		}else if (action.equals("comment")){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Feed feed = (Feed) em.createQuery("from Feed where guid = :uid").setParameter("uid", this.feedid).getResultList().stream().findFirst().orElse(null);
			FeedComment fc = new FeedComment();
			fc.setComment(FSUtils.sanitizeHTML(comment));
			fc.setCommenter(user);
			fc.setDateOfComment(new Date());
			feed.getComments().add(fc);
			em.persist(fc);
			em.persist(feed);
			HibHelper.getInstance().commit();
			
		}else if (action.equals("delpost")){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Feed feed = (Feed) em.createQuery("from Feed where guid = :uid").setParameter("uid", this.feedid).getResultList().stream().findFirst().orElse(null);
			if(user.getId() == feed.getPoster().getId()){
				//session.getTransaction().begin();
				for(FeedComment fc : feed.getComments())
					em.remove(fc);//session.delete(fc);
				
				em.remove(feed);
				HibHelper.getInstance().commit();
				
			}
			
			//session.close();
			//em.close();
			return "successJson";
			
		}else if (action.equals("delcomment")){
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			String queryFeedComment = "{\"guid\" : \"" + FSUtils.sanitizeGUID(this.feedid) + "\"}";
			FeedComment feedComment = (FeedComment)em.createNativeQuery(queryFeedComment,FeedComment.class).getResultList().stream().findFirst().orElse(null);
			String queryFeed = "{ \"comments\" : " + feedComment.getId() +"}";
			Feed feed = (Feed) em.createNativeQuery(queryFeed, Feed.class).getResultList().stream().findFirst().orElse(null);
			if(user.getId() == feedComment.getCommenter().getId()){
				em.remove(feedComment);
				feed.getComments().remove(feedComment);
				em.persist(feed);
				HibHelper.getInstance().commit();
			}
			
			
			//session.close();
			//em.close();
			return "successJson";
			
		}else if(action.equals("check")){
			int tc = (int)this.JSESSION.get("teamPostCount");
			int ac = (int)this.JSESSION.get("asmtPostCount");
			int ec = (int)this.JSESSION.get("everyPostCount");
			if(tc != this.teamFeed.size()){
				teamNew=this.teamFeed.size() - tc;
			}
			if(ac != this.asmtFeed.size()){
				asmtNew=this.asmtFeed.size() - ac;			
						}
			if(ec != this.everyFeed.size()){
				everyNew=this.everyFeed.size()-ec;
			}
			//session.close();
			return "checkFeedJson";
			
		}else{
			
			this.JSESSION.put("teamPostCount", teamFeed.size());
			this.JSESSION.put("asmtPostCount", asmtFeed.size());
			this.JSESSION.put("everyPostCount", everyFeed.size());
			
		}
		
		return SUCCESS;
	}
	
	public String getActiveLF(){
		return "active";
	}

	public User getUser() {
		return user;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public TreeMap<Date, Feed> getTeamFeed() {
		return teamFeed;
	}

	public TreeMap<Date, Feed> getAsmtFeed() {
		return asmtFeed;
	}

	public TreeMap<Date, Feed> getEveryFeed() {
		return everyFeed;
	}

	public void setEveryFeed(TreeMap<Date, Feed> everyFeed) {
		this.everyFeed = everyFeed;
	}

	public List<Assessment> getAssessments() {
		return assessments;
	}

	public String getFeedid() {
		return feedid;
	}

	public void setFeedid(String feedid) {
		this.feedid = feedid;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public Long getAsmtId() {
		return asmtId;
	}

	public void setAsmtId(Long asmtId) {
		this.asmtId = asmtId;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public int getEveryNew() {
		return everyNew;
	}

	public int getTeamNew() {
		return teamNew;
	}

	public int getAsmtNew() {
		return asmtNew;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
	
	
	

	
	


	
	
	
	
	

}
