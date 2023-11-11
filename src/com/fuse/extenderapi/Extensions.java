package com.fuse.extenderapi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fuse.dao.Assessment;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.extender.AssessmentManager;
import com.fuse.extender.VerificationManager;
import com.fuse.extender.VulnerabilityManager;

public class Extensions {

	public static String INVENTORY = "Inventory";
	public static String LOGGING = "Logging";
	public static String VER_MANAGER = "VerificationManager";
	public static String ASMT_MANAGER = "AssessmentManager";
	public static String VULN_MANAGER = "VulnerabilityManager";

	public HashMap<String, String> methods = new HashMap();

	public Extensions() {
		methods.put(INVENTORY, "search");
		methods.put(ASMT_MANAGER, "assessmentChange");
		methods.put(VULN_MANAGER, "vulnerabilityChange");
		methods.put(VER_MANAGER, "verificationChange");

	}

	public static boolean checkIfExtended(String Module) {
		try {
			// Check if the Extender Modules Exits
			Class.forName("com.fuse.elements.Assessment");
			// Check if the Module Exists
			Class.forName("com.fuse.extender.module." + Module);
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}

	}

	public Object execute(String Module, Class[] classes, Object... arguments) {
		try {
			Object ai = Class.forName("com.fuse.extender.module." + Module).newInstance();
			Method m = ai.getClass().getMethod(methods.get(Module), classes);
			return m.invoke(ai, arguments);

		} catch (ClassNotFoundException e) {

			// This is OK... it means no one has extended this feature
			// e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void execute(EntityManager em, Assessment assessment, AssessmentManager.Operation operation) {
		try {

			com.fuse.elements.Assessment tmpAssessment = new com.fuse.elements.Assessment();
			List<com.fuse.elements.Vulnerability> tmpVulns = new ArrayList();
			for (Vulnerability v : assessment.getVulns()) {
				com.fuse.elements.Vulnerability tVuln = new com.fuse.elements.Vulnerability();
				copy(v, tVuln);
				tmpVulns.add(tVuln);
			}

			copy(assessment, tmpAssessment);
			com.fuse.elements.User eng = new com.fuse.elements.User();
			copy(assessment.getEngagement(), eng);
			com.fuse.elements.User rem = new com.fuse.elements.User();
			copy(assessment.getRemediation(), rem);
			List<com.fuse.elements.User> assessors = new ArrayList<com.fuse.elements.User>();
			for (User u : assessment.getAssessor()) {
				com.fuse.elements.User assessor = new com.fuse.elements.User();
				copy(u, assessor);
				assessors.add(assessor);
			}

			tmpAssessment.setEngagementContact(eng);
			tmpAssessment.setRemediationContact(rem);
			tmpAssessment.setAssessors(assessors);
			tmpAssessment.setCampaign(assessment.getCampaign().getName());
			tmpAssessment.setType(assessment.getType().getType());

			Object[] updates = (Object[]) this.execute(Extensions.ASMT_MANAGER,
					new Class[] { com.fuse.elements.Assessment.class, List.class, AssessmentManager.Operation.class },
					tmpAssessment, tmpVulns, operation);

			if (updates != null && updates[0] != null) {
				copy(updates[0], assessment);

			}
			if (updates != null && updates[1] != null) {
				for (com.fuse.elements.Vulnerability tVuln : ((List<com.fuse.elements.Vulnerability>) updates[1])) {
					for (Vulnerability v : assessment.getVulns()) {
						if (tVuln.getId() == v.getId()) {
							copy(tVuln, v);
							break;
						}
					}
				}
			}
			if (updates != null) {
				em.persist(assessment);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void execute(EntityManager em, Assessment assessment, Vulnerability vuln,
			VulnerabilityManager.Operation operation) {
		try {

			com.fuse.elements.Assessment tmpAssessment = new com.fuse.elements.Assessment();

			com.fuse.elements.Vulnerability tVuln = new com.fuse.elements.Vulnerability();
			copy(vuln, tVuln);

			copy(assessment, tmpAssessment);

			Object[] updates = (Object[]) this.execute(
					Extensions.VULN_MANAGER, new Class[] { com.fuse.elements.Assessment.class,
							com.fuse.elements.Vulnerability.class, VulnerabilityManager.Operation.class },
					tmpAssessment, tVuln, operation);

			if (updates != null && updates[0] != null) {
				copy(updates[0], assessment);
			}
			if (updates != null && updates[1] != null) {
				com.fuse.elements.Vulnerability uVuln = (com.fuse.elements.Vulnerability) updates[1];
				for (Vulnerability v : assessment.getVulns()) {
					if (tVuln.getId() == v.getId()) {
						copy(uVuln, v);
						break;
					}
				}

			}
			if (updates != null) {
				em.persist(assessment);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void execute(EntityManager em, Verification verification, VerificationManager.Operation operation) {
		try {

			com.fuse.elements.Vulnerability tVuln = new com.fuse.elements.Vulnerability();
			copy(verification.getVerificationItems().get(0).getVulnerability(), tVuln);
			com.fuse.elements.User user = new com.fuse.elements.User();
			copy(verification.getAssessor(), user);

			Object[] updates = (Object[]) this.execute(Extensions.VER_MANAGER,
					new Class[] { com.fuse.elements.User.class, com.fuse.elements.Vulnerability.class, String.class,
							java.util.Date.class, java.util.Date.class, VerificationManager.Operation.class },
					user, tVuln, verification.getVerificationItems().get(0).getNotes(), verification.getStart(),
					verification.getEnd(), operation);

			if (updates != null && updates[0] != null) {
				copy(updates[0], verification.getAssessment());
			}
			if (updates != null && updates[1] != null) {
				com.fuse.elements.Vulnerability uVuln = (com.fuse.elements.Vulnerability) updates[1];
				for (Vulnerability v : verification.getAssessment().getVulns()) {
					if (tVuln.getId() == v.getId()) {
						copy(uVuln, v);
						break;
					}
				}

			}
			if (updates != null) {
				em.persist(verification);
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
		String[] nulls = getNullPropertyNames(source);
		BeanUtils.copyProperties(source, dest, nulls);
	}

}
