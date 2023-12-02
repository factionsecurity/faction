package com.fuse.extenderapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.faction.extender.VerificationManager;
import com.faction.extender.VulnerabilityManager;

public class Extensions {

	public enum EventType {
		INVENTORY, VER_MANAGER, ASMT_MANAGER, VULN_MANAGER
	}

	public HashMap<EventType, String> methods = new HashMap();
	private List<Class> extendedClasses = new ArrayList<Class>();
	private String method;
	private EventType eventType;

	public Extensions(EventType type) {
		methods.put(EventType.INVENTORY, "search"); // TODO: Change this to be less generic
		methods.put(EventType.ASMT_MANAGER, "assessmentChange");
		methods.put(EventType.VULN_MANAGER, "vulnerabilityChange");
		methods.put(EventType.VER_MANAGER, "verificationChange");
		this.eventType = type;
		this.method = this.methods.get(type);
	}

	public boolean checkIfExtended() {
		this.extendedClasses = this.getExtendedClasses();
		return this.extendedClasses != null && this.extendedClasses.size() != 0;
	}

	public Object execute(Class[] classes, Object... arguments) {
		try {
			System.out.println(
					"There are " + (this.extendedClasses.size() - 1) + " other matched classes that did not run");
			Class classToLoad = this.extendedClasses.get(0);
			Method classMethod = classToLoad.getDeclaredMethod(methods.get(this.eventType), classes);
			Object instance = classToLoad.newInstance();
			return classMethod.invoke(instance, arguments);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

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
			for (User u : assessment.getAssessor()) {
				com.faction.elements.User assessor = new com.faction.elements.User();
				copy(u, assessor);
				assessors.add(assessor);
			}
			List<CustomField>fields = assessment.getCustomFields();
			List<com.faction.elements.CustomField> tmpFields = new ArrayList<>();
			for(CustomField field : fields) {
				com.faction.elements.CustomField tmpField = new com.faction.elements.CustomField();
				com.faction.elements.CustomType tmpType = new com.faction.elements.CustomType();
				tmpType.setKey(field.getType().getKey());
				tmpType.setVariable(field.getType().getVariable());
				tmpField.setType(tmpType);
				tmpField.setValue(field.getValue());
				tmpFields.add(tmpField);
				
			}
			tmpAssessment.setCustomFields(tmpFields);

			tmpAssessment.setEngagementContact(eng);
			tmpAssessment.setRemediationContact(rem);
			tmpAssessment.setAssessors(assessors);
			tmpAssessment.setCampaign(assessment.getCampaign().getName());
			tmpAssessment.setType(assessment.getType().getType());

			Object[] updates = (Object[]) this
					.execute(
							new Class[] { com.faction.elements.Assessment.class,
									List.class, AssessmentManager.Operation.class },
							tmpAssessment, tmpVulns, operation);
			if (updates != null && updates[0] != null) {
				copy(updates[0], assessment);
				List<com.faction.elements.CustomField> updatedFields = ((com.faction.elements.Assessment) updates[0]).getCustomFields();				
				if(updatedFields != null && updatedFields.size() > 0) {
					for(com.faction.elements.CustomField updatedField : updatedFields) {
						for(CustomField originalField : fields) {
							if(updatedField.getType().getId() == originalField.getType().getId()) {
								originalField.setValue(updatedField.getValue());
							}
						}
					}
				}
				assessment.setCustomFields(fields);

			}
			
			if (updates != null && updates[1] != null && ((List<com.faction.elements.Vulnerability>)updates[1]).size() >0) {
				for (com.faction.elements.Vulnerability tVuln : ((List<com.faction.elements.Vulnerability>) updates[1])) {
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

			Object[] updates = (Object[]) this
					.execute(
							new Class[] { com.faction.elements.Assessment.class,
									com.faction.elements.Vulnerability.class, VulnerabilityManager.Operation.class },
							tmpAssessment, tVuln, operation);

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
			if (updates != null) {
				em.persist(assessment);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void execute(EntityManager em, Verification verification, VerificationManager.Operation operation) {
		try {

			com.faction.elements.Vulnerability tVuln = new com.faction.elements.Vulnerability();
			copy(verification.getVerificationItems().get(0).getVulnerability(), tVuln);
			com.faction.elements.User user = new com.faction.elements.User();
			copy(verification.getAssessor(), user);

			Object[] updates = (Object[]) this.execute(
					new Class[] { com.faction.elements.User.class, com.faction.elements.Vulnerability.class,
							String.class, java.util.Date.class, java.util.Date.class,
							VerificationManager.Operation.class },
					user, tVuln, verification.getVerificationItems().get(0).getNotes(), verification.getStart(),
					verification.getEnd(), operation);

			if (updates != null && updates[0] != null) {
				copy(updates[0], verification.getAssessment());
			}
			if (updates != null && updates[1] != null) {
				com.faction.elements.Vulnerability uVuln = (com.faction.elements.Vulnerability) updates[1];
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

	private List<Class> getExtendedClasses() {
		List<Class> classes = new ArrayList<Class>();
		try {
			for (String file : this.getJarFiles("/opt/faction/modules/")) {
				Class extendedClass= getExtendedClassFromFile(file);
				if (extendedClass != null) {
					classes.add(extendedClass);
				}
			}
			return classes;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} catch (Throwable ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private Set<String> getJarFiles(String dir) throws IOException {
		try (Stream<Path> stream = Files.list(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName).map(Path::toString)
					.collect(Collectors.toSet());
		}
	}

	private Class getExtendedClassFromFile(String file) {
		JarFile jarFile;
		try {
			jarFile = new JarFile("/opt/faction/modules/" + file);
			Manifest m = jarFile.getManifest();
			String moduleString = (String) m.getMainAttributes().getValue("Import-Library");
			URL[] urls = { new URL("jar:file:/opt/faction/modules/" + file + "!/") };
			URLClassLoader child = new URLClassLoader(
			        urls,
			        this.getClass().getClassLoader()
			);
			Class classToLoad = Class.forName(moduleString, true, child);
			for(Method method : classToLoad.getMethods()) {
				if(method.getName().endsWith(this.method)) {
					return classToLoad;
				}
			}
			return null;
		} catch (ClassNotFoundException ex) {
			System.out.println("Cant load " + file);
		} catch (Exception ex) {
			System.out.println("Cant load " + file);
		} catch (Throwable ex) {
			System.out.println("Cant load " + file);
			ex.printStackTrace();
		}
		return null;
	}

}
