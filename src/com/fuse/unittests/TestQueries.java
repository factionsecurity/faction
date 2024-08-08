package com.fuse.unittests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Test;

import com.fuse.dao.Assessment;
import com.fuse.dao.Comment;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.dao.query.VulnerabilityQueries;

public class TestQueries {

	@Test
	public void test() {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		User user = em.find(User.class, 2l);
		List<Assessment> asmt = AssessmentQueries.getAllAssessments(em, user, AssessmentQueries.OnlyNonCompleted);
		//System.out.println(asmt.size());
		em.close();
		
	}
	
	@Test
	public void testLowerCase() {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		Teams team = em.createNamedQuery("getTeam", Teams.class)
				.setParameter("team", "MEH").getSingleResult();
				
		System.out.println(team);
		em.close();
		
	}
	
	@Test
	public void testDefaultVulnsCase() {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		DefaultVulnerability dv = VulnerabilityQueries.getDefaultVulnerability(em,"[http]");
		System.out.println(dv.getName());
		
		em.close();
		
	}
	
	@Test
	public void testObjectCase() {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		User u = em.find(User.class, 2l);
		List<Verification> v = em.createQuery("from Verification v where v.assessor = :id")
				.setParameter("id", u)
				.getResultList();
		
		System.out.println(v.size());
		
		List<Assessment> a = em.createQuery("from Assessment v inner join v.assessor a where a is not null and a = :id   ")
				.setParameter("id", u)
				.getResultList();
		
		System.out.println(a.size());
		
		em.close();
		
	}
	
	@Test
	public void testVerObjectCase() {
		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
		Vulnerability v = em.find(Vulnerability.class, 102l);
		List<VerificationItem> items = em.createQuery("from VerificationItem vi where vi.vulnerability = :vuln")
				.setParameter("vuln", v)
				.getResultList();
		/*String itemstr = "[";
		for(VerificationItem vi : items) {
			itemstr += "" + vi.getId() + ",";
		}
		itemstr = itemstr.substring(0, itemstr.length() -1); // remove last comma
		itemstr += "]";
		String mongo = "{'verificationItems' : { '$in' : " + itemstr + "}}";
		System.out.println(mongo);
		List<Verification> ver = em.createNativeQuery(mongo, Verification.class).getResultList();*/
	
		List<Verification> ver = em
				.createQuery("from Verification v  inner join fetch  v.verificationItems vi"
						+ " where vi in ( :items )")
				.setParameter("items", items)
				.getResultList();;;
		System.out.println("Survived");
		System.out.println(ver.size());
		System.out.println(ver.get(0).getNotes());
		em.close();
		
	}
	
	@Test
	public void testRegex() {
		String html = "<table class=\"MsoTableGrid\" style=\"border-collapse:collapse; border:solid windowtext 1.0pt;\">\r\n" + 
				"	<tbody>\r\n" + 
				"		<tr>\r\n" + 
				"			<td style=\"border:solid windowtext 1.0pt; width:155.8pt; padding:0in 5.4pt 0in 5.4pt\"><span style=\"font-size:11pt\"><span style=\"line-height:normal\"><span style=\"font-family:Calibri,sans-serif\">Test</span></span></span></td>\r\n" + 
				"			<td style=\"border:solid windowtext 1.0pt; width:155.85pt; border-left:none; padding:0in 5.4pt 0in 5.4pt\"><span style=\"font-size:11pt\"><span style=\"line-height:normal\"><span style=\"font-family:Calibri,sans-serif\">testsadsfasdf</span></span></span></td>\r\n" + 
				"			<td style=\"border:solid windowtext 1.0pt; width:155.85pt; border-left:none; padding:0in 5.4pt 0in 5.4pt\"><span style=\"font-size:11pt\"><span style=\"line-height:normal\"><span style=\"font-family:Calibri,sans-serif\">teaset</span></span></span></td>\r\n" + 
				"		</tr>\r\n" + 
				"		<tr>\r\n" + 
				"			<td style=\"border:solid windowtext 1.0pt; width:155.8pt; border-top:none; padding:0in 5.4pt 0in 5.4pt\"><span style=\"font-size:11pt\"><span style=\"line-height:normal\"><span style=\"font-family:Calibri,sans-serif\">asdfasdfa</span></span></span></td>\r\n" + 
				"			<td style=\"border-bottom:solid windowtext 1.0pt; width:155.85pt; border-top:none; border-left:none; border-right:solid windowtext 1.0pt; padding:0in 5.4pt 0in 5.4pt\"><span style=\"font-size:11pt\"><span style=\"line-height:normal\"><span style=\"font-family:Calibri,sans-serif\">afafaf</span></span></span></td>\r\n" + 
				"			<td style=\"border-bottom:solid windowtext 1.0pt; width:155.85pt; border-top:none; border-left:none; border-right:solid windowtext 1.0pt; padding:0in 5.4pt 0in 5.4pt\"><span style=\"font-size:11pt\"><span style=\"line-height:normal\"><span style=\"font-family:Calibri,sans-serif\">fafafaf</span></span></span></td>\r\n" + 
				"		</tr>\r\n" + 
				"	</tbody>\r\n" + 
				"</table>";
		System.out.println(html.replaceAll("(style=\".*? )(windowtext)", "$1"));
	}
	

}
