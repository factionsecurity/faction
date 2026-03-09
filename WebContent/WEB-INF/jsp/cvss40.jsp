
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
			<div class="cvss-content" style="overflow-y: auto; direction:rtl; ">
				<div style="direction:ltr; padding-left: 30px;">
				<bs:row>
					<bs:mco colsize="6">
					<h2>Base Metrics</h2>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<h4><b><center><u>Exploitability Metrics</u></center></b></h4>
						<br/>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="3"><label>Attack Vector (AV):</label></bs:mco>
					<bs:mco colsize="9">
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
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Attack Complexity (AC):</label> </bs:mco>
					<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary activeVector vector"> <input
								type="radio" name="ac" id="ac_l" autocomplete="off"
								 value="L" checked> Low (L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="ac" id="ac_h" autocomplete="off" value="H">
								High (H)
							</label>
						</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Attack Requirements (AT):</label> </bs:mco>
					<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary activeVector vector"> <input
								type="radio" name="at" id="at_n" autocomplete="off"
								 value="N" checked> None (N)
							</label><label class="btn btn-secondary vector"> <input
								type="radio" name="at" id="at_p" autocomplete="off"
								 value="P" checked> Present (P)
							</label>
						</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Privileges Required (PR):</label> </bs:mco>
					<bs:mco colsize="9">
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
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>User Interaction (UI):</label> </bs:mco>
					<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary activeVector vector"> <input
								type="radio" name="ui" id="ui_n" autocomplete="off"
								 value="N" checked> None (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="ui" id="ui_p" autocomplete="off" value="P">
								Passive (P)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="ui" id="ui_a" autocomplete="off" value="A">
								Active (A)
							</label>
						</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="12">
						<hr>
						<h4><b><center><u>Vulnerable System Impact Metrics</u></center></b></h4>
						<br/>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="3"><label>Confidentiality (VC):</label> </bs:mco>
					<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary activeVector vector"> <input
								type="radio" name="vc" id="vc_n" autocomplete="off"
								 value="N" checked> None (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="vc" id="vc_l" autocomplete="off" value="L"> Low
								(L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="vc" id="vc_h" autocomplete="off" value="H"> High
								(H)
							</label>
						</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Integrity (VI):</label></bs:mco>
					<bs:mco colsize="9">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="vi" id="vi_n" autocomplete="off"  value="N" checked>
									None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="vi" id="vi_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="vi" id="vi_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Availability (VA):</label> </bs:mco>
					<bs:mco colsize="9">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="va" id="va_n" autocomplete="off"
									 value="N" checked> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="va" id="va_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="va" id="va_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="12">
						<hr>
						<h4><b><center><u>Subsequent System Impact Metrics</u></center></b></h4>
						<br/>
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="3"><label>Confidentiality (SC):</label> </bs:mco>
					<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary activeVector vector"> <input
								type="radio" name="sc" id="sc_n" autocomplete="off"
								 value="N" checked> None (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="sc" id="sc_l" autocomplete="off" value="L"> Low
								(L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="sc" id="sc_h" autocomplete="off" value="H"> High
								(H)
							</label>
						</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Integrity (SI):</label></bs:mco>
					<bs:mco colsize="9">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="si" id="si_n" autocomplete="off"  value="N" checked>
									None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="si" id="si_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="si" id="si_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Availability (SA):</label></bs:mco>
					<bs:mco colsize="9">
							<div class="btn-group btn-group-toggle" data-toggle="buttons">
								<label class="btn btn-secondary activeVector vector"> <input
									type="radio" name="sa" id="sa_n" autocomplete="off"
									 value="N" checked> None (N)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="sa" id="sa_l" autocomplete="off" value="L"> Low (L)
								</label> <label class="btn btn-secondary vector"> <input type="radio"
									name="sa" id="sa_h" autocomplete="off" value="H"> High (H)
								</label>
							</div>
					</bs:mco>
				</bs:row>
				<hr>
				<bs:row>
					<bs:mco colsize="12">
				 <h2>Supplemental Metrics (Optional)</h2>
				</bs:mco>
			</bs:row>
				<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Safety (S):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="s" id="s_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="s" id="s_n" autocomplete="off" value="N"> Negligible (N) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="s" id="s_p" autocomplete="off" value="P"> Present (P) 
							</label> 
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Automatable (AU):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="au" id="au_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="au" id="au_n" autocomplete="off" value="N"> No (N) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="au" id="au_y" autocomplete="off" value="Y"> Yes (Y)
							</label> 
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Recovery (R):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="r" id="r_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="r" id="r_a" autocomplete="off" value="A"> Automate (A) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="r" id="r_u" autocomplete="off" value="U"> User (U) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="r" id="r_i" autocomplete="off" value="I"> Irrecoverable (I) 
							</label> 
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Value Density (V):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="v" id="v_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="v" id="v_d" autocomplete="off" value="D"> Diffuse (D) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="v" id="v_c" autocomplete="off" value="C"> Concentrated (C) 
							</label> 
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Vulnerability Response Effort (RE):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="re" id="re_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="re" id="re_l" autocomplete="off" value="L"> Low (L) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="re" id="re_m" autocomplete="off" value="M"> Moderate (M) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="re" id="re_h" autocomplete="off" value="H"> High (H) 
							</label> 
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Provider Urgency (U):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="u" id="u_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="u" id="u_c" autocomplete="off" value="C"> Clear (C)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="u" id="u_g" autocomplete="off" value="G"> Green (G)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="u" id="u_a" autocomplete="off" value="A"> Amber (A)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="u" id="u_r" autocomplete="off" value="R"> Red (R)
							</label> 
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="12">
				 <h2>Environmental (Modified Base Metrics)</h2>
				</bs:mco>
			</bs:row>
				<br/>
			<bs:row>
				<bs:mco colsize="12">
					<h4><b><center><u>Exploitability Metrics</u></center></b></h4>
					<br/>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="3"><label>Attack Vector (MAV):</label> </bs:mco>
				<bs:mco colsize="9">
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
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Attack Complexity (MAC):</label> </bs:mco>
				<bs:mco colsize="9">
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
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Attack Requirements (MAT):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="mat" id="mat_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mat" id="mat_n" autocomplete="off" value="N"> None (N) 
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mat" id="mat_p" autocomplete="off" value="P"> Present (P)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Privileges Required (MPR):</label> </bs:mco>
				<bs:mco colsize="9">
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
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>User Interaction (MUI):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="mui" id="mui_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mui" id="mui_n" autocomplete="off" value="N"> None (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mui" id="mui_p" autocomplete="off" value="P"> Passive (P)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mui" id="mui_a" autocomplete="off" value="A"> Active (A)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="12">
					<hr>
					<h4><b><center><u>Vulnerable System Impact Metrics</u></center></b></h4>
					<br/>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="3"><label> Confidentiality (MVC):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="mvc" id="mvc_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mvc" id="mvc_n" autocomplete="off" value="N"> None (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mvc" id="mvc_l" autocomplete="off" value="L"> Low (L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mvc" id="mvc_h" autocomplete="off" value="H"> High (H)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label> Integrity (MVI):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="mvi" id="mvi_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mvi" id="mvi_n" autocomplete="off" value="N"> None (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mvi" id="mvi_l" autocomplete="off" value="L"> Low (L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mvi" id="mvi_h" autocomplete="off" value="H"> High (H)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label> Availability (MVA):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="mva" id="mva_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mva" id="mva_n" autocomplete="off" value="N"> None (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mva" id="mva_l" autocomplete="off" value="L"> Low (L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="mva" id="mva_h" autocomplete="off" value="H"> High (H)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="12">
					<hr>
					<h4><b><center><u>Subsequent System Impact Metrics</u></center></b></h4>
					<br/>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="3"><label> Confidentiality (MSC):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="msc" id="msc_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msc" id="msc_n" autocomplete="off" value="N"> Negligible (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msc" id="msc_l" autocomplete="off" value="L"> Low (L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msc" id="msc_h" autocomplete="off" value="H"> High (H)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label> Integrity (MSI):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="msi" id="msi_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msi" id="msi_s" autocomplete="off" value="S"> Safety (S)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msi" id="msi_n" autocomplete="off" value="N"> Negligible (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msi" id="msi_l" autocomplete="off" value="L"> Low (L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msi" id="msi_h" autocomplete="off" value="H"> High (H)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label> Availability (MSA):</label> </bs:mco>
				<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="msa" id="msi_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msa" id="msa_s" autocomplete="off" value="S"> Safety (S)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msa" id="msa_n" autocomplete="off" value="N"> Negligible (N)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msa" id="msa_l" autocomplete="off" value="L"> Low (L)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="msa" id="msa_h" autocomplete="off" value="H"> High (H)
							</label>
						</div>
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="12">
				<hr>
				 <h2>Environmental (Security Requirements)</h2>
				</bs:mco>
			</bs:row>
				<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Confidentiality Requirement (CR):</label> </bs:mco>
				<bs:mco colsize="9">
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
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Integrity Requirement (IR):</label> </bs:mco>
				<bs:mco colsize="9">
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
				</bs:mco>
			</bs:row>
			<br/>
			<bs:row>
				<bs:mco colsize="3"><label>Availability Requirement (AR):</label> </bs:mco>
				<bs:mco colsize="9">
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
					</bs:mco>
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
					<hr>
					 <h2>Threat Metrics</h2>
					</bs:mco>
				</bs:row>
				<br/>
				<bs:row>
					<bs:mco colsize="3"><label>Exploit Maturity (E):</label></bs:mco>
					<bs:mco colsize="9">
						<div class="btn-group btn-group-toggle" data-toggle="buttons">
							<label class="btn btn-secondary vector"> <input
								type="radio" name="e" id="e_x" autocomplete="off"
								 value="X"> Not Defined (X)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="e" id="e_u" autocomplete="off" value="U"> Unreported (U)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="e" id="e_p" autocomplete="off" value="P"> Proof of Concept (P)
							</label> <label class="btn btn-secondary vector"> <input type="radio"
								name="e" id="e_a" autocomplete="off" value="A"> Attacked (A)
							</label>
						</div>
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