package com.fuse.extenderapi;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ThisFieldRefForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fuse.dao.Assessment;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.extender.ApplicationInventory;
import com.fuse.extender.AssessmentManager;
import com.fuse.extender.VerificationManager;
import com.fuse.extender.VulnerabilityManager;

public class Extensions {

	
	public enum EventType {
		INVENTORY, VER_MANAGER, ASMT_MANAGER, VULN_MANAGER
	}

	public HashMap<EventType, String> methods = new HashMap();
	private SortedSet<Class> extendedClasses = new TreeSet<Class>();
	private String method;
	private EventType eventType;
	
	

	public Extensions(EventType type) {
		methods.put(EventType.INVENTORY, "search"); //TODO: Change this to be less generic
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
			System.out.println("There are " + (this.extendedClasses.size() -1) +" other matched classes that did not run");
			Object ai = this.extendedClasses.first().newInstance();
			Method m = ai.getClass().getMethod(methods.get(this.eventType), classes);
			return m.invoke(ai, arguments);
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

			Object[] updates = (Object[]) this.execute(
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
							new Class[] { com.fuse.elements.Assessment.class,
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

			Object[] updates = (Object[]) this.execute(
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
	
	private SortedSet<Class> getExtendedClasses() {
		SortedSet<Class> classes = new TreeSet<Class>();
		try {
			for( String file : this.getJarFiles("/opt/faction/modules/")) {
				SortedSet<Class> matchedMethods = getExtendedClassesFromFile(file);
				if(matchedMethods != null && matchedMethods.size()>0) {
					classes.addAll(matchedMethods);
				}
			}
			return classes;
		}catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}catch(Throwable ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private Set<String> getJarFiles(String dir) throws IOException {
    try (Stream<Path> stream = Files.list(Paths.get(dir))) {
	        return stream
	          .filter(file -> !Files.isDirectory(file))
	          .map(Path::getFileName)
	          .map(Path::toString)
	          .collect(Collectors.toSet());
	    }
	}
	
	private SortedSet<Class> getExtendedClassesFromFile(String file){
		JarFile jarFile;
		try {
			jarFile = new JarFile("/opt/faction/modules/"+file);
			Enumeration<JarEntry> e = jarFile.entries();

			URL[] urls = { new URL("jar:file:/opt/faction/modules/" + file + "!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls);
			SortedSet<Class> classes = new TreeSet<Class>();

			while (e.hasMoreElements()) {
				JarEntry je = e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				// -6 because of .class
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				try {
					Class c = cl.loadClass(className);
					if(		ApplicationInventory.class.isInstance(c.getClass()) || 
							AssessmentManager.class.isInstance(c.getClass()) ||
							VerificationManager.class.isInstance(c.getClass()) ||
							VulnerabilityManager.class.isInstance(c.getClass())
								
							) {
						Method [] methods = c.getMethods();
						for(Method m : methods) {
							System.out.println(m.getName());
							if(m.getName().endsWith("."+this.method)) {
								classes.add(c);
								break;
							}
						}
					}
					
				} catch (ClassNotFoundException ex) {
					System.out.println("Cant load " + className);
				}catch(Exception ex) {
					System.out.println("Cant load " + className);
				}catch(Throwable ex) {
					System.out.println("Cant load " + className);
				}
			}
			return classes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
