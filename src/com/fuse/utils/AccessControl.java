package com.fuse.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.struts2.dispatcher.SessionMap;
import org.pac4j.core.profile.UserProfile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fuse.authentication.LDAPValidator;
import com.fuse.dao.Permissions;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;

public class AccessControl {

	public static enum AuthResult {
		FAILED_AUTH, LOCKEDOUT, NOACCOUNT, SUCCESS, INACTIVITY, REDIRECT_OAUTH, NOT_VALID_OAUTH_ACCOUNT, REDIRECT_SAML2, NOT_VALID_EMAIL, DUPLICATE_USERS, LOGOUT_PROFILE
	}

	public static boolean isNewInstance(EntityManager em) {
		List<User> tmp = (List<User>) em.createQuery("from User").getResultList();
		if (tmp.size() == 0) {
			return true;
		} else {
			for (User u : tmp) {
				if (u.getPermissions().isAdmin()) {
					return false;
				}
			}
			return true;
		}

	}

	public static boolean isAuthenticated(SessionMap jsession) {
		

		if (jsession == null)
			return false;
		User user = (User) jsession.get("user");
		if (user != null) {
			return true;
		} else
			return false;
	}
	

	public static User getAuthenticatedUser(SessionMap jsession) {
		User user = (User) jsession.get("user");
		return user;
	}

	public static AuthResult Authenticate(String user, String pass, HttpServletRequest request, EntityManager em, List<UserProfile> profiles) {
		
		HttpSession jsession = request.getSession(true);
		BCryptPasswordEncoder encoder =new BCryptPasswordEncoder();
		
		//Check oAuth Stuff
	
		if( profiles != null && profiles.size() > 0) {
			for(UserProfile profile : profiles) {
				String email = (String) profile.getAttribute("email");
				if (email == null) {
				    email = (String) profile.getAttribute("EmailAddress");
				}
				if (email == null) {
				    Object attribute = profile.getAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");
				    if(attribute != null) {
				    	if (attribute instanceof String) {
				            email = (String) attribute;
				        } else if (attribute instanceof List) {
				            List<?> list = (List<?>) attribute;
				            if (!list.isEmpty() && list.get(0) instanceof String) {
				                email = (String) list.get(0);
				            }
				        }
				    	
				    }
				}
				
				if(email != null) {
					String query = String.format("{'email': {$regex: '^%s$', $options: 'i'}}",FSUtils.sanitizeMongo(email));
					List<User> users = em.createNativeQuery(query, User.class).getResultList();
					User tmp = null;
					if(users.size() == 0) {
						return AuthResult.NOT_VALID_OAUTH_ACCOUNT;
					}
					else if(users.size() == 1) {
						tmp = users.get(0);
					}else {
						return AuthResult.DUPLICATE_USERS;
					}
					
					if(tmp != null) {
						tmp.setLastLogin(tmp.getLoginTime());
						tmp.setLoginTime(new Date());
						tmp.setFailedAuth(0);
						em.persist(tmp);

						jsession.invalidate();
						jsession = request.getSession(true);
						jsession.setAttribute("user", tmp);
						return AuthResult.SUCCESS;
					}
				}else {
					return AuthResult.NOT_VALID_EMAIL;
				}
			}
			//lets invalidate the session that has existing profiles since 
			// none matched and return an error that oAuth failed. 
			jsession.invalidate();
			jsession = request.getSession(true);
			return AuthResult.NOT_VALID_OAUTH_ACCOUNT;
		}
		
		// Native, LDAP, and OAUTH Redirect processing
		user = user.trim().toLowerCase();
		pass = pass==null? "" : pass.trim();
		SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);

		User tmp = (User) em.createQuery("from User where username = :user").setParameter("user", user).getResultList()
				.stream().findFirst().orElse(null);
		if (tmp == null)
			return AuthResult.NOACCOUNT;
		if (tmp.getFailedAuth() != null && tmp.getFailedAuth() > 5) {
			tmp.setInActive(true);
			em.persist(tmp);
			return AuthResult.LOCKEDOUT;
		}
		
		// redirect to OAUTH
		if(tmp.getAuthMethod().equals("OAUTH2.0")) {
			return AuthResult.REDIRECT_OAUTH;
		// redirect SAML
		}else if(tmp.getAuthMethod().equals("SAML2")) {
			return AuthResult.REDIRECT_SAML2;
		// Validate LDAP
		}else if (tmp.getAuthMethod().equals("LDAP")) {
			LDAPValidator ldapValidator = new LDAPValidator(ems.getLdapURL(), ems.getLdapBaseDn(),
					ems.getLdapBindDn(), ems.getLdapSecurity(), ems.getLdapInsecureSSL());
			if (!ldapValidator.validateCredentials(tmp, pass)) {
				tmp.setFailedAuth(tmp.getFailedAuth() == null ? 1 : tmp.getFailedAuth() + 1);
				em.persist(tmp);
				return AuthResult.FAILED_AUTH;
			}

		// Validate Native
		} else if (!encoder.matches(pass, tmp.getPasshash())) {
			tmp.setFailedAuth(tmp.getFailedAuth() == null ? 1 : tmp.getFailedAuth() + 1);
			em.persist(tmp);
			return AuthResult.FAILED_AUTH;
		}

		if (ems != null && ems.getInactiveDays() != null && ems.getInactiveDays().intValue() > 30) {
			if (tmp.getLastLogin() != null) {
				Date ll = tmp.getLastLogin();
				// first check if the user has ever logged in.
				if (ll != null && ll.getTime() != 0l) {
					Calendar backdate = Calendar.getInstance();
					backdate.add(Calendar.DATE, -ems.getInactiveDays().intValue());
					if (ll.getTime() < backdate.getTimeInMillis()) {
						tmp.setInActive(true);
						tmp.setFailedAuth(10);
						em.persist(tmp);
						return AuthResult.INACTIVITY;
					}
				}
			}
		}
		tmp.setLastLogin(tmp.getLoginTime());
		tmp.setLoginTime(new Date());
		tmp.setFailedAuth(0);
		em.persist(tmp);

		jsession.setAttribute("user", tmp);

		return AuthResult.SUCCESS;

	}

	public static void createAdmin(String username, String pass, String first, String last, String email,
			EntityManager em) {
		username = username.toLowerCase().trim();
		pass = pass.trim();
		User u = new User();
		Permissions p = new Permissions();
		p.setAdmin(true);
		p.setAssessor(true);
		p.setEngagement(true);
		p.setManager(true);
		p.setRemediation(false);
		p.setExecutive(false);
		u.setEmail(email.trim());
		u.setFname(first.trim());
		u.setLname(last.trim());
		u.setInActive(false);
		u.setPasshash(HashPass(pass));
		u.setPermissions(p);
		u.setUsername(username);
		em.persist(u);

	}

	public static String HashPass(String pass) {
		BCryptPasswordEncoder encoder =new BCryptPasswordEncoder();
		return encoder.encode(pass);
	}

	/**
	 * This function checks that the password conforms to the password strength
	 * rules. It returns errors if the password is weak.
	 * 
	 * @param password
	 * @param confirm
	 * @return Errors
	 */
	public static String checkPassword(String password, String confirm) {
		Pattern upper = Pattern.compile("[A-Z]");
		Pattern number = Pattern.compile("[0-9]");
		Pattern lower = Pattern.compile("[a-z]");

		Matcher upMatch = upper.matcher(password);
		Matcher numMatch = number.matcher(password);
		Matcher lowMatch = lower.matcher(password);

		if (password.equals(""))
			return "Invalid Password";
		else if (!password.equals(confirm))
			return "Passwords Don't Match";
		else if (password.length() < 8)
			return "Password Must be 8 Characters in Length";
		else if(!upMatch.find())
			return "Password Must Have One Upper Case Character";
		else if (!lowMatch.find())
			return "Password Must Have One Lower Case Character";
		else if (!numMatch.find())
			return "Password Must Have One Number";

		return "";

	}
	

}
