package com.fuse.utils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import com.fuse.dao.HibHelper;
import com.fuse.dao.PasswordReset;
import com.fuse.dao.User;

/**
 * Password reset and invitation tokens.
 *
 * A token is bound to the user whose password it will set, expires, and
 * supersedes any earlier token for that user. Callers issue a token after the
 * target user is persisted and put the returned key in the emailed link;
 * {@link com.fuse.actions.Register} redeems it.
 */
public final class PasswordResets {

	/** Self-service password resets: one hour. */
	public static final long RESET_TTL_MILLIS = 60L * 60 * 1000;
	/** Invitations for newly created accounts: three days. */
	public static final long INVITE_TTL_MILLIS = 72L * 60 * 60 * 1000;

	private PasswordResets() {
	}

	/**
	 * Issues a fresh token for {@code target}, invalidating any outstanding ones.
	 * Manages its own transaction, so call it after the target user is committed.
	 */
	public static String issue(EntityManager em, User target, long ttlMillis) {
		if (target == null || target.getId() == 0L) {
			throw new IllegalArgumentException("reset target must be a persisted user");
		}
		String key = UUID.randomUUID().toString();
		Date now = new Date();
		PasswordReset reset = new PasswordReset();
		reset.setKey(key);
		reset.setUser(target);
		reset.setCreated(now);
		reset.setExpires(new Date(now.getTime() + ttlMillis));

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		for (PasswordReset old : outstandingFor(em, target)) {
			em.remove(old);
		}
		em.persist(reset);
		HibHelper.getInstance().commit();
		return key;
	}

	/**
	 * Returns the live token for {@code key}, or null when it is unknown or
	 * expired. Expired tokens are deleted on the way out.
	 */
	public static PasswordReset redeem(EntityManager em, String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}
		PasswordReset reset = (PasswordReset) em.createQuery("from PasswordReset where key = :key")
				.setParameter("key", key).getResultList().stream().findFirst().orElse(null);
		if (reset == null) {
			return null;
		}
		if (isExpired(reset)) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(reset);
			HibHelper.getInstance().commit();
			return null;
		}
		return reset;
	}

	static boolean isExpired(PasswordReset reset) {
		Date expires = reset.getExpires();
		if (expires == null) {
			// Pre-1.8.15 row: it never carried an expiry, so give it the reset window.
			if (reset.getCreated() == null) {
				return true;
			}
			expires = new Date(reset.getCreated().getTime() + RESET_TTL_MILLIS);
		}
		return !expires.after(new Date());
	}

	@SuppressWarnings("unchecked")
	private static List<PasswordReset> outstandingFor(EntityManager em, User target) {
		return em.createQuery("from PasswordReset where user = :user").setParameter("user", target).getResultList();
	}
}
