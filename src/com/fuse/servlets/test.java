package com.fuse.servlets;


import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.simple.JSONObject;

import com.fuse.extender.InventoryResult;
import com.fuse.extenderapi.Extensions;

import org.json.simple.JSONArray;

/**
 * Servlet implementation class test
 */
public class test extends HttpServlet {
	private static final long serialVersionUID = 1L;

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public test() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Extensions ex = new Extensions();
		if(ex.checkIfExtended(Extensions.INVENTORY)){
			Class[] classes = new Class[2];
			classes[0] = String.class;
			classes[1] = String.class;
			InventoryResult[] j = (InventoryResult[])  ex.execute(Extensions.INVENTORY, classes, "test","test");
			System.out.println(   j[0].getApplicationName());
		}else{
			System.out.println("failed");
		}
		
		
		
	}
	


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
