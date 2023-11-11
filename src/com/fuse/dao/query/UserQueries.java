package com.fuse.dao.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.fuse.dao.HibHelper;
import com.fuse.dao.Teams;
import com.fuse.dao.User;

public class UserQueries {

	public static Teams getTeam(EntityManager em, String team) {
		String query = String.format("{'TeamName':{'$regex' : '^%s$', '$options' : 'i'}}", team.trim());
		return (Teams) em.createNativeQuery(query, Teams.class).getResultList().stream().findFirst().orElse(null);
	}
	
	public static List<User>getUser(EntityManager em, String username) {
		String query = String.format("{'username':{'$regex' : '^%s$', '$options' : 'i'}}", username.trim());
		return em.createNativeQuery(query, User.class).getResultList();
	}
	public static List<User>getUsersByTeamName(EntityManager em, String teamName) {
		String query = String.format("{'TeamName':{'$regex' : '^%s$', '$options' : 'i'}}", teamName.trim());
		Teams t = (Teams)em.createNativeQuery(query, Teams.class).getResultList().stream().findFirst().orElse(null);
		if(t == null)
			return new ArrayList();
		else {
			query = String.format("{'team_id': %d }", t.getId());
			return em.createNativeQuery(query, User.class).getResultList();
		}
	}
	public static List<User>getUsersByTeamId(EntityManager em, Long teamId) {
		String query = String.format("{'team_id': %d }", teamId);
		return em.createNativeQuery(query, User.class).getResultList();
		
	}
	public static Teams getTeamById(EntityManager em, Long teamId) {
		return em.find(Teams.class, teamId);
	}
	public static boolean removeTeam(EntityManager em, Long teamId) {
		Teams t = (Teams)em.createQuery("from Teams where id = :teamid")
				.setParameter("teamid", teamId).getResultList().stream().findFirst().orElse(null);
		if(t != null) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(t);
			HibHelper.getInstance().commit();
			return true;
		}else {
			return false;
		}
		
		
	}
}
