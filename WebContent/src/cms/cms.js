require('../scripts/fileupload/css/fileinput.css');
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs'   ;
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';
import 'bootstrap';
import '../scripts/fileupload/js/fileinput.min';
import 'jquery-ui';
import 'jquery-confirm';
import 'icheck-bootstrap'
import 'select2';

$(function(){
    let editor;
    $(".select2").select2();
    
	
    $("#sample").click(function(){
        let retest="false";
        if($('#doRetest').is(":checked"))
            retest="true";
        let team=$("#asmtTeam").val();
        let type=$("#asmtType").val();
        fetch(`checkReportValues?teamid=${team}&typeid=${type}`).then(resp => {
			if(resp.status == 202){
        		let qs = "test=test&team="+team + "&type="+type+"&retest=" + retest;
        		let win = window.open('../service/Report.pdf?'+ qs, '_blank');
			}else{
				$.alert(
						{
							title: "Error",
							type:"red",
							content: "There isn't a report with this combination of assessment type and team.",
							columnClass: 'small'
						}
					);
				}
		})
        });
    $("#addTemplate").click(function(){
        
        $.confirm({
            title:"Add New Template",
            content: 'url:cms?action=templateUpload',
            contentLoaded: function(){
               setTimeout(() => {
               }, 1000); 
            },
            buttons: {
				formSubmit: {
					text: "Save",
					action: function(){
						let data="action=templateCreate";
						data+="&name=" + $("#name").val();
						data+="&teamid="+$("#team").val();
						data+="&typeid="+$("#type").val();
						data+="&retest="+$("#retest").is(':checked');
						$.post("cms",data).done(function(resp){
							if(resp.result == "success"){
								console.log("saved");
								document.location = document.location;
							}else{
								$.alert({
									title: "Error!",
									content: resp.message
								})
							}
						}).error(function(){
							console.log("error");
						});
					}
					
				},
				cancel:function(){ this.close();}
            }
            
        });
    });
    $("[id^=tmpDel]").click(function(){
        let id = $(this).attr("id");
        id=id.replace("tmpDel","");
        $.confirm({
            title:"Confirm",
            content:"Are you sure you want to delete this template.",
            buttons:{
            formSubmit: {
                text:"Delete",
                action: function(){
                $.post("cms?action=templateDelete&id=" +id).done(function(resp){
                    if(resp.result == "success")
                        document.location=document.location;
                });
                }
            },
            cancel:function(){}
            }
            });
    });
    $("[id^=tmpEdit]").click(function(){
        let id = $(this).attr("id");
        id=id.replace("tmpEdit","");
        $.confirm({
            title:"Update Template",
            content: 'url:cms?action=templateUpload&id='+id,
            contentLoaded:function(){
                setTimeout(function(){
                $("#imgForm").show();
                //$("#team, #retest, #type").attr('disabled','disabled');
                }, 1000);
            },
            buttons:{
            formSubmit: {
                text: "Save", 
                action: function(){
                
                    let data="action=templateSave"
                    data+="&id=" + id;
                    data+="&name=" + $("#name").val();
                    data+="&teamid="+$("#team").val();
                    data+="&typeid="+$("#type").val();
                    data+="&retest="+$("#retest").is(':checked');
                    $.post("cms",data).done(function(resp){
                        if(resp.result == "success"){
							if($(".file-caption-name")[0].title != ""){
								console.log("Submit")
								$("#imgForm").submit();
								setTimeout( function(){
                            		document.location=document.location;
								}, 2000);
							}else{
                            	document.location=document.location;
							}
                        }else{
                            $.alert({
                                title: "Error!",
                                content: resp.message
                            });
                        }
                    }).error(function(){
                        console.log("error");
                    });
                }
            },
            /*"Upload File":function(){
                $("#imgForm").submit();
            },*/
            cancel: function(){}
            }
        });
    });
        $('#templates').DataTable({
            "paging": true,
            "lengthChange": false,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": true,
            "order": [[ 3, "asc" ], [ 4, "asc" ], [ 1, "asc" ]]
        });
        $("#cssUpdate").click(function(){
        
            let data = "action=updateCSS";
            data += "&fontname=" + $("#fontname").val();
            data += "&fontsize=" + $("#fontsize").val();
            data += "&headerSize=" + $("#headersize").val();
            data += "&footerSize=" + $("#footersize").val();
            if($("coverHeader").is(':checked'))
            data += "&headerCover=true";
            else
                data += "&headerCover=false";
            if($("coverFooter").is(':checked'))
            data += "&footerCover=true";
            else
                data += "&footerCover=false";
        
            $.post("cms", data).done(function(resp){
                
                document.location=document.location;
            });
            
        });
        $("#editCSS").click(function (){
            $("#cssModal").modal({	show: true,
            						keyboard: false,
            						backdrop: 'static'});
            if(!editor){
                editor = CodeMirror.fromTextArea(document.getElementById("css"), {
                    lineNumbers: true,
                    mode: "text/css",
                    matchBrackets: true,
                });
            }
        });
        
        $("#SaveCSS").click(function(){
            let data="action=updateCSS";
            data += "&css=" + encodeURIComponent(editor.getValue());
            $.post("cms", data).done(function(){
                document.location=document.location;
            });
            
        });
})
