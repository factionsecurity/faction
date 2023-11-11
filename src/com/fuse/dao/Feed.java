package com.fuse.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.hibernate.Session;

@Entity
public class Feed {
	@Transient
	public static final int EVERYONE = 0x001;
	@Transient
	public static final int ASSESSMENT = 0x010;
	@Transient
	public static final int TEAM = 0x100;
	
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "feedGen")
    @TableGenerator(
        name = "feedGen",
        table = "feedGenseq",
        pkColumnValue = "feed",
        valueColumnName = "nextFeed",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String guid;
	private Date postDate;
	private Date updated;
	private String message;
	private Integer viewRights;
	@ManyToOne(fetch = FetchType.EAGER)
	private User poster;
	//private List<Integer> teamIds;
	@OneToMany(fetch = FetchType.EAGER)
	private List<Teams> teams;
	private Long assessmentId;
	@OneToMany(fetch = FetchType.EAGER)
	private List<FeedComment> comments;
	@ManyToOne(fetch = FetchType.EAGER)
	private Vulnerability vulnerability;
	@OneToMany(fetch = FetchType.EAGER)
	private List<Image> images;
	private Integer likes;
	
	
	

	private void overrideFeed(Feed f){
		this.id = f.getId();
		this.assessmentId = f.getAssessmentId();
		this.comments = f.getComments();
		this.guid = f.getGuid();
		this.images = f.getImages();
		this.likes = f.getLikes();
		this.message = f.getMessage();
		this.postDate = f.getPostDate();
		this.poster = f.getPoster();
		this.teams = f.getTeams();
		this.updated = f.getUpdated();
		this.viewRights = f.getLikes();
		this.vulnerability = f.getVulnerability();
	}
	public Feed(){
		UUID uuid = UUID.randomUUID();
		this.guid = uuid.toString();
	}
	
	
	public Long getAssessmentId() {
		return assessmentId;
	}
	public void setAssessmentId(Long assessmentId) {
		this.assessmentId = assessmentId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public Date getPostDate() {
		return postDate;
	}
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getViewRights() {
		return viewRights;
	}
	public void setViewRights(Integer viewRights) {
		this.viewRights = viewRights;
	}
	public List<Teams> getTeams() {
		return teams;
	}
	public void setTeams(List<Teams> teams) {
		this.teams = teams;
	}
	/*public List<Integer> getTeamIds() {
		return teamIds;
	}
	public void setTeamIds(List<Integer> teamIds) {
		this.teamIds = teamIds;
	}*/
	public User getPoster() {
		return poster;
	}
	public void setPoster(User poster) {
		this.poster = poster;
	}
	
	
	public List<FeedComment> getComments() {
		return comments;
	}
	
	
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public Vulnerability getVulnerability() {
		return vulnerability;
	}
	public void setVulnerability(Vulnerability vulnerability) {
		this.vulnerability = vulnerability;
	}
	public List<Image> getImages() {
		return images;
	}
	public void setImages(List<Image> images) {
		this.images = images;
	}
	public void setComments(List<FeedComment> comments) {
		this.comments = comments;
	}
	
	public Integer getLikes() {
		return likes;
	}
	public void setLikes(Integer likes) {
		this.likes = likes;
	}
	@Transient
	public boolean isEveryone(){
		if((this.viewRights & this.EVERYONE )== this.EVERYONE){
			return true;
		}else
			return false;
	}
	@Transient
	public boolean isAssessmentOnly(){
		if((this.viewRights & this.ASSESSMENT )== this.ASSESSMENT){
			return true;
		}else
			return false;
		
	}
	@Transient
	public boolean isTeamOnly(){
		if((this.viewRights & this.TEAM )== this.TEAM){
			return true;
		}else
			return false;
		
	}
	
	@Transient
	public boolean addNewVulnToFeed(User user, Vulnerability vuln, String message, Integer rights, EntityManager em){
		this.updated=new Date();
		this.postDate=this.updated;
		this.vulnerability = vuln;
		this.message = message;
		this.poster = user;
		this.assessmentId = vuln.getAssessmentId();
		this.viewRights=rights;
		try{
			//Session s = HibHelper.getSessionFactory().openSession();
			//s.getTransaction().begin();
			em.persist(this);
			//s.save(this);
			//s.getTransaction().commit();
			//s.close();
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	
	}

	@Transient
	public TreeMap<Date,Feed> getFeedsForAssessment(Assessment asmt,EntityManager em){
		TreeMap<Date,Feed> myfeed = new TreeMap<Date,Feed>(Collections.reverseOrder());
		try{
			//Session s = HibHelper.getSessionFactory().openSession();
			//TODO: Add Date Here.
			List<Feed> feeds = (List<Feed>)em.createQuery("from Feed where assessmentId = :aid ").setParameter("aid", asmt.getId()).getResultList();
			for(Feed f : feeds){
				myfeed.put(f.getUpdated(), f);
			}
			//s.close();
			return myfeed;	
		}catch(Exception ex){
			ex.printStackTrace();
			return myfeed;
		}
	}
	@Transient
	public TreeMap<Date,Feed> getFeedsForTeam(Long teamid, EntityManager em){
		TreeMap<Date,Feed> myfeed = new TreeMap<Date,Feed>(Collections.reverseOrder());

		try{
			//EntityManager em = HibHelper.getEM();
			//TODO: Add Date Here.
			String query = "{ \"teams\" : " + teamid + "}";
			List<Feed> feeds = (List<Feed>)em.createNativeQuery(query, Feed.class).getResultList();
			for(Feed f : feeds){
				myfeed.put(f.getUpdated(), f);
			}
			//em.close();
			return myfeed;	
		}catch(Exception ex){
			ex.printStackTrace();
			return myfeed;
		}
	}
	
	@Transient
	public TreeMap<Date,Feed> getFeedsForEveryone(EntityManager em ){
		TreeMap<Date,Feed> myfeed = new TreeMap<Date,Feed>(Collections.reverseOrder());

		try{
			//Session s = HibHelper.getSessionFactory().openSession();
			//TODO: Add Date Here.
			List<Feed> feeds = (List<Feed>)em.createQuery("from Feed where viewRights = 1").getResultList();
			for(Feed f : feeds){
				myfeed.put(f.getUpdated(), f);
			}
			//s.close();
			return myfeed;	
		}catch(Exception ex){
			ex.printStackTrace();
			return myfeed;
		}
	}
	

	
	
	
	

}
