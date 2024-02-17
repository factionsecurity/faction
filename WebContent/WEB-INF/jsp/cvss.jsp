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
				<bs:box type="success" title="Scope (S)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="scope" id="s_u" autocomplete="off" value="U" checked>
							Unchanged (U)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="scope" id="s_o" autocomplete="off" value="C"> Changed (C)
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
				<bs:box type="success" title="Attack Complexity (AC)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="attackComplexity" id="ac_l" autocomplete="off"
							 value="L" checked> Low (L)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="attackComplexity" id="ac_h" autocomplete="off" value="H">
							High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="success" title="Confidentiality (C)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="confidentiality" id="c_n" autocomplete="off"
							 value="N" checked> None (N)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="confidentiality" id="c_l" autocomplete="off" value="L"> Low
							(L)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="confidentiality" id="c_h" autocomplete="off" value="H"> High
							(H)
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
				<bs:box type="success" title="Integrity (I)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="integrity" id="i_n" autocomplete="off"  value="N" checked>
							None (N)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="integrity" id="i_l" autocomplete="off" value="L"> Low (L)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="integrity" id="i_h" autocomplete="off" value="H"> High (H)
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
							name="userInteraction" id="ui_r" autocomplete="off" value="R">
							Required (R)
						</label>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="success" title="Availability (A)">
					<div class="btn-group btn-group-toggle" data-toggle="buttons">
						<label class="btn btn-secondary activeVector vector"> <input
							type="radio" name="availability" id="a_n" autocomplete="off"
							 value="N" checked> None (N)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="availability" id="a_l" autocomplete="off" value="L"> Low (L)
						</label> <label class="btn btn-secondary vector"> <input type="radio"
							name="availability" id="a_h" autocomplete="off" value="H"> High (H)
						</label>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>
	</section>
	<input type="hidden" id="modalCVSSString" />
</body>
</html>