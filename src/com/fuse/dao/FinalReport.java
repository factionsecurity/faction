package com.fuse.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

@Entity
public class FinalReport{
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "frepGen")
    @TableGenerator(
        name = "frepGen",
        table = "frepGenseq",
        pkColumnValue = "frep",
        valueColumnName = "nextfrep",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String filename;
	private String base64EncodedPdf;
	private Date gentime;
	private Boolean retest;
	private String fileType;
	private Boolean largeFile=false;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FinalReportPart>parts;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getBase64EncodedPdf() {
		if(this.largeFile != null && this.largeFile) {
			return this.combineChunks();
		}else {
			return base64EncodedPdf;
		}
	}
	public void setBase64EncodedPdf(String base64EncodedPdf) {
		if(this.parts != null) {
			this.parts.clear();
		}
		if(base64EncodedPdf != null && base64EncodedPdf.length() > 15_000_000) {
			this.largeFile=true;
			String [] chunks = this.chunk(base64EncodedPdf);
			this.setBase64EncodedPdf("");
			int index=0;
			for(String c : chunks) {
				FinalReportPart frp = new FinalReportPart();
				frp.setIndex(index++);
				frp.setBase64Part(c);
				this.parts.add(frp);
			}
		}else {
			this.base64EncodedPdf = base64EncodedPdf;
		}
	}
	public Date getGentime() {
		return gentime;
	}
	public void setGentime(Date gentime) {
		this.gentime = gentime;
	}
	public Boolean getRetest() {
		return retest == null? false : retest;
	}
	public void setRetest(Boolean retest) {
		this.retest = retest;
	}
	public String getFileType() {
		return this.fileType == null || this.fileType.equals("")? "docx": this.fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public Boolean getLargeFile() {
		return largeFile;
	}
	public void setLargeFile(Boolean largeFile) {
		this.largeFile = largeFile;
	}
	public List<FinalReportPart> getParts(){
		return this.parts;
	}
	public void setParts(List<FinalReportPart>parts) {
		this.parts = parts;
	}
	@Transient	
	private String[] chunk(String largeString){
		int chunkSize = 15_000_000;
		return largeString.split("(?<=\\G.{" + chunkSize + "})");
	}
	@Transient
	private String combineChunks() {
		StringBuilder b64report = new StringBuilder();
		this.parts.sort(Comparator.comparingInt(FinalReportPart::getIndex));
		this.parts.forEach( part -> b64report.append(part.getBase64Part()));
		return b64report.toString();
	}
	
	
	

}
