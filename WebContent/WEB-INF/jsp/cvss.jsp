<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<html>
<body>
	<section class="content">
		<bs:row>
			<bs:mco colsize="10">
			<div class="cvss-content" style="width: 900px; overflow-y: auto; direction:rtl; ">
				<div style="direction:ltr; padding-left: 30px">
				<bs:row>
					<bs:mco colsize="6">
					<h2>Base Score</h2>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="6">
						<bs:box type="success" title="Attack Vector (AV)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="av" id="av_n" autocomplete="off"
									 value="N" checked> Network (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="av" id="av_a" autocomplete="off" value="A">
									Adjacent (A)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="av" id="av_l" autocomplete="off" value="L"> Local
									(L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="av" id="av_p" autocomplete="off" value="P">
									Physical (P)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="6">
						<bs:box type="success" title="Scope (S)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="s" id="s_u" autocomplete="off" value="U" checked>
									Unchanged (U)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="s" id="s_c" autocomplete="off" value="C"> Changed (C)
								</label>
							</div>
						</bs:box>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="6">
						<bs:box type="success" title="Attack Complexity (AC)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="ac" id="ac_l" autocomplete="off"
									 value="L" checked> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="ac" id="ac_h" autocomplete="off" value="H">
									High (H)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="6">
						<bs:box type="success" title="Confidentiality (C)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="c" id="c_n" autocomplete="off"
									 value="N" checked> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="c" id="c_l" autocomplete="off" value="L"> Low
									(L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="c" id="c_h" autocomplete="off" value="H"> High
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
									type="radio" name="pr" id="pr_n" autocomplete="off"
									 value="N" checked> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="pr" id="pr_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="pr" id="pr_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="6">
						<bs:box type="success" title="Integrity (I)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="i" id="i_n" autocomplete="off"  value="N" checked>
									None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="i" id="i_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="i" id="i_h" autocomplete="off" value="H"> High (H)
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
									type="radio" name="ui" id="ui_n" autocomplete="off"
									 value="N" checked> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="ui" id="ui_r" autocomplete="off" value="R">
									Required (R)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="6">
						<bs:box type="success" title="Availability (A)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="a" id="a_n" autocomplete="off"
									 value="N" checked> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="a" id="a_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="a" id="a_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
						</bs:box>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
								<hr/>
					 <h2>Temporal Score (Optional)</h2>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="success" title="Exploit Code Maturity (E)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="e" id="e_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="e" id="e_u" autocomplete="off" value="U"> Unproven (U)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="e" id="e_p" autocomplete="off" value="P"> Proof of Concept (P)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="e" id="e_f" autocomplete="off" value="F"> Functional (F)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="e" id="e_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
						</bs:box>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="success" title="Remediation Level (RL)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="rl" id="rl_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="rl" id="rl_o" autocomplete="off" value="O"> Official Fix (O)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="rl" id="rl_t" autocomplete="off" value="T"> Temporary Fix (T)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="rl" id="rl_w" autocomplete="off" value="W"> Work Around (W)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="rl" id="rl_u" autocomplete="off" value="U"> Unavailable (U)
								</label>
							</div>
						</bs:box>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="success" title="Report Confidende (RC)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="rc" id="rc_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="rc" id="rc_u" autocomplete="off" value="U"> Unknown (U) 
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="rc" id="rc_r" autocomplete="off" value="R"> Reasonable (R)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="rc" id="rc_c" autocomplete="off" value="C"> Confirmed (C) 
								</label> 
							</div>
						</bs:box>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
								<hr/>
					<h2>Environmental Score (Optional)</h2>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<h4><b><u>Exploitability Metrics</u></b></h4>
					</bs:mco>
					<bs:mco colsize="12">
						<bs:box type="success" title="Modified Attack Vector (MAV)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="mav" id="mav_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mav" id="mav_n" autocomplete="off" value="N"> Network (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mav" id="mav_a" autocomplete="off" value="A"> Adjacent Network (A)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mav" id="mav_l" autocomplete="off" value="L"> Local (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mav" id="mav_p" autocomplete="off" value="P"> Physical (P)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="12">
						<bs:box type="success" title="Modified Attack Complexity (MAC)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="mac" id="mac_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mac" id="mac_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mac" id="mac_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="12">
						<bs:box type="success" title="Modified Privileges Required (MPR)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="mpr" id="mpr_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mpr" id="mpr_n" autocomplete="off" value="N"> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mpr" id="mpr_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mpr" id="mpr_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="12">
						<bs:box type="success" title="Modified User Interaction (MUI)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="mui" id="mui_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mui" id="mui_n" autocomplete="off" value="N"> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="mui" id="mui_r" autocomplete="off" value="R"> Required (R)
								</label>
							</div>
						</bs:box>
					</bs:mco>
					<bs:mco colsize="12">
						<bs:box type="success" title="Modified Scope (MS)">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary vector"> <input
									type="radio" name="ms" id="ms_x" autocomplete="off"
									 value="X"> Not Defined (X)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="ms" id="ms_u" autocomplete="off" value="U"> Unchanged (U) 
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="ms" id="ms_c" autocomplete="off" value="C"> Changed (C)
								</label>
							</div>
						</bs:box>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="6">
						<bs:row>
							<bs:mco colsize="12">
								<h4><b><u>Impact Metrics</u></b></h4>
							</bs:mco>
							<bs:mco colsize="12">
								<bs:box type="success" title="Modified Confidentiality (MC)">
									<div class="btn-group btn-group-toggle" data-toggle="buttons">
										<label class="btn btn-secondary vector"> <input
											type="radio" name="mc" id="mc_x" autocomplete="off"
											 value="X"> Not Defined (X)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="mc" id="mc_n" autocomplete="off" value="N"> None (N)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="mc" id="mc_l" autocomplete="off" value="L"> Low (L)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="mc" id="mc_h" autocomplete="off" value="H"> High (H)
										</label>
									</div>
								</bs:box>
							</bs:mco>
							<bs:mco colsize="12">
								<bs:box type="success" title="Modified Integrity (MI)">
									<div class="btn-group btn-group-toggle" data-toggle="buttons">
										<label class="btn btn-secondary vector"> <input
											type="radio" name="mi" id="mi_x" autocomplete="off"
											 value="X"> Not Defined (X)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="mi" id="mi_n" autocomplete="off" value="N"> None (N)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="mi" id="mi_l" autocomplete="off" value="L"> Low (L)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="mi" id="mi_h" autocomplete="off" value="H"> High (H)
										</label>
									</div>
								</bs:box>
							</bs:mco>
							<bs:mco colsize="12">
								<bs:box type="success" title="Modified Availability (MA)">
									<div class="btn-group btn-group-toggle" data-toggle="buttons">
										<label class="btn btn-secondary vector"> <input
											type="radio" name="ma" id="ma_x" autocomplete="off"
											 value="X"> Not Defined (X)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ma" id="ma_n" autocomplete="off" value="N"> None (N)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ma" id="ma_l" autocomplete="off" value="L"> Low (L)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ma" id="ma_h" autocomplete="off" value="H"> High (H)
										</label>
									</div>
								</bs:box>
							</bs:mco>
						</bs:row>
					</bs:mco>
					<bs:mco colsize="6">
						<bs:row>
							<bs:mco colsize="12">
								<h4><b><u>Impact SubScore Metrics</u></b></h4>
							</bs:mco>
							<bs:mco colsize="12">
								<bs:box type="success" title="Confidentiality Requirement (CR)">
									<div class="btn-group btn-group-toggle" data-toggle="buttons">
										<label class="btn btn-secondary vector"> <input
											type="radio" name="cr" id="cr_x" autocomplete="off"
											 value="X"> Not Defined (X)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="cr" id="cr_l" autocomplete="off" value="L"> Low (L)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="cr" id="cr_m" autocomplete="off" value="M"> Medium (M)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="cr" id="cr_h" autocomplete="off" value="H"> High (H)
										</label>
									</div>
								</bs:box>
							</bs:mco>
							<bs:mco colsize="12">
								<bs:box type="success" title="Integrity Requirement (IR)">
									<div class="btn-group btn-group-toggle" data-toggle="buttons">
										<label class="btn btn-secondary vector"> <input
											type="radio" name="ir" id="ir_x" autocomplete="off"
											 value="X"> Not Defined (X)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ir" id="ir_l" autocomplete="off" value="L"> Low (L)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ir" id="ir_m" autocomplete="off" value="M"> Medium (M)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ir" id="ir_h" autocomplete="off" value="H"> High (H)
										</label>
									</div>
								</bs:box>
							</bs:mco>
							<bs:mco colsize="12">
								<bs:box type="success" title="Availability Requirement (AR)">
									<div class="btn-group btn-group-toggle" data-toggle="buttons">
										<label class="btn btn-secondary vector"> <input
											type="radio" name="ar" id="ar_x" autocomplete="off"
											 value="X"> Not Defined (X)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ar" id="ar_l" autocomplete="off" value="L"> Low (L)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ar" id="ar_m" autocomplete="off" value="M"> Medium (M)
										</label> <label class="btn btn-secondary vector"> <input type="radio"
											name="ar" id="ar_h" autocomplete="off" value="H"> High (H)
										</label>
									</div>
								</bs:box>
							</bs:mco>
						</bs:row>
					</bs:mco>
				</bs:row>
			</div>
			</div>
			</bs:mco>
			<bs:mco colsize="2">
				<div class="scoreBody">
					<h3 class="scoreNumber None" id="modalScore">0.0</h3>
					<span class="severity None" id="modalSeverity">None</span>
				</div>
			</bs:mco>
		</bs:row>
	</section>
	<input type="hidden" id="modalCVSSString" />
</body>
</html>