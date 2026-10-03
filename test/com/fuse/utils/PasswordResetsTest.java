package com.fuse.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fuse.api.MongoTestBase;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PasswordReset;
import com.fuse.dao.User;

/**
 * Password reset / invitation tokens: bound to the target user, expiring, and
 * one-at-a-time per user. Issued by sendReset, the admin "add user" page and the
 * users API; redeemed by Register.
 */
public class PasswordResetsTest extends MongoTestBase {

	private static EntityManagerFactory emf;
	private static String tag;
	private static User target, admin;

	@BeforeClass
	public static void seed() {
		emf = HibHelper.getInstance().getEMF();
		org.junit.Assume.assumeNotNull("EntityManagerFactory unavailable - skipping integration test", emf);
		tag = UUID.randomUUID().toString().substring(0, 8);
		EntityManager em = emf.createEntityManager();
		try {
			target = user(em, "target-" + tag);
			admin = user(em, "admin-" + tag);
		} finally {
			em.close();
		}
	}

	@AfterClass
	public static void cleanup() {
		if (emf == null)
			return;
		EntityManager em = emf.createEntityManager();
		try {
			for (PasswordReset r : (List<PasswordReset>) em.createQuery("from PasswordReset").getResultList()) {
				if (r.getUser() != null && (r.getUser().getId() == target.getId() || r.getUser().getId() == admin.getId()))
					remove(em, r);
			}
			remove(em, em.find(User.class, target.getId()));
			remove(em, em.find(User.class, admin.getId()));
		} finally {
			em.close();
		}
	}

	@Test
	public void issuedTokenIsBoundToTheTargetUserNotTheCaller() {
		EntityManager em = emf.createEntityManager();
		try {
			String key = PasswordResets.issue(em, target, PasswordResets.INVITE_TTL_MILLIS);
			PasswordReset reset = PasswordResets.redeem(em, key);
			assertNotNull(reset);
			assertEquals(target.getId(), reset.getUser().getId());
			assertNotEquals(admin.getId(), reset.getUser().getId());
			assertNotNull("tokens carry an expiry", reset.getExpires());
			assertTrue(reset.getExpires().after(new Date()));
		} finally {
			em.close();
		}
	}

	@Test
	public void issuingANewTokenInvalidatesTheOlderOne() {
		EntityManager em = emf.createEntityManager();
		try {
			String first = PasswordResets.issue(em, target, PasswordResets.RESET_TTL_MILLIS);
			String second = PasswordResets.issue(em, target, PasswordResets.RESET_TTL_MILLIS);
			assertNull("superseded token must stop working", PasswordResets.redeem(em, first));
			assertNotNull(PasswordResets.redeem(em, second));
		} finally {
			em.close();
		}
	}

	@Test
	public void expiredTokenIsRejectedAndRemoved() {
		EntityManager em = emf.createEntityManager();
		try {
			String key = PasswordResets.issue(em, target, -1000L); // already expired
			assertNull(PasswordResets.redeem(em, key));
			assertTrue("expired token is cleaned up",
					em.createQuery("from PasswordReset where key = :k").setParameter("k", key).getResultList().isEmpty());
		} finally {
			em.close();
		}
	}

	@Test
	public void legacyTokenWithoutExpiryIsTreatedAsExpiringAnHourAfterCreation() {
		EntityManager em = emf.createEntityManager();
		try {
			PasswordReset legacy = new PasswordReset();
			legacy.setKey("legacy-" + tag);
			legacy.setUser(target);
			legacy.setCreated(new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000L)); // two hours old
			persist(em, legacy);
			assertNull(PasswordResets.redeem(em, "legacy-" + tag));
		} finally {
			em.close();
		}
	}

	@Test
	public void unknownTokenIsRejected() {
		EntityManager em = emf.createEntityManager();
		try {
			assertNull(PasswordResets.redeem(em, "nope-" + tag));
			assertNull(PasswordResets.redeem(em, null));
		} finally {
			em.close();
		}
	}

	private static User user(EntityManager em, String name) {
		User u = new User();
		u.setUsername(name);
		u.setFname(name);
		u.setLname("t");
		u.setEmail(name + "@example.test");
		persist(em, u);
		return u;
	}

	private static void persist(EntityManager em, Object entity) {
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(entity);
		HibHelper.getInstance().commit();
	}

	private static void remove(EntityManager em, Object entity) {
		if (entity == null)
			return;
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.remove(em.contains(entity) ? entity : em.merge(entity));
		HibHelper.getInstance().commit();
	}
}
