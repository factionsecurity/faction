package com.fuse.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.fuse.dao.Assessment;
import com.fuse.dao.FinalReport;
import com.fuse.dao.FinalReportVariant;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Notification;
import com.fuse.dao.User;
import com.fuse.reporting.GenerateReport;



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
		this.notifiers = new ArrayList<>(notifiers);
		this.isRetest=retest;
	}
	public ReportGenThread(String host, Assessment asmt, List<User> notifiers){
		this.host = host;
		this.asmt = asmt;
		this.notifiers = new ArrayList<>(notifiers);
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
		
		EntityManager em = HibHelper.getInstance().getEM();
		Long id = this.asmt.getId();
		try{
			GenerateReport genReport = new GenerateReport();
			String [] generated = genReport.generateDocxReport(id, em, isRetest);
			this.report = generated[0];
			String fileType = generated[1];
			if(em.isOpen())
				em.close();
			em = HibHelper.getInstance().getEM();
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Assessment a = em.find(Assessment.class, id);
			if(a.getFinalReport() == null && !isRetest){
				FinalReport fr = new FinalReport();
				fr.setRetest(false);
				fr.setFilename(UUID.randomUUID().toString());
				fr.setGentime(new Date());
				fr.getVariants().addAll(buildVariants(generated));
				fr.setVariantCount(fr.getVariants().size());
				em.persist(fr);
				a.setFinalReport(fr);
			}else if(!isRetest){
				FinalReport fr = a.getFinalReport();
				fr.getVariants().clear();
				fr.getVariants().addAll(buildVariants(generated));
				fr.setVariantCount(fr.getVariants().size());
				fr.setGentime(new Date());
			}else if(isRetest && a.getRetestReport() == null){
				FinalReport fr = new FinalReport();
				fr.setRetest(true);
				fr.setFilename(UUID.randomUUID().toString());
				fr.setGentime(new Date());
				fr.getVariants().addAll(buildVariants(generated));
				fr.setVariantCount(fr.getVariants().size());
				em.persist(fr);
				a.setRetestReport(fr);
			}else if(isRetest){
				FinalReport fr = a.getRetestReport();
				fr.getVariants().clear();
				fr.getVariants().addAll(buildVariants(generated));
				fr.setVariantCount(fr.getVariants().size());
				fr.setGentime(new Date());
			}
				
			em.persist(a);
				
			for(User u : this.notifiers){
				Notification notify = new Notification();
				notify.setAssessorId(u.getId());
				if(isRetest){
					notify.setMessage("Retest Report Created for <b>" 
							+ a.getAppId() + " " + a.getName()
							+"</b>: <a href='DownloadReport?guid=" + a.getRetestReport().getFilename() +"'>Retest Report</a>");
				}else{
					notify.setMessage("Report Generation Completed for <b>" +asmt.getAppId() + " - " 
							+ asmt.getName() + "</b>: <a href='DownloadReport?guid=" + a.getFinalReport().getFilename() + "'>Report</a>");
				}
				notify.setCreated(new Date());
				em.persist(notify);
				
			}
			HibHelper.getInstance().commit();
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

	protected List<FinalReportVariant> buildVariants(String[] generated) {
		List<FinalReportVariant> variants = new ArrayList<>();
		FinalReportVariant docx = new FinalReportVariant();
		docx.setFileType("docx");
		docx.setBase64Content(generated[0]);
		variants.add(docx);
		return variants;
	}
}
