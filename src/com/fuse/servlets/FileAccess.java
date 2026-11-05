package com.fuse.servlets;

import javax.persistence.EntityManager;

import com.fuse.dao.Assessment;
import com.fuse.dao.Files;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.query.AssessmentQueries;

/**
 * Object-level authorization for stored attachments. The /service/* servlets
 * sit outside the Struts access-control interceptor, so each one has to decide
 * for itself; this applies the same rules used to open an assessment.
 */
public final class FileAccess {

	private FileAccess() {
	}

	/** May {@code user} read or delete this stored file? */
	public static boolean canAccess(EntityManager em, User user, Files file) {
		if (user == null || file == null || file.getEntityId() == null) {
			return false;
		}
		if (Files.VERIFICATION.equals(file.getType())) {
			return canAccessVerification(em, user, file.getEntityId());
		}
		// Assessment and engagement attachments both hang off an assessment id.
		return canAttachToAssessment(em, user, file.getEntityId());
	}

	/** May {@code user} add or remove attachments on this assessment? */
	public static boolean canAttachToAssessment(EntityManager em, User user, Long assessmentId) {
		if (user == null || assessmentId == null) {
			return false;
		}
		Assessment asmt = em.find(Assessment.class, assessmentId);
		return asmt != null && AssessmentQueries.canAccessAssessment(user, asmt);
	}

	/** May {@code user} work with attachments on this verification? */
	public static boolean canAccessVerification(EntityManager em, User user, Long verificationId) {
		if (user == null || verificationId == null) {
			return false;
		}
		Verification v = em.find(Verification.class, verificationId);
		if (v == null) {
			return false;
		}
		if (isUser(v.getAssessor(), user) || isUser(v.getAssignedRemediation(), user)) {
			return true;
		}
		return v.getAssessment() != null && AssessmentQueries.canAccessAssessment(user, v.getAssessment());
	}

	private static boolean isUser(User candidate, User user) {
		return candidate != null && candidate.getId() == user.getId();
	}
}
