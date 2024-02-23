<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<html>
<body>
	<section class="content">
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="success" title="Attack Vector (AV)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="attackVector" id="av_n" autocomplete="off"
							 value="N" checked> Network (N)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="attackVector" id="av_a" autocomplete="off" value="A">
							Adjacent (A)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="attackVector" id="av_l" autocomplete="off" value="L"> Local
							(L)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="attackVector" id="av_p" autocomplete="off" value="P">
							Physical (P)
						</label>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="4">
				<bs:box type="success" title="Confidentiality (VC)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="vc_confidentiality" id="vc_n" autocomplete="off" value="N" checked>
							None (N)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="vc_confidentiality" id="vc_l" autocomplete="off" value="L"> Low (L)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="vc_confidentiality" id="vc_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="2">
				<div class="scoreBody">
					<h3 class="scoreNumber None" id="modalScore">0.0</h3>
					<span class="severity None" id="modalSeverity">None</span>
				</div>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="success" title="Attack Requirements (AT)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="attackRequirements" id="at_n" autocomplete="off"
							 value="L" checked> None (N)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="attackRequirements" id="at_p" autocomplete="off" value="H">
							Present (P)
						</label>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="success" title="Integrity (VI)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="vc_integrity" id="vi_n" autocomplete="off" value="N" checked>
							None (N)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="vc_integrity" id="vi_l" autocomplete="off" value="L"> Low (L)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="vc_integrity" id="vi_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="success" title="Privileges Required (PR)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="privileges" id="pr_n" autocomplete="off"
							 value="N" checked> None (N)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="privileges" id="pr_l" autocomplete="off" value="L"> Low (L)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="privileges" id="pr_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="success" title="Availability (VA)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="va_availability" id="va_n" autocomplete="off" value="N" checked>
							None (N)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="va_availability" id="va_l" autocomplete="off" value="L"> Low (L)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="va_availability" id="va_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="success" title="User Interaction (UI)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="userInteraction" id="ui_n" autocomplete="off"
							 value="N" checked> None (N)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="userInteraction" id="ui_p" autocomplete="off" value="P">
							Passive (P)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="userInteraction" id="ui_a" autocomplete="off" value="A">
							Active (A)
						</label>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="6">
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="success" title="Confidentiality (SC)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="sc_confientiality" id="sc_n" autocomplete="off" value="N" checked>
							None (N)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="sc_confientiality" id="sc_l" autocomplete="off" value="L"> Low (L)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="sc_confientiality" id="sc_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="6">
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="success" title="Integrity (SI)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="si_integrity" id="si_n" autocomplete="off" value="N" checked>
							None (N)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="si_integrity" id="si_l" autocomplete="off" value="L"> Low (L)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="si_integrity" id="si_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="6">
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="success" title="Availability (SA)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="sa_availability" id="sa_n" autocomplete="off" value="N" checked>
							None (N)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="sa_availability" id="sa_l" autocomplete="off" value="L"> Low (L)
						</label>
						<label class="btn btn-secondary vector"> <input type="radio"
							name="sa_availability" id="sa_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>
	</section>
	<input type="hidden" id="modalCVSSString" />
</body>
</html>