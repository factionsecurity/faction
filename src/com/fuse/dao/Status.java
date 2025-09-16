package com.fuse.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Status {
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private Boolean builtin=false;
	
	public Status() {}
	
	public Status(String name){
		this.name = name;
	}
	private Status(String name, Boolean builtin){
		this.name = name;
		this.builtin = builtin;
	}
	public Long getId() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public Boolean getBuiltin() {
		return this.builtin;
	}
	
	@Transient
	private static List<Status> createBuiltins() {
		List<Status> builtins = new ArrayList<>();
		builtins.add(new Status("Scheduled", true));
		builtins.add(new Status("In Progress", true));
		builtins.add(new Status("On Hold", true));
		builtins.add(new Status("Completed", true));
		builtins.add(new Status("Past Due", true));
		return builtins;
	}
	
	@Transient
	private static Boolean checkIfBuiltinsExist(EntityManager em) {
		return em.createQuery("from Status").getResultList().size() > 0;
	}
	
	@Transient
	private static void createBuiltins(EntityManager em) {
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		List<Status> statuses = createBuiltins();
		for(Status status : statuses) {
			System.out.println("Creating Status: " +status.name);
			em.persist(status);
		}
		HibHelper.getInstance().commit();
	}
	
	@Transient
	public static void createBuiltinsIfNotExist() {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		if(!checkIfBuiltinsExist(em)) {
			System.out.println("Creating Default Status");
			createBuiltins(em);
		}
		em.close();
	}
}