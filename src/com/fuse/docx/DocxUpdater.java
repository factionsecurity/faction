package com.fuse.docx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;

import com.fuse.utils.FSUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class DocxUpdater {
	
	private  final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";
	private  long chunk = 0;
	
	/*
	 * insertDocx  will accept another document template as a byte area and insert it inot the keyword location in the main
	 * 		Document.. 
	 * 		main = Main docx document
	 *  	bytes = Byte array of a docx template
	 *   	keyord = location in the base document to insert the byte array template
	 */
	private  void insertDocx(MainDocumentPart main, byte[] bytes, String keyword) throws Exception {
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/part" + (chunk++) + ".docx"));
        afiPart.setContentType(new ContentType(CONTENT_TYPE));
        afiPart.setBinaryData(bytes);
        Relationship altChunkRel = main.addTargetPart(afiPart, AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS);

        CTAltChunk chunk = Context.getWmlObjectFactory().createCTAltChunk();
        chunk.setId(altChunkRel.getId());
        //main.addAltChunk(AltChunkType.OfficeWordMacroEnabled, bytes);
        int index = replaceTemplate(main, keyword);
        main.getContent().add(index,chunk);
        //main.addObject(chunk);
	}
	
	/*
	 * insertHTML will insert 
	 */
	public  void insertHTML(MainDocumentPart main, byte[] bytes, String keyword) throws Exception {
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/part" + (chunk++) + ".html"));
        afiPart.setContentType(new ContentType("text/html"));
        afiPart.setBinaryData(bytes);
        Relationship altChunkRel = main.addTargetPart(afiPart, AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS);

        CTAltChunk chunk = Context.getWmlObjectFactory().createCTAltChunk();
        chunk.setId(altChunkRel.getId());
        //main.addAltChunk(AltChunkType.OfficeWordMacroEnabled, bytes);
        int index = replaceTemplate(main, keyword);
        main.getContent().add(index,chunk);
        //main.addObject(chunk);
	}
	
	public  int replaceTemplate(final MainDocumentPart mainPart, String keyword){
		 Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");
	        

	        // look for all P elements in the specified object
	        final List<P> paragraphs = Lists.newArrayList();
	        new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
	            @Override
	            public List<Object> apply(Object o) {
	                if (o instanceof P) {
	                    paragraphs.add((P) o);
	                }

	                return null;
	            }
	        });

	        // run through all found paragraphs to located identifiers
	        for (final P paragraph : paragraphs) {
	            // check if this is one of our identifiers
	            final StringWriter paragraphText = new StringWriter();
	            try {
	                TextUtils.extractText(paragraph, paragraphText);
	            } catch (Exception ex) {
	               
	            }

	            final String identifier = paragraphText.toString();
	            if (identifier != null && identifier.contains(keyword)) {
	            	int index = mainPart.getContent().indexOf(paragraph);
	            	mainPart.getContent().remove(index);
	            	return index;
	            
	            }
	            
	        }
	        return -1;
	}
	
	public  List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
		List<Object> result = new ArrayList<Object>();
		if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();
 
		if (obj.getClass().equals(toSearch))
			result.add(obj);
		else if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElementFromObject(child, toSearch));
			}
 
		}
		return result;
	}
	
	/*
	 * Change color of a table cell based on the variable names
	 */
	public  int changeColorOfCell(Tr row,String variable, String color){
		 List<Object> paragraphs = getAllElementFromObject(row, P.class);
		 for(Object para : paragraphs){
			 if(matchText((P)para, variable)){
				 Tc cell = ((Tc)((P)para).getParent());
				 if(cell.getTcPr().getShd() != null){
					 cell.getTcPr().getShd().setFill(color);
				 }else{
					 CTShd shader = new CTShd();
					 shader.setColor("auto");
					 shader.setFill(color);
					 
					 cell.getTcPr().setShd(shader);
				 }
			 }	 
		 }
		 return -1;
	 }
	/*
	 * Utiltity function to file elements in the docx file
	 */
	 public  int indexOfRow(Tbl table, List<Object> paragraphs, String variable){
		 for(Object para : paragraphs){
			 if(matchText((P)para, variable)){
				 Tc cell = ((Tc)((P)para).getParent());
				 JAXBElement jrow = ((JAXBElement)cell.getParent());
				 List<Object> rows = table.getContent();
				 for(Object oRow : rows){
					 Tr row = (Tr)oRow;
					 if(row.getContent().indexOf(jrow) >= 0){
						 return table.getContent().indexOf(row);
					 }
				 }
				 
			 }	 
		 }
		 return -1;
	 }
	 
	 /*
	  * Check if there is a match in the text
	  */
	 public  boolean matchText(P paragraph, String variable){
		 final StringWriter paragraphText = new StringWriter();
        try {
            TextUtils.extractText(paragraph, paragraphText);
        } catch (Exception ex) {
           return false;
        }
        final String identifier = paragraphText.toString();
        if (identifier != null && identifier.startsWith(variable)) {
       	 return true;
        }
        return false;
	 }
	 
	 public static void replaceHTML(final MainDocumentPart mainPart, final Map<String, List<Object>> replacements) {
	        Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");
	        Preconditions.checkNotNull(replacements, "replacements may not be null!");

	        // look for all P elements in the specified object
	        final List<P> paragraphs = Lists.newArrayList();
	        new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
	            @Override
	            public List<Object> apply(Object o) {
	                if (o instanceof P) {
	                    paragraphs.add((P) o);
	                }

	                return null;
	            }
	        });

	        // run through all found paragraphs to located identifiers
	        for (final P paragraph : paragraphs) {
	            // check if this is one of our identifiers
	            final StringWriter paragraphText = new StringWriter();
	            try {
	                TextUtils.extractText(paragraph, paragraphText);
	            } catch (Exception ex) {
	               
	            }

	            final String identifier = paragraphText.toString();
	            if (identifier != null && replacements.containsKey(identifier)) {
	                final List<Object> listToModify;

	                if (paragraph.getParent() instanceof Tc) {
	                    // paragraph located in table-cell
	                    final Tc parent = (Tc) paragraph.getParent();
	                    listToModify = parent.getContent();
	                } else if( paragraph.getParent() instanceof Hdr){
	                	final Hdr parent = (Hdr) paragraph.getParent();
	                	listToModify = parent.getContent();
	                }else {
	                    // paragraph located in main document part
	                    listToModify = mainPart.getContent();
	                }

	                if (listToModify != null) {
	                    final int index = listToModify.indexOf(paragraph);
	                    Preconditions.checkState(index > -1, "could not located the paragraph in the specified list!");

	                    // remove the paragraph from it's current index
	                    listToModify.remove(index);
	                    
	                   
	                    // add the converted HTML paragraphs
	                    listToModify.addAll(index, replacements.get(identifier));
	                }
	            }
	        }
	}
	 
	 
	 /*public  static List<Object> wrapHtml(WordprocessingMLPackage mlp, String value) throws Docx4JException{
			XHTMLImporterImpl xhtml = new XHTMLImporterImpl(mlp);
			xhtml.addFontMapping("Times New Roman", "Calibri Light");
			return xhtml.convert("<html><body style='font-family: Arial; font-size:12px'>"+value+"</body></html>",null);
		}*/
	 public  int getIndex(final MainDocumentPart mainPart, String keyword){
		 Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");
	        

	        // look for all P elements in the specified object
	        final List<P> paragraphs = Lists.newArrayList();
	        new TraversalUtil(mainPart, new TraversalUtil.CallbackImpl() {
	            @Override
	            public List<Object> apply(Object o) {
	                if (o instanceof P) {
	                    paragraphs.add((P) o);
	                }

	                return null;
	            }
	        });

	        // run through all found paragraphs to located identifiers
	        for (final P paragraph : paragraphs) {
	            // check if this is one of our identifiers
	            final StringWriter paragraphText = new StringWriter();
	            try {
	                TextUtils.extractText(paragraph, paragraphText);
	            } catch (Exception ex) {
	               
	            }

	            final String identifier = paragraphText.toString();
	            if (identifier != null && identifier.contains(keyword)) {
	            	int index = mainPart.getContent().indexOf(paragraph);
	            	mainPart.getContent().remove(index);
	            	return index;
	            
	            }
	            
	        }
	        return -1;
	}
	 
	 public  int insertTemplate(final WordprocessingMLPackage mlp, int index, WordprocessingMLPackage template) throws Docx4JException{
		 if(index != -1){
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				template.save(os);
				
				byte [] bytes = os.toByteArray();
				AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/part" + (chunk++) + ".docx"));
				afiPart.setContentType(new ContentType(CONTENT_TYPE));
				afiPart.setBinaryData(bytes);
				Relationship altChunkRel = mlp.getMainDocumentPart().addTargetPart(afiPart, AddPartBehaviour.OVERWRITE_IF_NAME_EXISTS);
				
				CTAltChunk chunk = Context.getWmlObjectFactory().createCTAltChunk();
				chunk.setId(altChunkRel.getId());
				if(mlp.getMainDocumentPart().getContent().size() < index){
					//mlp.getMainDocumentPart().getContent().add(chunk);
					for(Object o : template.getMainDocumentPart().getContent())
						mlp.getMainDocumentPart().getContent().add(index++,o);
				
				}else{
					for(Object o : template.getMainDocumentPart().getContent())
						mlp.getMainDocumentPart().getContent().add(index++, o);
					//mlp.getMainDocumentPart().getContent().add(index, chunk);
				}
				//return index; //index +1;
				return index +1;
			}
		 
		 return index;
	 }
	 public  int insertTemplate(final WordprocessingMLPackage mlp, String Keyword, WordprocessingMLPackage template) throws Docx4JException{
		int index = getIndex(mlp.getMainDocumentPart(), Keyword);
		if(index != -1){
			return insertTemplate(mlp, index, template);
		}
		
		return index;
		 
	 }
	 public static String getMatchingText(P paragraph, String variable){
		 final StringWriter paragraphText = new StringWriter();
         try {
             TextUtils.extractText(paragraph, paragraphText);
         } catch (Exception ex) {
            return null;
         }
         final String identifier = paragraphText.toString();
         if (identifier != null && identifier.startsWith(variable)) {
        	 return identifier;
         }
         return null;
	 }
	 public static String getMatchingText(List<Object> paragraphs, String variable){
		 final StringWriter paragraphText = new StringWriter();
		 for(Object paragraph : paragraphs){
			 String text = getMatchingText((P)paragraph, variable);
	         if(text != null)
	        	 return text;
		 }
         return null;
	 }
	


}
