package com.fuse.extenderapi;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.junit.Test;

import com.fuse.extender.InventoryResult;

public class TestAIModule {
	
	public static void addPath(String s){
	    
		try {
			File f = new File(s);
		    URL u = f.toURL();
		    URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		    Class urlClass = URLClassLoader.class;
		    Method method;
			method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
			 method.setAccessible(true);
			 method.invoke(urlClassLoader, new Object[]{u});
			 
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	}

	@Test
	public void test() {
		addPath("C:/tmp/modules/Inventory.jar");
	
		Extensions ex = new Extensions(Extensions.EventType.INVENTORY);
		if(ex.checkIfExtended()){
			Class[] classes = new Class[2];
			classes[0] = String.class;
			classes[1] = String.class;
			InventoryResult[] j = (InventoryResult[])  ex.execute(classes, "test","test");
			System.out.println(   j[0].getApplicationName());
		}else{
			System.out.println("failed");
		}
			
		
		
	}

}
