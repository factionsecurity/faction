package com.fuse.api.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import com.fuse.dao.APIKeys;
import com.fuse.dao.User;
import com.fuse.utils.FSUtils;

public class Support {
	
	public static  String SUCCESS = "[{ \"result\" : \"SUCCESS\"}]";
	public static  String ERROR = "[{ \"result\" : \"ERROR\", \"message\":\"%s\"}]";
	public static  String SUCCESSMSG = "[{ \"result\" : \"SUCCESS\", %s}]";

	public static User getUser(EntityManager em, String apiKey){
		APIKeys keys = (APIKeys)em.createQuery("from APIKeys where key = :key").setParameter("key", apiKey).getResultList().stream().findFirst().orElse(null);
		if ( keys != null){
			return keys.getUser();
		}else{
			return null;
		}
		
	}
	
	///uses Reflection to create a JSON object out of a class
	public static JSONObject dao2JSON(Object obj, Class cls){
		 JSONObject json = new JSONObject();
		 Method[] declaredMethods = cls.getDeclaredMethods();
		 for (Method dmethod : declaredMethods) {
			 //System.out.println(dmethod.getName());
			 //System.out.println( dmethod.getReturnType().toString());
			 
			 if(dmethod.getName().startsWith("get") && 
					 (
							 dmethod.getReturnType().equals(Integer.TYPE) ||
							 dmethod.getReturnType().equals(Long.TYPE) ||
							 dmethod.getReturnType().equals(Integer.class) ||
							 dmethod.getReturnType().equals(Long.class) ||
							 dmethod.getReturnType().equals(String.class) ||
							 dmethod.getReturnType().equals(Date.class)
							 )){
				 try {
					 Object o = dmethod.invoke(obj,  null);
					 if(o != null){
						 String name = dmethod.getName().replace("get", "");
						 if(dmethod.getReturnType().equals(Date.class))
							 json.put(name, ""+ ((Date)o).getTime());	
						 else
							 json.put(name, o);	 
					 }
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
			 
		 }
		 return json;

	}
	
	public static Response error(String Message) {
		return Response.status(400).entity(String.format(Support.ERROR,Message)).build();
	}
	public static Response autherror() {
		return Response.status(401).entity(String.format(Support.ERROR,"Not Authorized.")).build();
	}
	public static Response success() {
		return Response.status(200).entity(Support.SUCCESS).build();
	}
	public static Response success(String Message) {
		return Response.status(200).entity(String.format(Support.SUCCESSMSG,Message)).build();
	}
	public static String getTier(){
		return FSUtils.getEnv("FACTION_TIER");
	}
}
