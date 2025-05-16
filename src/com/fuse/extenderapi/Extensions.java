package com.fuse.extenderapi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ThisFieldRefForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fuse.dao.AppStore;
import com.fuse.dao.Assessment;
import com.fuse.dao.CheckListAnswers;
import com.fuse.dao.CustomField;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.faction.elements.CheckList;
import com.faction.elements.results.AssessmentManagerResult;
import com.faction.elements.results.InventoryResult;
import com.faction.elements.utils.Log;
import com.faction.extender.ApplicationInventory;
import com.faction.extender.AssessmentManager;
import com.faction.extender.ReportManager;
import com.faction.extender.VerificationManager;
import com.faction.extender.VulnerabilityManager;

public class Extensions {

	public enum EventType {
		INVENTORY, VER_MANAGER, ASMT_MANAGER, VULN_MANAGER, REPORT_MANAGER
	}

	private List<AssessmentManager> assessmentManagers = new ArrayList<>();
	private List<VulnerabilityManager> vulnerabilityManagers = new ArrayList<>();
	private List<VerificationManager> verificationManagers = new ArrayList<>();
	private List<ApplicationInventory> inventoryManagers = new ArrayList<>();
	private List<ReportManager> reportManagers = new ArrayList<>();
	private List<Log> logs = new ArrayList<>();
	private EventType type;
	private final EntityManagerFactory entityManagerFactory;
	
	public Extensions(EntityManagerFactory entityManagerFactory, EventType type) {
		this.entityManagerFactory = entityManagerFactory;
		this.type = type;
		try {
			this.loadExtensions();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public Extensions(EventType type) {
		this.entityManagerFactory = HibHelper.getInstance().getEMF();
		this.type = type;
		try {
			this.loadExtensions();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public boolean isExtended() {
		switch (this.type) {
		case INVENTORY:
			return this.inventoryManagers.size() > 0;
		case ASMT_MANAGER:
			return this.assessmentManagers.size() > 0;
		case VULN_MANAGER:
			return this.vulnerabilityManagers.size() > 0;
		case VER_MANAGER:
			return this.verificationManagers.size() > 0;
		case REPORT_MANAGER:
			return this.reportManagers.size() > 0;
		default:
			return false;
		}

	}

	private void persistVulnerabilities(EntityManager em,
			List<com.faction.elements.Vulnerability> clonedVulnerabilities, List<Vulnerability> daoVulnerabilities) {
		if (clonedVulnerabilities != null) {
			for (com.faction.elements.Vulnerability cloneVuln : clonedVulnerabilities) {
				for (Vulnerability daoVuln : daoVulnerabilities) {
					if (cloneVuln.getId() == daoVuln.getId()) {
						copy(cloneVuln, daoVuln);
						List<CustomField> fields = daoVuln.getCustomFields();
						fields = updateCustomFields(cloneVuln.getCustomFields(), fields);
						HibHelper.getInstance().preJoin();
						em.joinTransaction();
						em.persist(daoVuln);
						HibHelper.getInstance().commit();
						break;
					}
				}
			}
		}
	}

	private void persistAssessment(EntityManager em, com.faction.elements.Assessment clonedAssessment,
			Assessment daoAssessment) {
		List<com.faction.elements.CustomField> updatedFields = clonedAssessment.getCustomFields();
		List<CustomField> fields = daoAssessment.getCustomFields();
		copy(clonedAssessment, daoAssessment);
		fields = updateCustomFields(updatedFields, fields);
		daoAssessment.setCustomFields(fields);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(daoAssessment);
		HibHelper.getInstance().commit();
	}

	private List<CustomField> updateCustomFields(List<com.faction.elements.CustomField> clonedFields,
			List<CustomField> daoFields) {
		if (clonedFields != null && clonedFields.size() > 0) {
			for (com.faction.elements.CustomField updatedField : clonedFields) {
				for (CustomField originalField : daoFields) {
					if (updatedField.getType().getId() == originalField.getType().getId()) {
						originalField.setValue(updatedField.getValue());
					}
				}
			}
		}
		return daoFields;
	}
	
	private List<com.faction.elements.CheckList> cloneChecklists(Assessment assessment){
			Map<String,com.faction.elements.CheckList> checklists = new HashMap<>(); // This is to make mapping easy
			
			for (CheckListAnswers a : assessment.getAnswers()) {
				if(!checklists.containsKey(a.getChecklist())) {
					com.faction.elements.CheckList checklist = new com.faction.elements.CheckList();
					checklist.setName(a.getChecklist());
					checklist.setCheckListItems(new ArrayList<>());
					checklists.put(a.getChecklist(), checklist);
					
				}
				com.faction.elements.CheckListItem item = new com.faction.elements.CheckListItem();
				com.faction.elements.CheckListItem.Answer answer =  com.faction.elements.CheckListItem.Answer
						.getAnswer(a.getAnswer().getValue());
				item.setAnswer(answer);
				item.setNotes(a.getNotes());
				item.setQuestion(a.getQuestion());
				com.faction.elements.CheckList checklist = checklists.get(a.getChecklist());
				checklist.getCheckListItems().add(item);
				
			}
			com.faction.elements.CheckList[] clonedChecklists = checklists
					.values().toArray( new com.faction.elements.CheckList[checklists.size()]);
			
			return Arrays.asList(clonedChecklists);
	}

	private List<com.faction.elements.CustomField> cloneCustomFields(Assessment daoAssessment) {
		List<CustomField> daoFields = daoAssessment.getCustomFields();
		List<com.faction.elements.CustomField> clonedFields = new ArrayList<>();
		if (daoFields != null) {
			for (CustomField field : daoFields) {
				com.faction.elements.CustomField tmpField = new com.faction.elements.CustomField();
				com.faction.elements.CustomType tmpType = new com.faction.elements.CustomType();
				tmpType.setKey(field.getType().getKey());
				tmpType.setVariable(field.getType().getVariable());
				tmpField.setType(tmpType);
				tmpField.setValue(field.getValue());
				clonedFields.add(tmpField);

			}
			if (daoFields != null && daoFields.size() > 0) {
				for (CustomField originalField : daoFields) {
					for (com.faction.elements.CustomField clonedField : clonedFields) {
						if (clonedField.getType().getId() == originalField.getType().getId()) {
							clonedField.setValue(originalField.getValue());
						}
					}
				}
			}
		}
		return clonedFields;
	}
	private List<com.faction.elements.CustomField> cloneCustomFields(Vulnerability daoVulnerability) {
		List<CustomField> daoFields = daoVulnerability.getCustomFields();
		List<com.faction.elements.CustomField> clonedFields = new ArrayList<>();
		if (daoFields != null) {
			for (CustomField field : daoFields) {
				com.faction.elements.CustomField tmpField = new com.faction.elements.CustomField();
				com.faction.elements.CustomType tmpType = new com.faction.elements.CustomType();
				tmpType.setKey(field.getType().getKey());
				tmpType.setVariable(field.getType().getVariable());
				tmpField.setType(tmpType);
				tmpField.setValue(field.getValue());
				clonedFields.add(tmpField);

			}
			if (daoFields != null && daoFields.size() > 0) {
				for (CustomField originalField : daoFields) {
					for (com.faction.elements.CustomField clonedField : clonedFields) {
						if (clonedField.getType().getId() == originalField.getType().getId()) {
							clonedField.setValue(originalField.getValue());
						}
					}
				}
			}
		}
		return clonedFields;
	}
	@SuppressWarnings("unchecked")
	public String updateReport(Assessment localAssessment, String reportText) {
		if (!this.isExtended())
			return reportText;
		try {
			// Clone Assessment
			com.faction.elements.Assessment tmpAssessment = new com.faction.elements.Assessment();
			copy(localAssessment, tmpAssessment);
			// Clone Vulns
			List<com.faction.elements.Vulnerability> tmpVulns = new ArrayList();
			List<Vulnerability> vulnerabilities = localAssessment.getVulns();
			for (Vulnerability v : vulnerabilities) {
				com.faction.elements.Vulnerability tVuln = new com.faction.elements.Vulnerability();
				copy(v, tVuln);
				tVuln.setCustomFields(this.cloneCustomFields(v));
				tmpVulns.add(tVuln);
			}
			// Clone Checklists
			List<com.faction.elements.CheckList> clonedChecklists = this.cloneChecklists(localAssessment);
			tmpAssessment.setChecklists(clonedChecklists);
			
			// Clone Engagement
			com.faction.elements.User eng = new com.faction.elements.User();
			copy(localAssessment.getEngagement(), eng);
			tmpAssessment.setEngagementContact(eng);
			// Clone Remediation
			com.faction.elements.User rem = new com.faction.elements.User();
			copy(localAssessment.getRemediation(), rem);
			tmpAssessment.setRemediationContact(rem);
			// Clone Assessors
			List<com.faction.elements.User> assessors = new ArrayList<com.faction.elements.User>();
			if (localAssessment.getAssessor() != null) {
				for (User u : localAssessment.getAssessor()) {
					com.faction.elements.User assessor = new com.faction.elements.User();
					copy(u, assessor);
					assessors.add(assessor);
				}
			}
			tmpAssessment.setAssessors(assessors);
			// Clone Custom Fields
			tmpAssessment.setCustomFields(cloneCustomFields(localAssessment));
			// Clone Campaign
			if (localAssessment.getCampaign() != null) {
				tmpAssessment.setCampaign(localAssessment.getCampaign().getName());
			}
			if (localAssessment.getType() != null) {
				tmpAssessment.setType(localAssessment.getType().getType());
			}
			for (ReportManager mgr : this.reportManagers) {
				String updatedText = mgr.reportCreate(tmpAssessment, tmpVulns,reportText);
				if(updatedText != null) {
					reportText = updatedText;
				}
				
				this.logs.addAll(mgr.getLogs());

			}
			return reportText;

		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return reportText;
	}
	
	@SuppressWarnings("unchecked")
	public CompletableFuture<Boolean> execute(Assessment assessment, AssessmentManager.Operation operation) {
		if (!this.isExtended())
			return null;

		return CompletableFuture.supplyAsync(() -> {

			EntityManager em = HibHelper.getInstance().getEM();
			try {
				Assessment localAssessment = em.find(Assessment.class, assessment.getId());
				// Clone Assessment
				com.faction.elements.Assessment tmpAssessment = new com.faction.elements.Assessment();
				copy(localAssessment, tmpAssessment);
				
				// Clone Checklists
				List<com.faction.elements.CheckList> clonedChecklists = this.cloneChecklists(localAssessment);
				tmpAssessment.setChecklists(clonedChecklists);
				
				// Clone Vulns
				List<com.faction.elements.Vulnerability> tmpVulns = new ArrayList();
				List<Vulnerability> vulnerabilities = localAssessment.getVulns();
				for (Vulnerability v : vulnerabilities) {
					com.faction.elements.Vulnerability tVuln = new com.faction.elements.Vulnerability();
					copy(v, tVuln);
					tmpVulns.add(tVuln);
				}
				// Clone Engagement
				com.faction.elements.User eng = new com.faction.elements.User();
				copy(localAssessment.getEngagement(), eng);
				tmpAssessment.setEngagementContact(eng);
				// Clone Remediation
				com.faction.elements.User rem = new com.faction.elements.User();
				copy(localAssessment.getRemediation(), rem);
				tmpAssessment.setRemediationContact(rem);
				// Clone Assessors
				List<com.faction.elements.User> assessors = new ArrayList<com.faction.elements.User>();
				if (localAssessment.getAssessor() != null) {
					for (User u : localAssessment.getAssessor()) {
						com.faction.elements.User assessor = new com.faction.elements.User();
						copy(u, assessor);
						assessors.add(assessor);
					}
				}
				tmpAssessment.setAssessors(assessors);
				// Clone Custom Fields
				tmpAssessment.setCustomFields(cloneCustomFields(localAssessment));
				// Clone Campaign
				if (localAssessment.getCampaign() != null) {
					tmpAssessment.setCampaign(localAssessment.getCampaign().getName());
				}
				if (localAssessment.getType() != null) {
					tmpAssessment.setType(localAssessment.getType().getType());
				}
				// Execute Extensions
				AssessmentManagerResult clonedArguments = new AssessmentManagerResult();
				clonedArguments.setAssessment(tmpAssessment);
				clonedArguments.setVulnerabilities(tmpVulns);
				for (AssessmentManager mgr : this.assessmentManagers) {

					AssessmentManagerResult result = mgr.assessmentChange(clonedArguments.getAssessment(),
							clonedArguments.getVulnerabilities(), operation);
					this.logs.addAll(mgr.getLogs());

					// Persist
					if (result != null && result.getVulnerabilities() != null) {
						clonedArguments.setVulnerabilities(result.getVulnerabilities());
						this.persistVulnerabilities(em, result.getVulnerabilities(), vulnerabilities);
					}
					if (result != null && result.getAssessment() != null) {
						clonedArguments.setAssessment(result.getAssessment());
						this.persistAssessment(em, result.getAssessment(), localAssessment);
					}
				}
				return true;

			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (Throwable ex) {
				ex.printStackTrace();
			} finally {
				em.close();
			}
			return false;
		});

	}

	public CompletableFuture<Boolean> execute(Assessment assessment, Vulnerability vuln,
			VulnerabilityManager.Operation operation) {
		if (!this.isExtended())
			return null;

		return CompletableFuture.supplyAsync(() -> {
			EntityManager em = HibHelper.getInstance().getEM();
			Assessment localAssessment = em.find(Assessment.class, assessment.getId());
			Vulnerability localVuln = em.find(Vulnerability.class, vuln.getId());
			try {

				com.faction.elements.Assessment tmpAssessment = new com.faction.elements.Assessment();
				com.faction.elements.Vulnerability tmpVuln = new com.faction.elements.Vulnerability();

				// Clone Vulnerability
				copy(localVuln, tmpVuln);
				// Clone Assessment
				copy(localAssessment, tmpAssessment);

				// Execute Extensions
				for (VulnerabilityManager mgr : this.vulnerabilityManagers) {

					com.faction.elements.Vulnerability updatedVuln = mgr.vulnChange(tmpAssessment, tmpVuln, operation);
					this.logs.addAll(mgr.getLogs());
					// Persist
					if (updatedVuln != null) {
						tmpVuln = updatedVuln;
						persistVulnerabilities(em, Arrays.asList(tmpVuln), Arrays.asList(localVuln));

					}
				}
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				em.close();
			}
			return false;
		});

	}

	public CompletableFuture<Boolean> execute(Verification verification, VerificationManager.Operation operation) {
		if (!this.isExtended())
			return null;

		return CompletableFuture.supplyAsync(() -> {
			EntityManager em = HibHelper.getInstance().getEM();
			try {
				Verification localVerification = em.find(Verification.class, verification.getId());
				// Clone Vulnerability
				com.faction.elements.Vulnerability clonedVuln = new com.faction.elements.Vulnerability();
				Vulnerability vulnerability = localVerification.getVerificationItems().get(0).getVulnerability();
				copy(vulnerability, clonedVuln);
				// Clone User
				com.faction.elements.User clonedUser = new com.faction.elements.User();
				copy(localVerification.getAssessor(), clonedUser);
				// Clone Verification
				com.faction.elements.Verification clonedVerification = new com.faction.elements.Verification();
				copy(localVerification, clonedVerification);

				// Execute Extensions
				for (VerificationManager mgr : this.verificationManagers) {

					com.faction.elements.Vulnerability updatedVuln = mgr.verificationChange(clonedUser, clonedVuln,
							clonedVerification, operation);
					this.logs.addAll(mgr.getLogs());
					// Persist
					if (updatedVuln != null) {
						clonedVuln = updatedVuln;
						persistVulnerabilities(em, Arrays.asList(clonedVuln), Arrays.asList(vulnerability));
					}
				}
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				em.close();
			}
			return false;
		});

	}

	public List<InventoryResult> execute(String appId, String appName) {
		if (!this.isExtended())
			return null;

		List<InventoryResult> allResults = new ArrayList<>();
		try {
			for (ApplicationInventory mgr : this.inventoryManagers) {
				InventoryResult[] results = mgr.search(appId, appName);
				allResults.addAll(Arrays.asList(results));
				this.logs.addAll(mgr.getLogs());

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return allResults;

	}

	private static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null)
				emptyNames.add(pd.getName());
		}
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}

	private static void copy(Object source, Object dest) {
		if (source == null) {
			dest = null;
		} else {
			String[] nulls = getNullPropertyNames(source);
			BeanUtils.copyProperties(source, dest, nulls);
		}
	}

	private URLClassLoader dynamicExtensionClassLoader(AppStore app) {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			ExtensionClassLoader loader = new ExtensionClassLoader();
			loader.loadJarFromAppStore(app);
			URL url = loader.getURL();
			return new URLClassLoader(new URL[] { url }, currentClassLoader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public void loadExtensions() throws MalformedURLException {

		List<AppStore> apps = this.sortApps();
		for (AppStore app : apps) {
			
			URLClassLoader extensionLoader = dynamicExtensionClassLoader(app);
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

			// Load Assessment Manager Extensions
			if(this.type == EventType.ASMT_MANAGER) {
				try {
					Thread.currentThread().setContextClassLoader(extensionLoader);
					for (AssessmentManager asmtMgr : ServiceLoader.load(AssessmentManager.class, extensionLoader)) {
						if (asmtMgr != null && app.getAssessmentEnabled()) {
							asmtMgr.setConfigs(app.getHashMapConfig());
							assessmentManagers.add(asmtMgr);
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				} finally {
					Thread.currentThread().setContextClassLoader(currentClassLoader);
				}
			}
			// Load Report Manager Extensions
			if(this.type == EventType.REPORT_MANAGER) {
				try {
					Thread.currentThread().setContextClassLoader(extensionLoader);
					for (ReportManager reportMgr : ServiceLoader.load(ReportManager.class, extensionLoader)) {
						if (reportMgr != null && app.getReportEnabled()) {  ///eventually need to make this report enabled and have a sep section in app store
							reportMgr.setConfigs(app.getHashMapConfig());
							reportManagers.add(reportMgr);
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				} finally {
					Thread.currentThread().setContextClassLoader(currentClassLoader);
				}
			}

			// Load Vulnerability Manager Extensions
			if(this.type == EventType.VULN_MANAGER) {
				try {
					Thread.currentThread().setContextClassLoader(extensionLoader);
					for (VulnerabilityManager vulnMgr : ServiceLoader.load(VulnerabilityManager.class, extensionLoader)) {
						if (vulnMgr != null && app.getVulnerabilityEnabled()) {
							vulnMgr.setConfigs(app.getHashMapConfig());
							vulnerabilityManagers.add(vulnMgr);
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				} finally {
					Thread.currentThread().setContextClassLoader(currentClassLoader);
				}
			}

			// Load Verification Manager Extensions
			if(this.type == EventType.VER_MANAGER) {
				try {
					Thread.currentThread().setContextClassLoader(extensionLoader);
					for (VerificationManager verMgr : ServiceLoader.load(VerificationManager.class, extensionLoader)) {
						if (verMgr != null && app.getVerificationEnabled()) {
							verMgr.setConfigs(app.getHashMapConfig());
							verificationManagers.add(verMgr);
						}
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
				} finally {
					Thread.currentThread().setContextClassLoader(currentClassLoader);
				}
			}

			// Load Application Inventory Manager Extensions
			if(this.type == EventType.INVENTORY) {
				try {
					Thread.currentThread().setContextClassLoader(extensionLoader);
					for (ApplicationInventory invMgr : ServiceLoader.load(ApplicationInventory.class, extensionLoader)) {
						if (invMgr != null && app.getInventoryEnabled()) {
							invMgr.setConfigs(app.getHashMapConfig());
							inventoryManagers.add(invMgr);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} catch (Throwable ex) {
					ex.printStackTrace();
				} finally {
					Thread.currentThread().setContextClassLoader(currentClassLoader);
				}
			}
		}
	}

	private List<AppStore> sortApps() {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			List<AppStore> apps = em.createQuery("from AppStore order by order").getResultList();
			return apps;
		} finally {
			em.close();
		}
	}

	public List<Log> getLogs() {
		return this.logs;

	}

}