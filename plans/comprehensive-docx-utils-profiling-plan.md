# Comprehensive DocxUtils.java Profiling Implementation Plan

## Overview
This plan details the implementation of profiling annotations and instrumentation for all 47 methods in [`DocxUtils.java`](src/com/fuse/reporting/DocxUtils.java). The profiling will provide comprehensive performance metrics with minimal overhead (~100-200ns per method call when enabled).

## Method Inventory Analysis

### Total Methods Found: 47
- **Constructors**: 2 methods
- **Public Methods**: 4 methods  
- **Private Methods**: 41 methods

## Implementation Strategy

### 1. Import Statements (Required Additions)

Add these imports to the top of DocxUtils.java:

```java
import com.fuse.utils.MethodProfiler;
import com.fuse.utils.ProfileMethod;
```

### 2. Profiling Pattern

Each method will follow this pattern:

```java
@ProfileMethod("Brief description of what method does")
public/private ReturnType methodName(parameters) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "methodName");
    try {
        // Original method body
        return result;
    } finally {
        context.end();
    }
}
```

For `void` methods:
```java
@ProfileMethod("Brief description")
private void methodName(parameters) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "methodName");
    try {
        // Original method body
    } finally {
        context.end();
    }
}
```

## Detailed Method-by-Method Implementation Plan

### Phase 1: Constructors (2 methods)

#### 1.1 Constructor #1 (Line 88)
```java
@ProfileMethod("Initialize DocxUtils with EntityManagerFactory and document package")
public DocxUtils(EntityManagerFactory entityManagerFactory, WordprocessingMLPackage mlp, Assessment assessment) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "DocxUtils");
    try {
        this.mlp = mlp;
        this.reportExtension = new Extensions(entityManagerFactory, Extensions.EventType.REPORT_MANAGER);
        this.assessment = assessment;
        this.outlineImages();
        this.vulns = assessment.getVulns();
        this.setupReportSections(entityManagerFactory);
    } finally {
        context.end();
    }
}
```

#### 1.2 Constructor #2 (Line 97)
```java
@ProfileMethod("Initialize DocxUtils with document package and assessment")
public DocxUtils(WordprocessingMLPackage mlp, Assessment assessment) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "DocxUtils");
    try {
        this.mlp = mlp;
        this.reportExtension = new Extensions(HibHelper.getInstance().getEMF(), Extensions.EventType.REPORT_MANAGER);
        this.assessment = assessment;
        this.outlineImages();
        this.vulns = assessment.getVulns();
        this.setupReportSections(HibHelper.getInstance().getEMF());
    } finally {
        context.end();
    }
}
```

### Phase 2: Public Methods (4 methods)

#### 2.1 generateDocx (Line 538) - CRITICAL METHOD
```java
@ProfileMethod("Main document generation entry point with custom CSS")
public WordprocessingMLPackage generateDocx(String customCSS) throws Exception {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "generateDocx");
    try {
        VariablePrepare.prepare(mlp);

        // Convert all tables and match and replace values
        checkTables("vulnTable", "Default", customCSS);
        setFindings("Default", customCSS);
        if(ReportFeatures.allowSections()) {
            for(String section : this.reportSections) {
                checkTables("vulnTable", section, customCSS);
                setFindings(section, customCSS);
            }
        }

        // look for findings areass {fiBegin/fiEnd}
        HashMap<String, List<Object>> map = new HashMap();

        map.put("${summary1}",
                this.wrapHTML(this.assessment.getSummary() == null ? "" : this.assessment.getSummary(), customCSS, "summary1"));
        map.put("${summary2}",
                this.wrapHTML(this.assessment.getRiskAnalysis() == null ? "" : this.assessment.getRiskAnalysis(), customCSS, "summary2"));
        replaceHTML(mlp.getMainDocumentPart(), map, false);
        replaceAssessment(customCSS);
        updateDocWithExtensions(customCSS);

        return mlp;
    } finally {
        context.end();
    }
}
```

#### 2.2 replaceHyperlink (Line 1459)
```java
@ProfileMethod("Replace hyperlink text and update URL targets")
public void replaceHyperlink(Object wordPackage, String searchText, String newUrl) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceHyperlink");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 2.3 replaceFigureVariables (Line 1677)
```java
@ProfileMethod("Replace figure number variables in text content")
public String replaceFigureVariables(String text, int index) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceFigureVariables");
    try {
        // Pattern to match ${Figure#.X} where X is one or more digits
        Pattern pattern = Pattern.compile("\\$\\{Figure#\\.(\\d+)\\}");
        Matcher matcher = pattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String subNumber = matcher.group(1);
            String replacement = "Figure " + index + "." + subNumber;
            matcher.appendReplacement(result, replacement);
        }
        
        matcher.appendTail(result);
        return result.toString();
    } finally {
        context.end();
    }
}
```

#### 2.4 tocGenerator (Line 1752)
```java
@ProfileMethod("Generate table of contents in document")
public void tocGenerator(WordprocessingMLPackage mlp) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "tocGenerator");
    try {
        // Generate the table of contents
        TocGenerator tocGenerator = new TocGenerator(mlp);
        int index = this.getIndex(mlp.getMainDocumentPart(), "${TOC}");
        if (index == -1)
            return;
        tocGenerator.generateToc(index, "TOC \\o \"1-3\" \\h \\z \\u ", STTabTlc.DOT, true);
        addPageBreak(mlp, index + 1);
    } catch (TocException e) {
        e.printStackTrace();
    } finally {
        context.end();
    }
}
```

### Phase 3: Core Document Processing Methods (3 methods)

#### 3.1 checkTables (Line 182) - CRITICAL METHOD
```java
@ProfileMethod("Process vulnerability tables and insert data rows")
private void checkTables(String variable, String section, String customCSS)
        throws JAXBException, Docx4JException {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "checkTables");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 3.2 setFindings (Line 1011) - CRITICAL METHOD
```java
@ProfileMethod("Set vulnerability findings sections in document")
private void setFindings(String section, String customCSS)
        throws JAXBException, Docx4JException {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "setFindings");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 3.3 replaceAssessment (Line 650) - CRITICAL METHOD
```java
@ProfileMethod("Replace assessment data variables throughout document")
private void replaceAssessment(String customCSS) throws Exception {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceAssessment");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

### Phase 4: HTML Processing Methods (4 methods)

#### 4.1 wrapHTML - Version 1 (Line 570)
```java
@ProfileMethod("Wrap HTML content with CSS styling for Word conversion")
private List<Object> wrapHTML(String content, String customCSS, String className) throws Docx4JException {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "wrapHTML");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 4.2 wrapHTML - Version 2 (Line 597)
```java
@ProfileMethod("Wrap HTML content with CSS styling and max width constraint")
private List<Object> wrapHTML(String value, String customCSS, String className, BigInteger maxWidth)
        throws Docx4JException {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "wrapHTML_withWidth");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 4.3 replaceHTML - Version 1 (Line 1421)
```java
@ProfileMethod("Replace HTML content in document paragraphs")
private void replaceHTML(final Object mainPart, final Map<String, List<Object>> replacements) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceHTML");
    try {
        replaceHTML(mainPart, replacements, true);
    } finally {
        context.end();
    }
}
```

#### 4.4 replaceHTML - Version 2 (Line 1636)
```java
@ProfileMethod("Replace HTML content with once flag control")
private void replaceHTML(final Object mainPart, final Map<String, List<Object>> replacements, boolean once) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceHTML_withOnce");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

### Phase 5: Assessment Replacement Methods (6 methods)

#### 5.1 replacementText (Line 857)
```java
@ProfileMethod("Replace simple text variables in main document")
private void replacementText(Map<String, String> map) throws JAXBException, Docx4JException {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replacementText");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 5.2 replacementDate (Line 866)
```java
@ProfileMethod("Replace date variables with formatted dates")
private void replacementDate(String key, Date date) throws JAXBException, Docx4JException {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replacementDate");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 5.3 replacementHyperlinks (Line 848)
```java
@ProfileMethod("Replace text in hyperlinks throughout document")
private void replacementHyperlinks(Object document, Map<String,String> map) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replacementHyperlinks");
    try {
        for (String key : map.keySet()) {
            String value = map.get(key);
            this.replaceHyperlink(document, "${" + key + " link}", value);
        }
    } finally {
        context.end();
    }
}
```

#### 5.4 replacement (Line 914)
```java
@ProfileMethod("Replace assessment variables in HTML content")
private String replacement(String content) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replacement");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 5.5 replaceDateVariable (Line 873) - STATIC METHOD
```java
@ProfileMethod("Replace date variables with custom format patterns")
public static String replaceDateVariable(String text, String key, Date date) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceDateVariable");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 5.6 replaceHeaderAndFooter (Line 1571)
```java
@ProfileMethod("Replace variables in document headers and footers")
private void replaceHeaderAndFooter(final Map<String,String> replacements) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceHeaderAndFooter");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

### Phase 6: Image Processing Methods (3 methods)

#### 6.1 replaceImageLinks (Line 978)
```java
@ProfileMethod("Replace image links with base64 encoded data")
private String replaceImageLinks(String text) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "replaceImageLinks");
    try {
        text = this.centerImages(text);
        Long aid= this.assessment.getId();
        String matchPrefix = "getImage\\?id(=|&#61;)" + aid + ":";
        String badImage = "<img src=\"getImage\\?id(=|&#61;)undefined\" >";
        text = text.replaceAll(badImage, "");
        
        for(Image img : this.assessment.getImages()) {
            String matchStr = matchPrefix + img.getGuid();
            text = text.replaceAll( matchStr, img.getBase64Image());
        }
        return text;
    } finally {
        context.end();
    }
}
```

#### 6.2 outlineImages (Line 993)
```java
@ProfileMethod("Add borders to assessment images")
private void outlineImages() {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "outlineImages");
    try {
        for(Image img : this.assessment.getImages()) {
            try {
                String[] parts = img.getBase64Image().split(",");
                String file_dataContentType = parts[0].split(";")[0].replace("data:", "");
                byte[] imageData = Base64.getDecoder().decode(parts[1]);
                imageData = ImageBorderUtil.addBorder(imageData, 1, Color.GRAY);
                String borderedImage = Base64.getEncoder().encodeToString(imageData);
                borderedImage = "data:" + file_dataContentType +";base64,"+ borderedImage;
                img.setBase64Image(borderedImage);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } finally {
        context.end();
    }
}
```

#### 6.3 centerImages (Line 1553)
```java
@ProfileMethod("Center image elements in HTML content")
private String centerImages(String content) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "centerImages");
    try {
        int index = content.indexOf("<img ");
        while(index !=-1) {
            String first = content.substring(0,index);
            String second = content.substring(index,content.length());
            second = second.replaceFirst("/>", "></img>");
            content = first + second;
            index = content.indexOf("<img ", index+1);
        }
        content = content.replaceAll("<p><img", "<center><img");
        content = content.replaceAll("</img><br /></p>", "</img></center>");
        return content;
    } finally {
        context.end();
    }
}
```

### Phase 7: Table and Element Processing Methods (8 methods)

#### 7.1 getAllElementFromObject (Line 1291)
```java
@ProfileMethod("Recursively extract all elements of specified type from document object")
private List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getAllElementFromObject");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 7.2 indexOfRow (Line 1368)
```java
@ProfileMethod("Find table row index containing specified variable")
private int indexOfRow(Tbl table, List<Object> paragraphs, String variable) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "indexOfRow");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 7.3 indexOfCell (Line 1391)
```java
@ProfileMethod("Find table cell index containing specified variable")
private int indexOfCell(Tbl table, List<Object> paragraphs, String variable) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "indexOfCell");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 7.4 changeColorOfCell (Line 1311)
```java
@ProfileMethod("Change background color of table cell based on variable")
private int changeColorOfCell(Tr row, String variable, String color) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "changeColorOfCell");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 7.5 changeColorOfText (Line 1330)
```java
@ProfileMethod("Change text color in table row based on variable")
private int changeColorOfText(Tr row, String variable, String color) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "changeColorOfText");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 7.6 matchText (Line 1407)
```java
@ProfileMethod("Check if paragraph text matches specified variable")
private boolean matchText(P paragraph, String variable) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "matchText");
    try {
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
    } finally {
        context.end();
    }
}
```

#### 7.7 getParagraphs (Line 1425)
```java
@ProfileMethod("Extract all paragraphs from document part")
private List<P> getParagraphs(final Object mainPart){
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getParagraphs");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 7.8 getUpdatableElements (Line 1694)
```java
@ProfileMethod("Get updatable element list containing specified paragraph")
private List<Object> getUpdatableElements(P paragraph){
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getUpdatableElements");
    try {
        if (paragraph.getParent() instanceof Tc) {
            final Tc parent = (Tc) paragraph.getParent();
            return parent.getContent();
        } else if (paragraph.getParent() instanceof Hdr) {
            final Hdr parent = (Hdr) paragraph.getParent();
            return parent.getContent();
        } else if (paragraph.getParent() instanceof CTTxbxContent) {
            final CTTxbxContent parent = (CTTxbxContent) paragraph.getParent();
            return parent.getContent();
        } else {
            return ((MainDocumentPart) mlp.getMainDocumentPart()).getContent();
        }
    } finally {
        context.end();
    }
}
```

### Phase 8: Hyperlink Processing Methods (3 methods)

#### 8.1 getHyperLinks (Line 1441)
```java
@ProfileMethod("Extract all hyperlinks from document part")
private List<P.Hyperlink> getHyperLinks(final Object mainPart) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getHyperLinks");
    try {
        // ... existing method body ...
    } finally {
        context.end();
    }
}
```

#### 8.2 getHyperlinkDisplayText (Line 1510)
```java
@ProfileMethod("Extract display text from hyperlink element")
private String getHyperlinkDisplayText(P.Hyperlink hyperlink) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getHyperlinkDisplayText");
    try {
        StringBuilder text = new StringBuilder();
        for (Object obj : hyperlink.getContent()) {
            if (obj instanceof R) {
                R run = (R) obj;
                for (Object runContent : run.getContent()) {
                    if (runContent instanceof Text) {
                        text.append(((Text) runContent).getValue());
                    } else if (runContent instanceof JAXBElement) {
                        JAXBElement<?> element = (JAXBElement<?>) runContent;
                        if (element.getValue() instanceof Text) {
                            text.append(((Text) element.getValue()).getValue());
                        }
                    }
                }
            }
        }
        return text.toString();
    } finally {
        context.end();
    }
}
```

#### 8.3 updateHyperlinkDisplayText (Line 1530)
```java
@ProfileMethod("Update hyperlink display text with new content")
private void updateHyperlinkDisplayText(P.Hyperlink hyperlink, String newText) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "updateHyperlinkDisplayText");
    try {
        // Clear existing content
        hyperlink.getContent().clear();
        
        // Create new run with the text
        R run = new R();
        
        // Add hyperlink style
        RPr runProps = new RPr();
        RStyle hyperlinkStyle = new RStyle();
        hyperlinkStyle.setVal("Hyperlink");
        runProps.setRStyle(hyperlinkStyle);
        run.setRPr(runProps);
        
        // Add the text
        Text text = new Text();
        text.setValue(newText);
        run.getContent().add(text);
        
        hyperlink.getContent().add(run);
    } finally {
        context.end();
    }
}
```

### Phase 9: Document Structure Methods (4 methods)

#### 9.1 addPageBreak (Line 1769)
```java
@ProfileMethod("Add page break at specified document index")
private void addPageBreak(WordprocessingMLPackage mlp, int index) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "addPageBreak");
    try {
        org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();
        P p = wmlObjectFactory.createP();
        R r = wmlObjectFactory.createR();
        p.getContent().add(r);
        Br br = wmlObjectFactory.createBr();
        r.getContent().add(br);
        br.setType(org.docx4j.wml.STBrType.PAGE);
        mlp.getMainDocumentPart().getContent().add(index, p);
    } finally {
        context.end();
    }
}
```

#### 9.2 getIndex (Line 1783)
```java
@ProfileMethod("Find paragraph index containing specified keyword")
private int getIndex(final MainDocumentPart mainPart, String keyword) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getIndex");
    try {
        Preconditions.checkNotNull(mainPart, "the supplied main doc part may not be null!");

        final List<P> paragraphs = this.getParagraphs(mainPart);

        for (final P paragraph : paragraphs) {
            final StringWriter paragraphText = new StringWriter();
            try {
                TextUtils.extractText(paragraph, paragraphText);
            } catch (Exception ex) {
            }

            final String identifier = paragraphText.toString();
            if (identifier != null && identifier.contains(keyword)) {
                int index = mainPart.getContent().indexOf(paragraph);
                if (index == -1) {
                    return -1;
                } else {
                    mainPart.getContent().remove(index);
                    return index;
                }
            }
        }
        return -1;
    } finally {
        context.end();
    }
}
```

#### 9.3 getMatchingText - Version 1 (Line 1816)
```java
@ProfileMethod("Get text from paragraph if it matches variable pattern")
private String getMatchingText(P paragraph, String variable) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getMatchingText");
    try {
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
    } finally {
        context.end();
    }
}
```

#### 9.4 getMatchingText - Version 2 (Line 1830)
```java
@ProfileMethod("Find matching text in list of paragraphs")
private String getMatchingText(List<Object> paragraphs, String variable) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getMatchingText_fromList");
    try {
        final StringWriter paragraphText = new StringWriter();
        for (Object paragraph : paragraphs) {
            String text = getMatchingText((P) paragraph, variable);
            if (text != null)
                return text;
        }
        return null;
    } finally {
        context.end();
    }
}
```

### Phase 10: Extension Methods (1 method)

#### 10.1 updateDocWithExtensions (Line 1712)
```java
@ProfileMethod("Update document using extension modules")
private void updateDocWithExtensions(String customCSS) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "updateDocWithExtensions");
    try {
        MainDocumentPart mainPart = mlp.getMainDocumentPart();
        List<P> paragraphs = this.getParagraphs(mainPart);

        for (final P paragraph : paragraphs) {
            final StringWriter paragraphText = new StringWriter();
            try {
                TextUtils.extractText(paragraph, paragraphText);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            final String identifier = paragraphText.toString().trim();
            final List<Object> listToModify = this.getUpdatableElements(paragraph);

            if (listToModify != null) {
                final int index = listToModify.indexOf(paragraph);
                Preconditions.checkState(index > -1, "could not located the paragraph in the specified list!");
                if(this.reportExtension.isExtended()) {
                    String html = this.reportExtension.updateReport(this.assessment, identifier);
                    if(html != null && !html.equals(identifier)) {
                        listToModify.remove(index);
                        try {
                            listToModify.addAll(index,this.wrapHTML(html,customCSS, ""));
                        }catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    } finally {
        context.end();
    }
}
```

### Phase 11: Utility Methods (15 methods)

#### 11.1 setupReportSections (Line 106)
```java
@ProfileMethod("Setup report sections based on system settings")
private void setupReportSections(EntityManagerFactory entityManagerFactory) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "setupReportSections");
    try {
        if(ReportFeatures.allowSections()) {
            EntityManager em = entityManagerFactory.createEntityManager();
            try {
                SystemSettings ems = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream()
                        .findFirst().orElse(null);
                String features = ems.getFeatures();
                reportSections = ReportFeatures.getFeatures(features);
            }finally {
                em.close();
            }
        }
    } finally {
        context.end();
    }
}
```

#### 11.2 sectionExists (Line 120)
```java
@ProfileMethod("Check if report section exists in configuration")
private Boolean sectionExists(String section) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "sectionExists");
    try {
        return Arrays.asList(this.reportSections).stream().anyMatch(reportSection -> reportSection.equals(section));
    } finally {
        context.end();
    }
}
```

#### 11.3 cellContains (Line 124)
```java
@ProfileMethod("Check if table cell contains specified variable")
private boolean cellContains(Tc cell, String variable) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "cellContains");
    try {
        for (Object obj : cell.getContent()) {
            String xml = XmlUtils.marshaltoString(obj, false, false);
            if(xml.contains(variable)) {
                return true;
            }
        }
        return false;
    } finally {
        context.end();
    }
}
```

#### 11.4 setWidths (Line 135)
```java
@ProfileMethod("Set column widths for table cells containing variables")
private Map<String,BigInteger> setWidths(Tc cell, String variable, Map<String, BigInteger> widths){
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "setWidths");
    try {
        if(cellContains(cell, "${" + variable + "}")) {
            if(cell.getTcPr() != null && cell.getTcPr().getTcW() != null) {
                BigInteger margin = BigInteger.valueOf(200); 
                widths.put(variable, cell.getTcPr().getTcW().getW().subtract(margin));
            }else {
                widths.put(variable, BigInteger.valueOf(-1));
            }
        }
        return widths;
    } finally {
        context.end();
    }
}
```

#### 11.5 getFilteredVulns (Line 148)
```java
@ProfileMethod("Get vulnerabilities filtered by report section")
private List<Vulnerability> getFilteredVulns(String section){
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getFilteredVulns");
    try {
        if(section == null) {
            section = "Default";
        }else if(section.isEmpty()) {
            section = "Default";
        }
        
        List<Vulnerability> filteredVulns = new ArrayList<>();
        if(section.equals("Default")) {
            filteredVulns = this.vulns.stream()
                    .filter(vuln -> vuln.getSection() == null 
                                    || vuln.getSection().isEmpty() 
                                    || vuln.getSection().equals("Default")
                                    || !sectionExists(vuln.getSection())
                                    )
                    .collect(Collectors.toList());
        }else {
            final String query = section;
            filteredVulns = this.vulns.stream()
                    .filter(
                            vuln -> vuln.getSection().equals(query)
                    ).collect(Collectors.toList());
        }
        
        return filteredVulns;
    } finally {
        context.end();
    }
}
```

#### 11.6 CData (Assumed around Line 177)
```java
@ProfileMethod("Wrap text content in CDATA section")
private String CData(String text) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "CData");
    try {
        return "<![CDATA[" + text +  "]]>";
    } finally {
        context.end();
    }
}
```

#### 11.7 loopReplace (Line 728)
```java
@ProfileMethod("Replace assessment loop variables in content")
private String loopReplace(String content) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "loopReplace");
    try {
        for(int i=9; i>=0; i--) {
            content = innerLoop(content, i);
        }
        return content;
    } finally {
        context.end();
    }
}
```

#### 11.8 innerLoop (Line 736)
```java
@ProfileMethod("Process inner loop for vulnerability severity rankings")
private String innerLoop(String content, int rank ) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "innerLoop");
    try {
        Vulnerability tmp = new Vulnerability();
        String Var = tmp.vulnStr(new Long(rank)).toUpperCase();
        if (content.contains("{[asmt" + Var + "]}")) {
            String html = "<ol>\r\n";
            boolean isSomething = false;
            for (Vulnerability v : this.vulns) {
                if (v.getOverall() == rank) {
                    isSomething = true;
                    html += "<li>" + v.getName() + "</li>";
                }
            }
            html += "</ol>";
            if (isSomething == false) {
                html = "<i>No vulnerabilities found at this severity.</i>&nbsp;";
            }
            content = content.replaceAll("\\{\\[assessment\\." + Var + "\\]\\}", html);
        }
        return content;
    } finally {
        context.end();
    }
}
```

#### 11.9 getVulnCount - Array Version (Line 757)
```java
@ProfileMethod("Get vulnerability count array by severity level")
private int[] getVulnCount() {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getVulnCount");
    try {
        int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        if (this.vulns == null)
            return results;
        else {
            for (Vulnerability v : this.vulns) {
                if (v.getOverall() == null || v.getOverall() == -1l)
                    continue;
                results[v.getOverall().intValue()]++;
            }
            return results;
        }
    } finally {
        context.end();
    }
}
```

#### 11.10 getVulnCount - String Version (Line 775)
```java
@ProfileMethod("Replace vulnerability count variables in content string")
private String getVulnCount(String content ) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getVulnCount_withContent");
    try {
        int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int totals = 0;
        if (this.vulns != null) {
            for (Vulnerability v : this.vulns) {
                if (v.getOverall() == null || v.getOverall().intValue() == -1)
                    continue;
                results[v.getOverall().intValue()]++;
                totals++;
            }
            for (int i = 0; i < 10; i++) {
                content = content.replaceAll("\\$\\{riskCount" + i + "\\}", "" + results[i]);
            }
            content = content.replaceAll("\\$\\{riskTotal\\}", "" + totals);
        }
        return content;
    } finally {
        context.end();
    }
}
```

#### 11.11 getVulnMap - String Map Version (Line 795)
```java
@ProfileMethod("Get vulnerability count map for text replacement")
private Map<String, String> getVulnMap() {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getVulnMap");
    try {
        Map<String, String> maps = new HashMap();
        int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int totals = 0;
        if (this.vulns!= null) {
            for (Vulnerability v : this.vulns) {
                if (v.getOverall() == null || v.getOverall().intValue() == -1)
                    continue;
                results[v.getOverall().intValue()]++;
                totals++;
            }
        }
        for (int i = 0; i < 10; i++) {
            maps.put("riskCount" + i + "", "" + results[i]);
        }
        maps.put("riskTotal", "" + totals);
        return maps;
    } finally {
        context.end();
    }
}
```

#### 11.12 getVulnMap - HTML Version (Around Line 817)
```java
@ProfileMethod("Get vulnerability count map for HTML replacement")
private Map<String, List<Object>> getVulnMap(String customCSS) throws Docx4JException {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getVulnMap_withCSS");
    try {
        Map<String, List<Object>> maps = new HashMap();
        int[] results = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        if (this.vulns != null) {
            for (Vulnerability v : vulns) {
                if (v.getOverall() == null || v.getOverall().intValue() == -1)
                    continue;
                results[v.getOverall().intValue()]++;
            }
        }
        for (int i = 0; i < 10; i++) {
            maps.put("${riskCount" + i + "}", wrapHTML( "" + results[i], customCSS, ""));
        }
        return maps;
    } finally {
        context.end();
    }
}
```

#### 11.13 getKey (Line 837)
```java
@ProfileMethod("Get keyword key from predefined keywords array")
private String getKey(String key) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getKey");
    try {
        for (String word : keywords) {
            word = word.replace("\\", "");
            if (word.toLowerCase().equals(key.toLowerCase()))
                return word;
        }
        return "assessment.nothing";
    } finally {
        context.end();
    }
}
```

#### 11.14 getTotalOpenVulns (Line 905)
```java
@ProfileMethod("Count total open vulnerabilities")
private String getTotalOpenVulns(List<Vulnerability> vulns) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getTotalOpenVulns");
    try {
        return ""+vulns.stream().filter(v -> v.getClosed() == null).collect(Collectors.toList()).size();
    } finally {
        context.end();
    }
}
```

#### 11.15 getTotalClosedVulns (Line 909)
```java
@ProfileMethod("Count total closed vulnerabilities")
private String getTotalClosedVulns(List<Vulnerability> vulns) {
    MethodProfiler.ProfileContext context = MethodProfiler.start("DocxUtils", "getTotalClosedVulns");
    try {
        return ""+vulns.stream().filter(v -> v.getClosed() != null).collect(Collectors.toList()).size();
    } finally {
        context.end();
    }
}
```

## Implementation Checklist

### Pre-Implementation
- [ ] Backup original DocxUtils.java file
- [ ] Add required import statements
- [ ] Verify MethodProfiler and ProfileMethod classes are accessible

### Implementation Phases
- [ ] Phase 1: Constructors (2 methods)
- [ ] Phase 2: Public Methods (4 methods)  
- [ ] Phase 3: Core Document Processing (3 methods)
- [ ] Phase 4: HTML Processing (4 methods)
- [ ] Phase 5: Assessment Replacement (6 methods)
- [ ] Phase 6: Image Processing (3 methods)
- [ ] Phase 7: Table/Element Processing (8 methods)
- [ ] Phase 8: Hyperlink Processing (3 methods)
- [ ] Phase 9: Document Structure (4 methods)
- [ ] Phase 10: Extension Methods (1 method)
- [ ] Phase 11: Utility Methods (15 methods)

### Post-Implementation
- [ ] Compile and verify no syntax errors
- [ ] Run unit tests to ensure functionality unchanged
- [ ] Test profiling with DocxUtilsProfilingExample
- [ ] Validate profiling report output
- [ ] Update documentation with profiling results

## Performance Impact Analysis

### Expected Overhead
- **Profiling Disabled**: ~5-10ns per method call (negligible)
- **Profiling Enabled**: ~100-200ns per method call (minimal)
- **Memory Usage**: ~2KB for all 47 methods (ConcurrentHashMap + statistics)

### Profiling Benefits
- **Comprehensive Coverage**: All 47 methods profiled for complete visibility
- **Performance Bottleneck Identification**: Sorted by execution time
- **Method Call Analysis**: Count and duration tracking
- **Optimization Verification**: Before/after performance comparison

## Expected Profiling Report Structure

```
=== DocxUtils Method Profiling Report ===
Method                                             Calls      Total (ms)      Avg (ms)      Min (ms)      Max (ms)
================================================================================================================================
DocxUtils.generateDocx                                 1         1247.52       1247.52       1247.52       1247.52
DocxUtils.checkTables                                  2          891.23        445.62         98.45        792.78
DocxUtils.setFindings                                  2          156.89         78.45         45.23        111.66
DocxUtils.wrapHTML                                    15          234.67         15.64          2.13         89.23
DocxUtils.replaceAssessment                            1          134.56        134.56        134.56        134.56
DocxUtils.getAllElementFromObject                     89           67.23          0.76          0.12          5.67
DocxUtils.replaceImageLinks                            3           23.45          7.82          1.23         18.67
... (all 47 methods listed)
================================================================================================================================
Total Methods: 47 | Total Calls: 200+ | Total Time: 3000+ ms
=== End Report ===
```

This comprehensive profiling implementation will provide complete visibility into DocxUtils performance characteristics and enable data-driven optimization decisions.