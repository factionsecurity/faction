package com.fuse.authentication;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fuse.dao.User;

public class LDAPValidator {
	private String ldapURL;
	private String ldapBaseDN;
	private String ldapBindDN = null;
	private Boolean isInsecure=false;
	private String secureMethod;

	public LDAPValidator(String ldapURL, String ldapBaseDN, String ldapBindDN, String secureMethod, Boolean isInsecure) {
		this.ldapURL = ldapURL;
		this.ldapBaseDN = ldapBaseDN;
		this.ldapBindDN = ldapBindDN;
		this.isInsecure = isInsecure;
		this.secureMethod = secureMethod;
	}

	public boolean validateBindCredentials(String ldapBindPassword) {
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, this.ldapBindDN);
		env.put(Context.SECURITY_CREDENTIALS, ldapBindPassword);
		env.put(Context.SECURITY_PROTOCOL, this.secureMethod.toLowerCase());
		if(this.isInsecure) {
			env.put("java.naming.ldap.factory.socket", "com.fuse.authentication.InsecureSSLSocketFactory");
		}

		try {
			DirContext context = new InitialDirContext(env);
			return true;
		} catch (AuthenticationException e) {
			// Invalid credentials
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public Boolean validateCredentials(User user, String password) {

		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, user.getLdapUserDn());
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.SECURITY_PROTOCOL, this.secureMethod.toLowerCase());
		if(this.isInsecure) {
			env.put("java.naming.ldap.factory.socket", "com.fuse.authentication.InsecureSSLSocketFactory");
		}
		try {
			DirContext context = new InitialDirContext(env);
			return true;
		} catch (AuthenticationException e) {
			// Invalid credentials
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false; // Authentication failed
	}
	public User getUserInfo(String searchUsername, String ldapBindDNPassword, String ObjectClass) {
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapBindDN);
		env.put(Context.SECURITY_CREDENTIALS, ldapBindDNPassword);
		env.put(Context.SECURITY_PROTOCOL, this.secureMethod.toLowerCase());
		if(this.isInsecure) {
			env.put("java.naming.ldap.factory.socket", "com.fuse.authentication.InsecureSSLSocketFactory");
		}
		List<User> users = new ArrayList<User>();

		DirContext context = null;
		try {
			context = new InitialDirContext(env);

			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchUsername = sanitized(searchUsername);
			String filter = "(&(ObjectClass=user)(uid=" + searchUsername+ "))";
			if(ObjectClass == null || ObjectClass.equals("")) {
				
				filter = "(|(uid=" + searchUsername+ ")(sAMAccountName=" + searchUsername + "))";
			}
			NamingEnumeration<SearchResult> results = context.search(ldapBaseDN, filter, controls);

			while (results.hasMore()) {
				SearchResult searchResult = results.next();
				String userDN = searchResult.getNameInNamespace();
				Attributes attributes = searchResult.getAttributes();
				Attribute emailAttribute = attributes.get("mail");
				Attribute firstNameAttribute = attributes.get("givenName");
				Attribute lastNameAttribute = attributes.get("sn");
				Attribute uidAttribute = attributes.get("uid");
				Attribute sAMAccountAttribute = attributes.get("sAMAccountName");
				User user = new User();
				if (emailAttribute != null) {
					String email = (String) emailAttribute.get();
					user.setEmail(email);
				}
				if (firstNameAttribute != null) {
					String fname = (String) firstNameAttribute.get();
					user.setFname(fname);
				}
				if (lastNameAttribute != null) {
					String lname = (String) lastNameAttribute.get();
					user.setLname(lname);
				}
				if (uidAttribute != null) {
					String username = (String) uidAttribute.get();
					user.setUsername(username);
				}
				if (sAMAccountAttribute != null) { // this superceeds uid for username
					String username = (String) sAMAccountAttribute.get();
					user.setUsername(username);
				}
				if (userDN != null) {
					user.setLdapUserDn(userDN);
				}
				return user;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (context != null) {
				try {
					context.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
	private String sanitized(String term) {
		
		return term.replace("\"", "").replace("'","").replace("(", "").replace(")", "").replace("=", "");
	}

	public List<User> searchLdap(String term, String ldapBindDNPassword, String ObjectClass) {
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, ldapBindDN);
		env.put(Context.SECURITY_CREDENTIALS, ldapBindDNPassword);
		env.put(Context.SECURITY_PROTOCOL, this.secureMethod.toLowerCase());
		if(this.isInsecure) {
			env.put("java.naming.ldap.factory.socket", "com.fuse.authentication.InsecureSSLSocketFactory");
		}
		List<User> users = new ArrayList<User>();

		DirContext context = null;
		try {
			context = new InitialDirContext(env);

			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			term = sanitized(term);
			String filter = "(|(uid=" + term+ "*)(sAMAccountName=" + term + "*)(mail=" + term + "*)(givenName=" + term + "*)(sn="+ term + "*))";
			if(ObjectClass != null && !ObjectClass.trim().equals("")) {
				filter = "(&(ObjectClass=" + ObjectClass + ")(" + filter + "))";
			}
			NamingEnumeration<SearchResult> results = context.search(ldapBaseDN, filter, controls);

			while (results.hasMore()) {
				SearchResult searchResult = results.next();
				String userDN = searchResult.getNameInNamespace();
				Attributes attributes = searchResult.getAttributes();
				Attribute emailAttribute = attributes.get("mail");
				Attribute firstNameAttribute = attributes.get("givenName");
				Attribute lastNameAttribute = attributes.get("sn");
				Attribute uidAttribute = attributes.get("uid");
				Attribute sAMAccountAttribute = attributes.get("sAMAccountName");
				User user = new User();
				if (emailAttribute != null) {
					String email = (String) emailAttribute.get();
					user.setEmail(email);
				}
				if (firstNameAttribute != null) {
					String fname = (String) firstNameAttribute.get();
					user.setFname(fname);
				}
				if (lastNameAttribute != null) {
					String lname = (String) lastNameAttribute.get();
					user.setLname(lname);
				}
				if (uidAttribute != null) {
					String username = (String) uidAttribute.get();
					user.setUsername(username);
				}
				if (sAMAccountAttribute != null) { // this superceeds uid for username
					String username = (String) sAMAccountAttribute.get();
					user.setUsername(username);
				}
				if (userDN != null) {
					user.setLdapUserDn(userDN);
				}
				users.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (context != null) {
				try {
					context.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return users;
	}
	

}
