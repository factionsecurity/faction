package com.fuse.tasks;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.fuse.dao.HibHelper;
import com.fuse.utils.SendEmail;

public class EmailThread implements Runnable {

	private String Subject;
	private String Message;
	private Object dataObj;
	private String toAddress;

	public EmailThread(Object dataObj, String Subject, String Message) {
		this.Subject = Subject;
		this.Message = Message;
		this.dataObj = dataObj;
	}

	public EmailThread(String toAddress, String Subject, String Message) {
		this.Subject = Subject;
		this.Message = Message;
		this.toAddress = toAddress;
	}

	@Override
	public void run() {

		EntityManagerFactory emf = HibHelper.getInstance().getEMF();
		EntityManager em = emf.createEntityManager();
		try {
			if (this.toAddress != null && !this.toAddress.equals("")) {
				(new SendEmail(em)).send(this.toAddress, this.Message, this.Subject);
			} else {
				(new SendEmail(em)).send(this.dataObj, this.Message, this.Subject);
			}
			System.out.println("Completed");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			em.close();
		}
	}

}
