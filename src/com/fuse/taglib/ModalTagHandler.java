package com.fuse.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class ModalTagHandler extends TagSupport {

	private String modalId;
	private String saveId;
	private String title;
	private String width;
	private String color = "red";
	private String saveText = "Save";
	private String closeText = "Close";

	@Override
	public int doStartTag() throws JspException {
		try {
			// Get the writer object for output.
			JspWriter out = pageContext.getOut();
			String html = "<div class='modal' id='" + this.modalId + "' >"
					+ "   <div class='modal-dialog' style='width:" + this.width + "'>"
					+ "     <div class='modal-content'>" + "       <div class='modal-header bg-" + color + "'>"
					+ "         <button type='button' class='close' data-dismiss='modal' aria-label='Close'><span aria-hidden='true'>&times;</span></button>"
					+ "         <h4 class='modal-title'><b>" + this.title + "</b></h4>" + "       </div>"
					+ "       <div class='modal-body bg-" + color + "'>";

			out.println(html);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return EVAL_BODY_AGAIN;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			String html = "</tbody>" + "<tfoot>" + "</tfoot>" + "</table>" + "</div>" + "</div>";
			JspWriter out = pageContext.getOut();
			out.println(html);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return SKIP_BODY;

	}

	@Override
	public int doAfterBody() throws JspException {
		try {
			String html = "       </div>" + "       <div class='modal-footer bg-" + color + "'>"
					+ "         <button type='button' class='btn btn-default pull-left' data-dismiss='modal'> "
					+ this.closeText + "</button>";
			if (!this.saveId.equals("")) {
				html += "         <button type='button' class='btn btn-primary fileinput-upload-button' id='"
						+ this.saveId + "'><i class='fa fa-save'></i> " + this.saveText + "</button>";
			}
			html += "       </div>" + "     </div>" + "   </div>" + " </div>";

			JspWriter out = pageContext.getOut();
			out.println(html);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return SKIP_BODY;
	}

	public String getModalId() {
		return modalId;
	}

	public void setModalId(String modalId) {
		this.modalId = modalId;
	}

	public String getSaveId() {
		return saveId;
	}

	public void setSaveId(String saveId) {
		this.saveId = saveId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSaveText() {
		return saveText;
	}

	public void setSaveText(String saveText) {
		this.saveText = saveText;
	}

	public String getCloseText() {
		return closeText;
	}

	public void setCloseText(String closeText) {
		this.closeText = closeText;
	}

}