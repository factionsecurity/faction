package com.fuse.tasks;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

import org.hibernate.Hibernate;

import com.fuse.dao.Assessment;
import com.fuse.dao.FinalReport;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Notification;
import com.fuse.dao.User;
import com.fuse.utils.GenerateReport;



public class ReportGenThread implements Runnable{

	private String host;
	private Assessment asmt;
	private List<User> notifiers;
	public boolean complete=false;
	private boolean isRetest= false;
	private String report = "";
	
	public ReportGenThread(String host, Assessment asmt, List<User> notifiers, boolean retest){
		this.host = host;
		this.asmt = asmt;
		this.notifiers = notifiers;
		this.isRetest=retest;
	}
	public ReportGenThread(String host, Assessment asmt, List<User> notifiers){
		this.host = host;
		this.asmt = asmt;
		this.notifiers = notifiers;
		
	}
	public ReportGenThread(String host, Assessment asmt, User notifiers){
		this.host = host;
		this.asmt = asmt;
		this.notifiers = new ArrayList();
		this.notifiers.add(notifiers);
		
	}
	public ReportGenThread(String host, Assessment asmt, User notifiers, boolean retest){
		this.host = host;
		this.asmt = asmt;
		this.notifiers = new ArrayList();
		this.notifiers.add(notifiers);
		this.isRetest = retest;
		
	}
	@Override
	public void run() {
		
		System.out.println("Generating Report");
		EntityManager em = HibHelper.getInstance().getEM();
		Long id = this.asmt.getId();
		try{
			GenerateReport genReport = new GenerateReport();
			String docx = "";
			if(isRetest)
				docx = genReport.generateRetestDocxReport(id, em,"");
			else
				docx = genReport.generateDocxReport(id, em);
			this.report = docx;
			em.close();
			em = HibHelper.getInstance().getEM();
			System.out.println("Finished Generating Report");
			System.out.println("Update Notifications");
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Assessment a = em.find(Assessment.class, id);
			if(a.getFinalReport() == null && !isRetest){
				String guid = UUID.randomUUID().toString();
				FinalReport fr = new FinalReport();
				fr.setFilename(guid);
				fr.setBase64EncodedPdf(docx);
				fr.setGentime(new Date());
				em.persist(fr);
				a.setFinalReport(fr);
			}else if(!isRetest){
				a.getFinalReport().setBase64EncodedPdf(docx);
				a.getFinalReport().setGentime(new Date());
			}else if(isRetest && a.getRetestReport() == null){
				String guid = UUID.randomUUID().toString();
				FinalReport fr = new FinalReport();
				fr.setFilename(guid);
				fr.setBase64EncodedPdf(docx);
				fr.setGentime(new Date());
				em.persist(fr);
				a.setRetestReport(fr);
			}else if(isRetest){
				a.getRetestReport().setBase64EncodedPdf(docx);
				a.getRetestReport().setGentime(new Date());
			}
				
			em.persist(a);
				
			for(User u : this.notifiers){
				Notification notify = new Notification();
				notify.setAssessorId(u.getId());
				if(isRetest){
					notify.setMessage("Retest Report Created for <b>" 
							+ a.getAppId() + " " + a.getName()
							+"</b>: <a href='../service/Report.docx?guid=" + a.getRetestReport().getFilename() +"'>Retest Report</a>");
				}else{
					notify.setMessage("Report Generation Completed for <b>" +asmt.getAppId() + " - " 
							+ asmt.getName() + "</b>: <a href='../service/Report.pdf?guid=" + a.getFinalReport().getFilename() + "'>Report</a>");
				}
				notify.setCreated(new Date());
				em.persist(notify);
				
			}
			HibHelper.getInstance().commit();
			System.out.println("Notifications Sent");
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			HibHelper.getInstance().getEM().close();
			this.complete = true;
		}
		
	}
	public String getReport() {
		return report;
	}

	

	
}
