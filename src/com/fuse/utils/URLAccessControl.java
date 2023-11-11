package com.fuse.utils;

public class URLAccessControl {
	
	/**
	 * URL, Group (Assessment/Remediation/Scheduling/Admin), AccessLevel(All,Team,User)
	 */
	public static enum Type {Assessment,Remediation,Scheduling,Admin};
	public static enum AccessLevel{All,Team,User};
	
	public class LinkControl{
		private String URL;
		private Type type;
		
		
	}
}
