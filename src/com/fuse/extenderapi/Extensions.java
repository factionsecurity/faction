package com.fuse.extenderapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ThisFieldRefForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fuse.dao.Assessment;
import com.fuse.dao.CustomField;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.faction.extender.ApplicationInventory;
import com.faction.extender.AssessmentManager;
import com.faction.extender.AssessmentManagerResult;
import com.faction.extender.VerificationManager;
import com.faction.extender.VulnerabilityManager;

public class Extensions {

	public enum EventType {
		INVENTORY, VER_MANAGER, ASMT_MANAGER, VULN_MANAGER
	}

	public HashMap<EventType, String> methods = new HashMap();
	private List<AssessmentManager> assessmentManagers = new ArrayList<>();
	private List<VulnerabilityManager> vulnerabilityManagers = new ArrayList<>();
	private List<VerificationManager> verificationManagers = new ArrayList<>();
	private List<ApplicationInventory> inventoryManagers = new ArrayList<>();
	private String method;
	private EventType eventType;

	public Extensions(EventType type) {
		methods.put(EventType.INVENTORY, "search"); // TODO: Change this to be less generic
		methods.put(EventType.ASMT_MANAGER, "assessmentChange");
		methods.put(EventType.VULN_MANAGER, "vulnerabilityChange");
		methods.put(EventType.VER_MANAGER, "verificationChange");
		this.eventType = type;
		this.method = this.methods.get(type);
		
		try {
			this.loadExtensions();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	@SuppressWarnings("unchecked")
	public void execute(EntityManager em, Assessment assessment, AssessmentManager.Operation operation) {
		try {

			com.faction.elements.Assessment tmpAssessment = new com.faction.elements.Assessment();
			List<com.faction.elements.Vulnerability> tmpVulns = new ArrayList();
			for (Vulnerability v : assessment.getVulns()) {
				com.faction.elements.Vulnerability tVuln = new com.faction.elements.Vulnerability();
				copy(v, tVuln);
				tmpVulns.add(tVuln);
			}

			copy(assessment, tmpAssessment);
			com.faction.elements.User eng = new com.faction.elements.User();
			copy(assessment.getEngagement(), eng);
			com.faction.elements.User rem = new com.faction.elements.User();
			copy(assessment.getRemediation(), rem);
			List<com.faction.elements.User> assessors = new ArrayList<com.faction.elements.User>();
			if(assessment.getAssessor() != null) {
				for (User u : assessment.getAssessor()) {
					com.faction.elements.User assessor = new com.faction.elements.User();
					copy(u, assessor);
					assessors.add(assessor);
				}
			}
			List<CustomField> fields = assessment.getCustomFields();
			List<com.faction.elements.CustomField> tmpFields = new ArrayList<>();
			if(fields != null) {
				for (CustomField field : fields) {
					com.faction.elements.CustomField tmpField = new com.faction.elements.CustomField();
					com.faction.elements.CustomType tmpType = new com.faction.elements.CustomType();
					tmpType.setKey(field.getType().getKey());
					tmpType.setVariable(field.getType().getVariable());
					tmpField.setType(tmpType);
					tmpField.setValue(field.getValue());
					tmpFields.add(tmpField);

				}
			}
			tmpAssessment.setCustomFields(tmpFields);

			tmpAssessment.setEngagementContact(eng);
			tmpAssessment.setRemediationContact(rem);
			tmpAssessment.setAssessors(assessors);
			if(assessment.getCampaign() != null) {
				tmpAssessment.setCampaign(assessment.getCampaign().getName());
			}
			if(assessment.getType() != null) {
				tmpAssessment.setType(assessment.getType().getType());
			}
			
			Object[] updates = {tmpAssessment, tmpVulns};
			for(AssessmentManager mgr : this.assessmentManagers) {
				
				AssessmentManagerResult result = mgr.assessmentChange(
						(com.faction.elements.Assessment) updates[0], 
						(List<com.faction.elements.Vulnerability>) updates[1], 
						operation);
				if(result != null) {
					updates[0] = result.getAssessment();
					updates[1] = result.getVulnerabilities();
				}
			}

			if (updates != null && updates[0] != null) {
				copy(updates[0], assessment);
				List<com.faction.elements.CustomField> updatedFields = ((com.faction.elements.Assessment) updates[0])
						.getCustomFields();
				if (updatedFields != null && updatedFields.size() > 0) {
					for (com.faction.elements.CustomField updatedField : updatedFields) {
						for (CustomField originalField : fields) {
							if (updatedField.getType().getId() == originalField.getType().getId()) {
								originalField.setValue(updatedField.getValue());
							}
						}
					}
				}
				assessment.setCustomFields(fields);

			}

			if (updates != null && updates[1] != null
					&& ((List<com.faction.elements.Vulnerability>) updates[1]).size() > 0) {
				for (com.faction.elements.Vulnerability tVuln : ((List<com.faction.elements.Vulnerability>) updates[1])) {
					for (Vulnerability v : assessment.getVulns()) {
						if (tVuln.getId() == v.getId()) {
							copy(tVuln, v);
							break;
						}
					}
				}
			}
			if (em != null && updates != null) {
				em.persist(assessment);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Throwable ex) {
		}

	}

	public void execute(EntityManager em, Assessment assessment, Vulnerability vuln,
			VulnerabilityManager.Operation operation) {
		try {

			com.faction.elements.Assessment tmpAssessment = new com.faction.elements.Assessment();

			com.faction.elements.Vulnerability tVuln = new com.faction.elements.Vulnerability();
			copy(vuln, tVuln);

			copy(assessment, tmpAssessment);
			
			Object[] updates = {tmpAssessment, tVuln};
			for(VulnerabilityManager mgr : this.vulnerabilityManagers) {
				
				com.faction.elements.Vulnerability updatedVuln = mgr.vulnChange(
						(com.faction.elements.Assessment) updates[0], 
						(com.faction.elements.Vulnerability) updates[1],
						operation);
				if(updatedVuln != null) {
					updates[1] = updatedVuln;
				}
			}
			
			

			if (updates != null && updates[0] != null) {
				copy(updates[0], assessment);
			}
			if (updates != null && updates[1] != null) {
				com.faction.elements.Vulnerability uVuln = (com.faction.elements.Vulnerability) updates[1];
				for (Vulnerability v : assessment.getVulns()) {
					if (tVuln.getId() == v.getId()) {
						copy(uVuln, v);
						break;
					}
				}

			}
			if (em!= null && updates != null) {
				em.persist(assessment);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void execute(EntityManager em, Verification verification, VerificationManager.Operation operation) {
		try {

			com.faction.elements.Vulnerability clonedVuln = new com.faction.elements.Vulnerability();
			Vulnerability vulnerability = verification.getVerificationItems().get(0).getVulnerability();
			copy(vulnerability, clonedVuln);
			com.faction.elements.User clonedUser = new com.faction.elements.User();
			copy(verification.getAssessor(), clonedUser);
			com.faction.elements.Verification clonedVerification = new com.faction.elements.Verification();
			copy(verification, verification);
			
			for(VerificationManager mgr : this.verificationManagers) {
			
				com.faction.elements.Vulnerability updatedVuln = mgr.verificationChange(
						clonedUser, 
						clonedVuln, 
						clonedVerification,
						operation);
				if(updatedVuln != null) {
					clonedVuln = updatedVuln;
				}
			}
			if (clonedVuln != null) {
				copy(clonedVuln, vulnerability);
				em.persist(vulnerability);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

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
		if(source == null) {
			dest =null;
		}else {
			String[] nulls = getNullPropertyNames(source);
			BeanUtils.copyProperties(source, dest, nulls);
		}
	}


	private URLClassLoader createExtensionClassLoader(String modulePath) {
		File dir = new File(modulePath);
		URL[] urls = Arrays.stream(Optional.of(dir.listFiles()).orElse(new File[] {})).sorted().map(File::toURI)
				.map(t -> {
					try {
						return t.toURL();
					} catch (MalformedURLException e) {
						e.printStackTrace();
						return null;
					}
				}).filter(t -> t != null).toArray(URL[]::new);
		return new URLClassLoader(urls);
	}

	public void loadExtensions() throws MalformedURLException {
		URLClassLoader extensionLoader = createExtensionClassLoader("/opt/faction/modules");
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(extensionLoader);
			for (AssessmentManager asmtMgr : ServiceLoader.load(AssessmentManager.class, extensionLoader)) {
				if (asmtMgr != null) {
					assessmentManagers.add(asmtMgr);
				}
			}
		} catch(Throwable ex) {
			ex.printStackTrace();
		}finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
		
		try {
			Thread.currentThread().setContextClassLoader(extensionLoader);
			for (VulnerabilityManager vulnMgr : ServiceLoader.load(VulnerabilityManager.class, extensionLoader)) {
				if (vulnMgr != null) {
					vulnerabilityManagers.add(vulnMgr);
				}
			}
		} catch(Throwable ex) {
			ex.printStackTrace();
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
		
		try {
			Thread.currentThread().setContextClassLoader(extensionLoader);
			for (VerificationManager verMgr : ServiceLoader.load(VerificationManager.class, extensionLoader)) {
				if (verMgr != null) {
					verificationManagers.add(verMgr);
				}
			}
		} catch(Throwable ex) {
			ex.printStackTrace();
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
		
		try {
			Thread.currentThread().setContextClassLoader(extensionLoader);
			for (ApplicationInventory invMgr : ServiceLoader.load(ApplicationInventory.class, extensionLoader)) {
				if (invMgr != null) {
					inventoryManagers.add(invMgr);
				}
			}
		} catch(Throwable ex) {
			ex.printStackTrace();
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}


}
