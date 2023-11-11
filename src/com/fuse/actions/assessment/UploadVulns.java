package com.fuse.actions.assessment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;

import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.User;


@Deprecated
public class UploadVulns extends FSActionSupport{
	
	private File file_data;
	private String contentType;
	private String filename;
	
	

	public String execute(){
		try {
			
			User u = this.getSessionUser();
			
			
			FileInputStream fileInputStream = new FileInputStream(file_data);
			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
			int sheets =  workbook.getNumberOfSheets();
			List images = workbook.getAllPictures();
			
			for(int i=0; i<sheets; i++){
				String name="";
				String severity="";
				String desc="";
				String rec="";
				List<ExploitStep>steps = new ArrayList<ExploitStep>();
			
				XSSFSheet worksheet = workbook.getSheetAt(i);
				XSSFDrawing art = worksheet.getDrawingPatriarch();
				List shapes = art.getShapes();
				Iterator<Row> rows = worksheet.iterator();
				while(rows.hasNext()){
					Row row = rows.next();
					String param = row.getCell(0).getStringCellValue().toLowerCase();
					if(param.startsWith("name"))
						name=row.getCell(1).getStringCellValue();
					else if(param.startsWith("desc")){
						RichTextString rtf = row.getCell(1).getRichStringCellValue();
						desc = rtftohtml(rtf.getString());
					}else if(param.startsWith("sev")){
						severity= row.getCell(1).getStringCellValue();
					}else if(param.startsWith("reco")){
						RichTextString rtf = row.getCell(1).getRichStringCellValue();
						rec = rtftohtml(rtf.getString());
					}else if(param.startsWith("finding")){
						
						RichTextString rtf = row.getCell(1).getRichStringCellValue();
						String step = rtftohtml(rtf.getString());
						
						ExploitStep ex = new ExploitStep();
						ex.setCreator(u);
						ex.setDescription(step);
						ex.setStepNum(steps.size()+1);
						steps.add(ex);

					}
				}
			}
					
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return SUCCESS;
	}
	private String rtftohtml(String rtf){
		JEditorPane p = new JEditorPane();
	    p.setContentType("text/rtf");
	    EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
	    try {
	    	
	    	InputStream stream = new ByteArrayInputStream(rtf.getBytes("UTF-8"));
	        kitRtf.read(stream, p.getDocument(), 0);
	        kitRtf = null;
	        EditorKit kitHtml = p.getEditorKitForContentType("text/html");
	        Writer writer = new StringWriter();
	        kitHtml.write(writer, p.getDocument(), 0, p.getDocument().getLength());
	        return writer.toString();
	    } catch (BadLocationException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
	}
		

	public File getFile_data() {
		return file_data;
	}

	public String getContentType() {
		return contentType;
	}

	public String getFilename() {
		return filename;
	}
	
	
	
	

}
