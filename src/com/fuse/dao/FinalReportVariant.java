package com.fuse.dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class FinalReportVariant {

	@Id
	private String id = UUID.randomUUID().toString();
	private String fileType;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
	private String base64Content;
	private Boolean largeFile = false;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FinalReportPart> parts = new ArrayList<>();

	public String getBase64Content() {
		if (Boolean.TRUE.equals(this.largeFile)) {
			return this.combineChunks();
		}
		return base64Content;
	}

	public void setBase64Content(String base64Content) {
		if (this.parts != null) {
			this.parts.clear();
		}
		if (base64Content != null && base64Content.length() > 15_000_000) {
			this.largeFile = true;
			String[] chunks = this.chunk(base64Content);
			this.base64Content = "";
			int index = 0;
			for (String c : chunks) {
				FinalReportPart frp = new FinalReportPart();
				frp.setIndex(index++);
				frp.setBase64Part(c);
				this.parts.add(frp);
			}
		} else {
			this.largeFile = false;
			this.base64Content = base64Content;
		}
	}

	/**
	 * Populates content fields from a legacy FinalReport without triggering the
	 * chunking logic in setBase64Content. The legacy record already manages its
	 * own chunking via its parts list; we just need to reference those same objects
	 * in memory for read access via getBase64Content().
	 */
	@Transient
	public void initFromLegacy(String base64, Boolean largeFile, List<FinalReportPart> parts) {
		this.base64Content = base64;
		this.largeFile = largeFile != null && largeFile;
		this.parts = parts != null ? parts : new ArrayList<>();
	}

	@Transient
	private String[] chunk(String largeString) {
		int chunkSize = 15_000_000;
		return largeString.split("(?<=\\G.{" + chunkSize + "})");
	}

	@Transient
	private String combineChunks() {
		StringBuilder b64 = new StringBuilder();
		this.parts.sort(Comparator.comparingInt(FinalReportPart::getIndex));
		this.parts.forEach(part -> b64.append(part.getBase64Part()));
		return b64.toString();
	}
}
