package com.fuse.dao.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AssessmentType;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Campaign;
import com.fuse.dao.Comment;
import com.fuse.dao.Files;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Image;
import com.fuse.dao.PeerReview;
import com.fuse.dao.Permissions;
import com.fuse.dao.ReportTemplates;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.FSUtils;

public class AssessmentQueries {
	
	public static int OnlyCompleted = 0;
	public static int OnlyNonCompleted = 1;
	public static int All = 2;
	
	public static List<Assessment> getAllAssessmentsbk(EntityManager em, User user, int assessmentType){
		String query = "db.Assessment.aggregate("
				+ "["
				+ " {'$unwind':'$assessor'},"
				+ " {'$lookup' : { 'from' : 'User', 'localField' : 'assessor', 'foreignField' : '_id', 'as' : 'users'}},"
				+ " {'$match':{";
		if(!user.getPermissions().isManager() || user.getPermissions().getAccessLevel() == Permissions.AccessLevelUserOnly) {
			query += "'assessor' : "+user.getId() + "," ;
		}
		
		if(user.getPermissions().getAccessLevel() == Permissions.AccessLevelTeamOnly) {
			query+="'users.team_id': " + user.getTeam().getId() + " , ";
		}
		
		if(assessmentType == OnlyNonCompleted)
			query += "'completed' : {'$exists': false} ";
		else if(assessmentType == OnlyCompleted)
			query += "'completed' : {'$exists': true}  ";
				
		query += "}}"
				+ "])";
		
		return (List<Assessment>)em.createNativeQuery(query, Assessment.class).getResultList();
	} 
	
	public static Assessment getAssessmentById(EntityManager em, Long id) {
		return em.find(Assessment.class, id);
	}
	public static List<Assessment>getAllAssessments(EntityManager em, User user, int assessmentType ){
		
 		String query = "db.Assessment.find({ \"$query\" : {";
		if(!user.getPermissions().isManager() || user.getPermissions().getAccessLevel() == Permissions.AccessLevelUserOnly) {
			query += "\"assessor\" : "+user.getId() + "," ;
		}
		
		
		if(assessmentType == OnlyNonCompleted)
			query += "\"completed\" : {\"$exists\": false}  ";
		else if(assessmentType == OnlyCompleted)
			query += "\"completed\" : {\"$exists\": true}  ";
		
		
		query +="}, \"$orderby\": { \"start\" : 1 }})";
		List<Assessment> assessments = (List<Assessment>)em.createNativeQuery(
				query, Assessment.class).getResultList();
		
		if(user.getPermissions().getAccessLevel() == Permissions.AccessLevelTeamOnly)
			return (List<Assessment>)assessments.stream().filter(
					a -> hasTeam(user,a) 
					).collect(Collectors.toList()); 
		else 
			return assessments;
		
	}
	
	
	
	public static List<Assessment>getAssessmentsByCampaign(EntityManager em, User user, Long CampId, int assessmentType){
		
		String query = "db.Assessment.find({ \"$query\" : {";
		if(!user.getPermissions().isManager() && user.getPermissions().getAccessLevel() == Permissions.AccessLevelUserOnly) {
			query += "\"assessor\" : "+user.getId() + "," ;
		}
		

		
		query += "\"campaign_id\" : " + CampId +",";
		
		if(assessmentType == OnlyNonCompleted)
			query += "\"completed\" : {\"$exists\": false}  ";
		else if(assessmentType == OnlyCompleted)
			query += "\"completed\" : {\"$exists\": true}  ";
		
		query += "},\"$orderby\": { \"start\" : 1 }})";
	
		
		List<Assessment> assessments = (List<Assessment>)em.createNativeQuery(
				query, Assessment.class).getResultList();
		
		if(user.getPermissions().getAccessLevel() == Permissions.AccessLevelTeamOnly)
			return (List<Assessment>)assessments.stream().filter(
					a -> hasTeam(user,a) 
					).collect(Collectors.toList()); 
		else 
			return assessments;
		
	}
	
	public static List<Assessment>getAssessmentsByUserId(EntityManager em, User user, Long UserId, int assessmentType){
		
		if(user.getPermissions().getAccessLevel() ==  Permissions.AccessLevelUserOnly && user.getId() != UserId)
			return new ArrayList();
			
		String query = "db.Assessment.find({ \"$query\" : {";
		
		query += "\"assessor\" : "+UserId + "," ;
		
		
	
		if(assessmentType == OnlyNonCompleted)
			query += "\"completed\" : {\"$exists\": false}} , ";
		
		else if(assessmentType == OnlyCompleted)
			query += "\"completed\" : {\"$exists\": true}} , ";
		
		query += "\"$orderby\": { \"start\" : 1 }})";
	
		
		List<Assessment> assessments = (List<Assessment>)em.createNativeQuery(
				query, Assessment.class).getResultList();
		
		if(user.getPermissions().getAccessLevel() == Permissions.AccessLevelTeamOnly)
			return (List<Assessment>)assessments.stream().filter(
					a -> hasTeam(user,a) 
					).collect(Collectors.toList()); 
		else 
			return assessments;
		
	}
	
	public static Assessment getAssessmentByUserId(EntityManager em, Long UserId, Long AssessmentId, int assessmentType){
		
		
		String query = "db.Assessment.find({ \"$query\" : {";
		
		query += "\"_id\" : " + AssessmentId + ", \"assessor\" : "+UserId  ;
		
		
	
		if(assessmentType == OnlyNonCompleted)
			query += ", \"completed\" : {\"$exists\": false}";
		
		else if(assessmentType == OnlyCompleted)
			query += ", \"completed\" : {\"$exists\": true}";
		

		query +="}})";
		
		Assessment assessments = (Assessment)em.createNativeQuery(
				query, Assessment.class).getResultList().stream().findFirst().orElse(null);
		
		
		return assessments;
		
	}
	
	public static List<Assessment>getAssessmentsByAppDesc(EntityManager em, User user, String AppId, String AppName, int assessmentType){
		
		String query = "db.Assessment.find({ \"$query\" : {";
		if(!user.getPermissions().isManager() || user.getPermissions().getAccessLevel() == Permissions.AccessLevelUserOnly) {
			query += "\"assessor\" : "+user.getId() + "," ;
		}
		
	
		if(assessmentType == OnlyNonCompleted)
			query += "\"completed\" : {\"$exists\": false} , ";
		else if(assessmentType == OnlyCompleted)
			query += "\"completed\" : {\"$exists\": true} , ";
		
		if(AppId != null && AppName != null) {
			query += "\"appId\" : \"" + FSUtils.sanitizeMongo(AppId) + "\", \"name\": \"" + FSUtils.sanitizeMongo(AppName) + "\" ";
		}else if(AppId == null && AppName != null) {
			query += " \"name\": \"" + FSUtils.sanitizeMongo(AppName) + "\" ";
		}else if(AppId != null && AppName == null) {
			query += "\"appId\" : \"" + FSUtils.sanitizeMongo(AppId) + "\" ";
		}
		
		query += "}, \"$orderby\": { \"start\" : 1 }})";
	
		
		List<Assessment> assessments = (List<Assessment>)em.createNativeQuery(
				query, Assessment.class).getResultList();
		
		if(user.getPermissions().getAccessLevel() == Permissions.AccessLevelTeamOnly)
			return (List<Assessment>)assessments.stream().filter(
					a -> hasTeam(user,a) 
					).collect(Collectors.toList()); 
		else 
			return assessments;
		
	}
	
	public static Assessment getAssessment(EntityManager em, User user, Long AssessmentId) {
		Assessment asmt = em.find(Assessment.class, AssessmentId);
		if(!user.getPermissions().isManager() || user.getPermissions().getAccessLevel() == Permissions.AccessLevelUserOnly) {
			if(!asmt.getAssessor().stream().anyMatch(u -> u.getId() == user.getId())) {
				return null;
			}
		}
		if(user.getPermissions().getAccessLevel() == Permissions.AccessLevelTeamOnly) {
			if(!hasTeam(user,asmt))
				return null;
		}
		
		return asmt;
		
	}
	
	public static PeerReview getPeerReviewFromPRComment(EntityManager em, Long commentid) {
		String Query = "{ 'comments' : " + commentid +"}";
		PeerReview pr = (PeerReview) em.createNativeQuery(Query, PeerReview.class).getResultList()
				.stream().findFirst().orElse(null);
		return pr;
	}
	public static List<AuditLog> getLogs(EntityManager em,Assessment asmt){
		if(asmt == null)
			return new ArrayList();
		
		return (List<AuditLog>)em.createQuery("from AuditLog where compname = :type and compid = :asmtid")
			.setParameter("type", AuditLog.CompAssessment)
			.setParameter("asmtid", asmt.getId())
			.getResultList();
	}
	public static Campaign getCampaignByName(EntityManager em, String name) {
		String query = String.format("{'name':{'$regex' : '^%s$', '$options' : 'i'}}", name.trim());
		return (Campaign) em.createNativeQuery(query, Campaign.class).getResultList().stream().findFirst().orElse(null);
	
	}
	public static Campaign getCampaignById(EntityManager em, Long id) {
		return em.find(Campaign.class	, id);
	}
	public static AssessmentType getAssessmentTypeByName(EntityManager em, String name) {
		String query = String.format("{'type':{'$regex' : '^%s$', '$options' : 'i'}}", name.trim());
		return (AssessmentType) em.createNativeQuery(query, AssessmentType.class).getResultList().stream().findFirst().orElse(null);
	
	}
	public static AssessmentType getAssessmentTypeById(EntityManager em, Long id) {
		return em.find(AssessmentType.class	, id);
	}
	
	public static List<Files> getFilesByAssessmentId(EntityManager em, Long id){
		return em
			.createQuery("from Files where type = :type and entityId = :id")
			.setParameter("type", Files.ASSESSMENT)
			.setParameter("id",id).getResultList();
	}
	public static boolean checkForReportTemplates(EntityManager em, Assessment assessment){
		return checkForReportTemplates(em, assessment, false);
	}
	public static boolean checkForReportTemplates(EntityManager em, Assessment assessment, Boolean retest){
		List<ReportTemplates> templates = em.createQuery("from ReportTemplates").getResultList();
		boolean found=false;
		for(ReportTemplates template : templates){
			//TODO: fix lazy loading issue
			try {
				if(template.getType().getId() == assessment.getType().getId() &&
						template.getTeam().getId() == assessment.getAssessor().get(0).getTeam().getId() &&
						template.isRetest() == retest) {
					found = true;
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return found;
	}
	
	public static void saveAssessment(FSActionSupport sender, EntityManager em, Assessment assessment, String auditDescription) {
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(assessment);
		AuditLog.audit(sender, auditDescription, AuditLog.UserAction,
				AuditLog.CompAssessment, assessment.getId(), false);
		HibHelper.getInstance().commit();
	}
	public static void saveAll(FSActionSupport sender, Assessment a, EntityManager em,  String auditDescription, Object...entities) {
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		for (Object entity : entities) {
			if(entity.getClass().getName().contains("PersistentBag")) {
				List<Object> list= (List)entity;
				for(Object item : list) {
					em.persist(item.getClass().cast(item));
				}
			}else if(entity.getClass().getName().contains("List")) {
				List<Object> list= (ArrayList)entity;
				for(Object item : list) {
					em.persist(item.getClass().cast(item));
				}
			}else {
				em.persist(entity.getClass().cast(entity));
			}
		}
		AuditLog.audit(sender, auditDescription, AuditLog.UserAction,
				AuditLog.CompAssessment, a.getId(), false);
		HibHelper.getInstance().commit();
	}
	
	
	
	private static boolean hasTeam(User user, Assessment asmt) {
		if(asmt == null || asmt.getAssessor().size() == 0)
			return false;
		if(asmt.getAssessor() == null || asmt.getAssessor().size() == 0) {
			return false;
		}
		Long asmtTeam = asmt.getAssessor().get(0).getTeam().getId();
		return user.getTeam().getId().equals(asmtTeam);
	}
	
	public static String replaceImageLinks(Assessment asmt, String text) {
		Long aid= asmt.getId();
		String matchPrefix = "getImage\\?id(=|&#61;)" + aid + ":";
		for(Image img : asmt.getImages()) {
			String matchStr = matchPrefix + img.getGuid();
			text = text.replaceAll( matchStr, img.getBase64Image());
		}
		return text;
		
	}
	public static void updateImages(Assessment asmt, Vulnerability v) {
		v.setDescription(
				replaceImageLinks(asmt, v.getDescription())
				);
		v.setRecommendation(
				replaceImageLinks(asmt, v.getRecommendation())
				);
		v.setDetails(
				replaceImageLinks(asmt, v.getDetails())
		);
	}
	public static void updateImages(EntityManager em, Vulnerability v) {
		Assessment asmt = getAssessmentById(em, v.getAssessmentId());
		updateImages(asmt,v);
	}
	public static void updateImages(Assessment asmt) {
		asmt.setSummary(
				replaceImageLinks(asmt, asmt.getSummary())
				);
		asmt.setRiskAnalysis(
				replaceImageLinks(asmt, asmt.getRiskAnalysis())
				);
	}
	
	public static void removeImages(Assessment assessment) {
		List<Image>removeImages = new ArrayList<>();
		for(Image image : assessment.getImages()) {
			boolean found = false;
			String guid = image.getGuid();
			if(assessment.getSummary() != null && assessment.getSummary().contains(guid)) {
				found = true;
			}else if(assessment.getRiskAnalysis() != null && assessment.getRiskAnalysis().contains(guid)) {
				found = true;
			}else {
				for(Vulnerability vuln: assessment.getVulns()) {
					if(vuln.getDescription() != null && vuln.getDescription().contains(guid)) {
						found = true;
					}else if(vuln.getRecommendation() != null && vuln.getRecommendation().contains(guid)) {
						found = true;
					}else if(vuln.getDetails() != null && vuln.getDetails().contains(guid)) {
						found = true;
					}
				}
			}
			if(!found) {
				removeImages.add(image);
			}
		}
		for(Image removed : removeImages) {
			assessment.getImages().remove(removed);
		}
		
	}

}
