package com.fuse.dao;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.fuse.utils.FSUtils;

public class HibHelper {

	private TransactionManager tm;
	private static EntityManagerFactory emf;
	private EntityManager em;

	private static final HibHelper singleton = new HibHelper();

	public static HibHelper getInstance() {
		return singleton;
	}

	public EntityManagerFactory getEMF() {
		try {
			if (tm == null) {
				tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
			}
			if (emf == null || !emf.isOpen()) {

				Map<String, String> properties = new HashMap<String, String>();
				properties.put("hibernate.ogm.datastore.username", FSUtils.getEnv("FACTION_MONGO_USER"));
				properties.put("hibernate.ogm.datastore.password", FSUtils.getEnv("FACTION_MONGO_PASSWORD"));

				properties.put("hibernate.ogm.datastore.host", FSUtils.getEnv("FACTION_MONGO_HOST"));
				properties.put("hibernate.ogm.datastore.database", System.getenv("FACTION_MONGO_DATABASE"));
				properties.put("hibernate.ogm.datastore.port", System.getenv("FACTION_MONGO_PORT"));
				//if(!System.getenv("FACTION_MONGO_AUTH_DATABASE").equals(""))
				properties.put("hibernate.ogm.mongodb.authentication_database", System.getenv("FACTION_MONGO_AUTH_DATABASE"));
				if(System.getenv("FACTION_MONGO_SSL").equals("true")) {
					properties.put("hibernate.ogm.mongodb.driver.sslEnabled", true);
				}
				
				emf = Persistence.createEntityManagerFactory("Faction", properties);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return emf;

	}

	public EntityManager getEM() {
		try {
			if (tm == null) {
				tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
			}
			if (emf == null || !emf.isOpen()) {
				emf = Persistence.createEntityManagerFactory("Faction");
			}

			if (em == null || !em.isOpen()) {
				em = emf.createEntityManager();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return em;
	}

	public void preJoin() {
		try {
			if (tm.getStatus() == Status.STATUS_ROLLEDBACK || tm.getStatus() == Status.STATUS_ROLLEDBACK)
				tm.rollback();
			tm.begin();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}

	}

	public void commit() {
		try {

			tm.commit();

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			e.printStackTrace();
		} catch (RollbackException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
	}

	public void closeEMF() {
		emf.close();

	}

}
