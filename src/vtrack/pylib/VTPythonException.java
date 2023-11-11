package vtrack.pylib;

import java.io.PrintWriter;
import java.io.StringWriter;

public class VTPythonException extends Exception{
	private String Message;
	
	public VTPythonException(Exception Ex){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Ex.printStackTrace(pw);
		String trace = sw.toString().split("\r\n\r\n\t")[0];
		this.Message = trace;
	}
	
	public String getMessage(){
		return this.Message;
	}
	
	public VTArray getArray(){
		VTArray a = new VTArray();
		VTKVPair p = new VTKVPair();
		p.put("Error", this.Message);
		a.add(p);
		return a;
	}
	

}
