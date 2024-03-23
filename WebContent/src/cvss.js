import { CVSS31, CVSS40 } from '@pandatix/js-cvss';


export default class CVSS {
	constructor(cvssString, is40){
		this.is40 = is40;
		this.vector = this.createCVSSObject(cvssString);
	}
	
	is31(){
		return typeof this.vector.BaseScore == "function"
	}
	convertCVSSSeverity(severity){
		switch(severity){
			case "Critical": return 5;
			case "High": return 4;
			case "Medium": return 3;
			case "Low": return 2;
			case "None": return 1;
			default: return 1;
		}
	}
	updateCVSSString(cvssString){
		this.vector = this.createCVSSObject(cvssString);		
	}
	createCVSSObject(cvssString){
		try{
			if(cvssString.indexOf("CVSS:4.0") == 0){
				return new CVSS40(cvssString);
			}else if(cvssString.indexOf("CVSS:3") == 0 ){
				return new CVSS31(cvssString);
			}else if(this.is40){
				return new CVSS40("CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:N/SC:N/SI:N/SA:N");
			}else{
				return new CVSS31("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:N");
			}
		}catch(e){
			if(this.is40){
				return new CVSS40("CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:N/SC:N/SI:N/SA:N");
			}else{
				return new CVSS31("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:N");
			}
		}
		
	}
	getCVSSSeverity(score) {
		const limits = {
			"None": [0, 0],
			"Low": [0.1, 3.9],
			"Medium": [4, 6.9],
			"High": [7, 8.9],
			"Critical": [9, 10]
		}
		if (score == 0) {
			return "None"
		}
		return Object.keys(limits).reduce((acc, key) => score >= limits[key][0] && score <= limits[key][1] ? key : acc, "None")
	}
	
	getCVSSScore(){
		if(this.is31()){
			let score = this.vector.BaseScore();
			const tmpScore = this.vector.TemporalScore();
			const envScore = this.vector.EnvironmentalScore();
			if(tmpScore != 0 && tmpScore < score){
				score = tmpScore
			}
			if(envScore != 0 && envScore < score){
				score = envScore
			}
			return score;
		}else{
			return this.vector.Score();
		}
	}
	
	updateCVSSScore(){
		let score = this.getCVSSScore();
		let severity = this.getCVSSSeverity(score);
		$("#score").html(score);
		$("#severity").html(severity);
		["Critical", "High", "Medium", "Low", "None"].forEach((a, b) => {
			$("#score").removeClass(a);
			$("#severity").removeClass(a);
		});
		$("#score").addClass(severity);
		$("#severity").addClass(severity);
		$("#cvssScore").val(score)
	}
	
	getVector(){
		//fixes a bug in the cvss library
		if(this.is31()){
			let vectorString = "CVSS:3.1";
			const required = ['AV', 'AC', 'PR', 'UI', 'S', 'C', 'I', 'A'];
			for( const r of required){
				vectorString = vectorString.concat("/", r, ":", this.vector.Get(r));
			}
			for (const [t] of Object.entries(this.vector._metrics)) {
				if( !required.includes(t)){
					const n = this.vector.Get(t);
					null != n && "X" != n && (vectorString = vectorString.concat("/", t, ":", n))
				}
			}
			return vectorString;
		}else{
			return this.vector.Vector()
		}
		
	}
	setUpCVSSModal(btnId, valueId, saveCallback){
		let cvssURL = "url:CVSS"
		let title = "CVSS 3.1"
		if(this.is40){
			cvssURL = "url:CVSS40"
			title = "CVSS 4.0"
		}
		let _this = this;
		$(`#${btnId}`).on("click", () => {
			
			$.confirm({
				title: title,
				content: cvssURL,
				columnClass: 'col-md-12',
				onContentReady: () => {
					let vectorString = $(`#${valueId}`).val();
					console.log(vectorString);
					//resize form so scroll works correclty
					setTimeout( () => {
						let height = $(".jconfirm-content-pane")[0].clientHeight;
						$(".cvss-content")[0].style.maxHeight=`${height - 15}px`
					}, 100);
					
					if(vectorString.trim() != ""){
						
						//This is to reset values when we have a project that has changed from 3.1 to 4.0 or vice versa
						if(_this.is40 && _this.is31()){
							this.updateCVSSString("");
						}else if(!_this.is40 && !_this.is31()){
							this.updateCVSSString("");
						}
						this.updateCVSSString(vectorString);
						let score = this.getCVSSScore();
						let severity = this.getCVSSSeverity(score);
						$("#modalScore").addClass(severity);
						$("#modalScore").html(score);
						$("#modalSeverity").addClass(severity);
						$("#modalSeverity").html(severity);
						setTimeout( () => {
							
							$(`#av_${this.vector.Get('AV').toLowerCase()}`).click()
							$(`#ac_${this.vector.Get('AC').toLowerCase()}`).click()
							$(`#pr_${this.vector.Get('PR').toLowerCase()}`).click()
							$(`#ui_${this.vector.Get('UI').toLowerCase()}`).click()
							
							$(`#e_${this.vector.Get('E').toLowerCase()}`).click()
							
							$(`#cr_${this.vector.Get('CR').toLowerCase()}`).click()
							$(`#ir_${this.vector.Get('IR').toLowerCase()}`).click()
							$(`#ar_${this.vector.Get('AR').toLowerCase()}`).click()
							
							$(`#mav_${this.vector.Get('MAV').toLowerCase()}`).click()
							$(`#mac_${this.vector.Get('MAC').toLowerCase()}`).click()
							$(`#mpr_${this.vector.Get('MPR').toLowerCase()}`).click()
							$(`#mui_${this.vector.Get('MUI').toLowerCase()}`).click()
							
								
							if(!_this.is40){
								$(`#s_${this.vector.Get('S').toLowerCase()}`).click()
								$(`#c_${this.vector.Get('C').toLowerCase()}`).click()
								$(`#i_${this.vector.Get('I').toLowerCase()}`).click()
								$(`#a_${this.vector.Get('A').toLowerCase()}`).click()
								
								
								$(`#rl_${this.vector.Get('RL').toLowerCase()}`).click()
								$(`#rc_${this.vector.Get('RC').toLowerCase()}`).click()
								
								$(`#ms_${this.vector.Get('MS').toLowerCase()}`).click()
								$(`#mc_${this.vector.Get('MC').toLowerCase()}`).click()
								$(`#mi_${this.vector.Get('MI').toLowerCase()}`).click()
								$(`#ma_${this.vector.Get('MA').toLowerCase()}`).click()
							}else{
								$(`#at_${this.vector.Get('AT').toLowerCase()}`).click()
								$(`#vc_${this.vector.Get('VC').toLowerCase()}`).click()
								$(`#vi_${this.vector.Get('VI').toLowerCase()}`).click()
								$(`#va_${this.vector.Get('VA').toLowerCase()}`).click()
								$(`#sc_${this.vector.Get('SC').toLowerCase()}`).click()
								$(`#si_${this.vector.Get('SI').toLowerCase()}`).click()
								$(`#sa_${this.vector.Get('SA').toLowerCase()}`).click()
								
								$(`#s_${this.vector.Get('S').toLowerCase()}`).click()
								$(`#au_${this.vector.Get('AU').toLowerCase()}`).click()
								$(`#r_${this.vector.Get('R').toLowerCase()}`).click()
								$(`#re_${this.vector.Get('RE').toLowerCase()}`).click()
								$(`#v_${this.vector.Get('V').toLowerCase()}`).click()
								$(`#u_${this.vector.Get('U').toLowerCase()}`).click()
								
								$(`#mvc_${this.vector.Get('MVC').toLowerCase()}`).click()
								$(`#mvi_${this.vector.Get('MVI').toLowerCase()}`).click()
								$(`#mva_${this.vector.Get('MVA').toLowerCase()}`).click()
								
								
								$(`#msc_${this.vector.Get('MSC').toLowerCase()}`).click()
								$(`#msi_${this.vector.Get('MSI').toLowerCase()}`).click()
								$(`#msa_${this.vector.Get('MSA').toLowerCase()}`).click()
								
								$(`#mat_${this.vector.Get('MAT').toLowerCase()}`).click()
								
								
							}
							
						},100);
					}

					$('.vector').on("click", function(event) {
						let el = this;
						$(el).parent().children().each((_index, e) => {
							$(e).removeClass("activeVector");
						});
						$(el).addClass("activeVector");
						setTimeout(() => {
							let av = $("input[name='av']:checked").val()
							let ac = $("input[name='ac']:checked").val()
							let pr = $("input[name='pr']:checked").val()
							let ui = $("input[name='ui']:checked").val()
							
							let e = $("input[name='e']:checked").val() || "X"
							let cr = $("input[name='cr']:checked").val() || "X"
							let ir = $("input[name='ir']:checked").val() || "X"
							let ar = $("input[name='ar']:checked").val() || "X"
							let mav = $("input[name='mav']:checked").val() || "X"
							let mac = $("input[name='mac']:checked").val() || "X"
							let mpr = $("input[name='mpr']:checked").val() || "X"
							let mui = $("input[name='mui']:checked").val() || "X"
							
							let commonVector ={
									AV: av,
									AC: ac,
									PR: pr,
									UI: ui, 
									E: e, 
									CR: cr, 
									IR: ir, 
									AR: ar, 
									MAV: mav, 
									MAC: mac, 
									MPR: mpr, 
									MUI: mui 
							}
							let cvssVector = {}
							let score = 0.0;
							let severity = "None";
							let cvssString="";
									
							
							if(!_this.is40){
								let c = $("input[name='c']:checked").val()
								let i = $("input[name='i']:checked").val()
								let a = $("input[name='a']:checked").val()
								let s = $("input[name='s']:checked").val()
								
								let rl = $("input[name='rl']:checked").val() || "X"
								let rc = $("input[name='rc']:checked").val() || "X"
								let ms = $("input[name='ms']:checked").val() || "X"
								let mc = $("input[name='mc']:checked").val() || "X"
								let mi = $("input[name='mi']:checked").val() || "X"
								let ma = $("input[name='ma']:checked").val() || "X"
								let cvss31Vector = {
									C: c, 
									I: i, 
									A: a, 
									S: s, 
									E: e, 
									RL: rl, 
									RC: rc, 
									MS: ms, 
									MC: mc, 
									MI: mi, 
									MA: ma	
								}
								cvssVector = {
									...cvss31Vector, 
									...commonVector
								}
								
								Object.keys(cvssVector).forEach( (a, _i) =>{
									_this.vector.Set(a,cvssVector[a]);
								});
								score = _this.getCVSSScore();
								severity = _this.getCVSSSeverity(score);
								let vectorString = _this.getVector();
								$("#modalCVSSString").val(vectorString);
								
							}else{
								let at = $("input[name='at']:checked").val() || "X"
								let vc = $("input[name='vc']:checked").val() || "X"
								let vi = $("input[name='vi']:checked").val() || "X"
								let va = $("input[name='va']:checked").val() || "X"
								let sc = $("input[name='sc']:checked").val() || "X"
								let si = $("input[name='si']:checked").val() || "X"
								let sa = $("input[name='sa']:checked").val() || "X"
								
								let s = $("input[name='s']:checked").val() || "X"
								let au = $("input[name='au']:checked").val() || "X"
								let r = $("input[name='r']:checked").val() || "X"
								let v = $("input[name='v']:checked").val() || "X"
								let re = $("input[name='re']:checked").val() || "X"
								let u = $("input[name='u']:checked").val() || "X"
								
								let mvc = $("input[name='mvc']:checked").val() || "X"
								let mvi = $("input[name='mvi']:checked").val() || "X"
								let mva = $("input[name='mva']:checked").val() || "X"
								
								
								let msc = $("input[name='msc']:checked").val() || "X"
								let msi = $("input[name='msi']:checked").val() || "X"
								let msa = $("input[name='msa']:checked").val() || "X"
								
								let mat = $("input[name='mat']:checked").val() || "X"
								let cvss40Vector = {
									AT: at, 
									VC: vc, 
									VI: vi, 
									VA: va, 
									SC: sc, 
									SI: si, 
									SA: sa, 
									S: s, 
									AU: au, 
									R: r, 
									V: v, 
									RE: re, 
									U: u,
									MVC: mvc, 
									MVI: mvi, 
									MVA: mva, 
									MSC: msc, 
									MSI: msi, 
									MSA: msa, 
									MAT: mat
								}
								cvssVector = {
									...cvss40Vector, 
									...commonVector
								}
								Object.keys(cvssVector).forEach( (a, _i) =>{
										_this.vector.Set(a,cvssVector[a]);
								});
								
								
								
								score = _this.getCVSSScore();
								severity = _this.getCVSSSeverity(score);
								$("#modalCVSSString").val(_this.vector.Vector());
								
							}
							
							["Critical", "High", "Medium", "Low", "None"].forEach((a, b) => {
								$("#modalScore").removeClass(a);
								$("#modalSeverity").removeClass(a);
							});
							$("#modalScore").addClass(severity);
							$("#modalScore").html(score);
							$("#modalSeverity").addClass(severity);
							$("#modalSeverity").html(severity);
						}, 200);

					});
				},
				buttons: {
					update: () =>{
						let cvssString = _this.getVector();
						saveCallback(cvssString, _this.getCVSSScore());
					},
					cancel: () => { }
				}
			})

		});
		
	}
}