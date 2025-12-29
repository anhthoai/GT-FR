var Result = function () {

    return {
        //main function to initiate the module
        init: function () {
			var file_id=0, scale=1.0, org_w, org_h;
			var sel_photo=0;
            App.initFancybox();
            var id_arr = new Array(0);
			var name_arr = new Array(0);
			var fileid_arr = new Array(0);
			var over_SuccessRqst=-1;
						
			if ($.cookie('sidebar_closed') === '1'){
				$(".resultname").hide();
			}
			else{
				$(".resultname").show();
			}
			$("#close_btn").hide();
			document.getElementById('table_body').innerHTML="";
			$("#ctrl_page").hide();
			$("#sim_field").hide();

			$("#get_mobile").on('mousedown', function () {
				$("#download_mobile").modal("show");
			});
		
			function BackSet(){
				var new_html=sidebar_head;
				new_html+="<li class='resultli'>"+
									"<div class='resultdiv' id='resultdiv_1'>"+
										'<img class="res_back" src="assets/img/back.png" alt=""/>' +
										"<span class='comname' id='resultname_1'>Request ID = "+requestid+"</span>"+
									"</div></li>";
				
				document.getElementById("SuccessRqsts_sidebar").innerHTML=new_html;							
							
				$('.resultdiv').on('mouseover', function () {
					if ($.cookie('sidebar_closed') === '1'){
						var id = this.id;
						var sep_arr = id.split("_");
						var num = sep_arr[1];
						$('#resultname_'+num+'').show();
						over_SuccessRqst = num;
					}
					
				});
				$('.resultdiv').on('mouseout', function () {
					if ($.cookie('sidebar_closed') === '1'){
						var id = this.id;
						var sep_arr = id.split("_");
						var num = sep_arr[1];
						$('#resultname_'+num+'').hide();
						over_SuccessRqst =-1;
					}
				});
				$('.resultdiv').on('mousedown', function () {
					var id = this.id;
					var sep_arr = id.split("_");
					requestid = parseInt(sep_arr[1]);
					//var resultURL = "./order.jsp";
					//window.location.href = resultURL;
					
					//GetResult(requestid);
				});			
				
			}
			function GetResult(rqstid){
				$.ajax({
					url: "GetResult?userid="+userid+"&requestid="+rqstid,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						//alert(data);
						//return;
						var obj = eval ("(" + data + ")"); 
						if (obj.results!="none"){
							var tb_body="";
							//if (sel_photo==false) $("#sim_field").hide();
							//else $("#sim_field").show();
							var result_tr ="";
							for (var i=0;i<obj.results.length;i++){
								var id = obj.results[i].id;
								var name = obj.results[i].name;
								var age = obj.results[i].age;
								var address = obj.results[i].address;
								var state = obj.results[i].state;
								var city = obj.results[i].city;
								var knowledge = obj.results[i].knowledge;
								var photoid = obj.results[i].photoid;
								var fileno = obj.results[i].fileno;
								var foliono = obj.results[i].foliono;
								var similarity = obj.results[i].similarity;
								var admin =  obj.results[i].admin;
								if (similarity == 0){
									$("#sim_field").hide();
									result_tr +='<tr class="res_tr" id="tr_'+id+'">'+
										'<td><img class="res_face" id="photo_'+id+'" src="'+'data/'+userid+'/result/' + rqstid +'/'+photoid+'.jpg' +'" alt=""/></td>'+
										'<td id="name_'+id+'">'+name+'</td>'+
										'<td id="age_'+id+'">'+age+'</td>'+
										'<td id="state_'+id+'">'+state+'</td>'+
										'<td id="city_'+id+'">'+city+'</td>'+
										'<td id="add_'+id+'">'+address+'</td>'+
										'<td id="know_'+id+'">'+knowledge+'</td>'+
										'<td id="file_'+id+'">'+fileno+'</td>'+
										'<td id="foliono_'+id+'">'+foliono+'</td>'+
										'<td id="admin_'+id+'">'+admin+'</td>'+
									'</tr>';
									
								}else{
									$("#sim_field").show();
									result_tr +='<tr class="res_tr" id="tr_'+id+'">'+
										'<td><img class="res_face" id="photo_'+id+'" src="'+'data/'+userid+'/result/' + requestid +'/'+photoid+'.jpg' +'" alt=""/></td>'+
										'<td id="name_'+id+'">'+name+'</td>'+
										'<td id="age_'+id+'">'+age+'</td>'+
										'<td id="state_'+id+'">'+state+'</td>'+
										'<td id="city_'+id+'">'+city+'</td>'+
										'<td id="add_'+id+'">'+address+'</td>'+
										'<td id="know_'+id+'">'+knowledge+'</td>'+
										'<td id="file_'+id+'">'+fileno+'</td>'+
										'<td id="foliono_'+id+'">'+foliono+'</td>'+
										'<td id="admin_'+id+'">'+admin+'</td>'+
										'<td id="similarity_'+id+'">'+similarity+'</td>'+
									'</tr>';
								}
								//alert(result_tr);
								
							}
							tb_body=result_tr;
							document.getElementById('table_body').innerHTML=tb_body;
							
							$('.res_tr').on('mousedown', function () {
								var idstr = this.id;
								var sep_arr = idstr.split("_");
								var id = sep_arr[1];
								
								var name = document.getElementById('name_'+id).innerHTML.trim();
								var age = document.getElementById('age_'+id).innerHTML.trim();
								var state = document.getElementById('state_'+id).innerHTML.trim();
								var city = document.getElementById('city_'+id).innerHTML.trim();
								var address = document.getElementById('add_'+id).innerHTML.trim();
								var knowledge = document.getElementById('know_'+id).innerHTML.trim();
								var file_no = document.getElementById('file_'+id).innerHTML.trim();
								var folio_no = document.getElementById('foliono_'+id).innerHTML.trim();
								
								document.getElementById('view_img').src=document.getElementById('photo_'+id).src;
								document.getElementById('name_lab').innerHTML="Name : "+name;
								document.getElementById('age_lab').innerHTML="Age : "+age;
								document.getElementById('state_lab').innerHTML="State : "+state;
								document.getElementById('city_lab').innerHTML="City : "+city;
								document.getElementById('add_lab').innerHTML="Address : "+address;
								document.getElementById('fileno_lab').innerHTML="File No : "+file_no;
								document.getElementById('foliono_lab').innerHTML="Folio No : "+folio_no;
								document.getElementById('know_lab').innerHTML="Knowledge : "+knowledge;
								$('#viewperson').modal('show');										
									
							});							
						}else{
							document.getElementById('table_body').innerHTML="";
						}
						
					},
					cache: false,
					contentType: false,
					processData: false
				});
			}
			
			function updateaccountinfo(){
				document.getElementById("fullname").value=fullname;
				document.getElementById("email").value=email;
				document.getElementById("address").value=address;
				document.getElementById("city_town").value=city;
				document.getElementById("country").value=country;
				document.getElementById("userid").value=userid;
				
				document.getElementById("cur_password").value="";
				document.getElementById("new_password").value="";
				document.getElementById("rpassword").value="";				
			}
			$("#close_account").on('mousedown', function () {
				updateaccountinfo();
				$('#responsive').modal('hide');
			});
			$("#save_account").on('mousedown', function () {
				var new_fullname = document.getElementById("fullname").value.trim();
				if (new_fullname==''){
					alert("Type fullname, please!");
					return;
				}
				var new_email = document.getElementById("email").value.trim();
				if (new_email==''){
					alert("Type email, please!");
					return;
				}
				var new_address = document.getElementById("address").value.trim();
				if (new_address==''){
					alert("Type address, please!");
					return;
				}
				var new_city = document.getElementById("city_town").value.trim();
				if (new_city==''){
					alert("Type city, please!");
					return;
				}
				var new_country = document.getElementById("country").value.trim();
				if (new_country==''){
					alert("Type country, please!");
					return;
				}
				var new_userid = document.getElementById("userid").value.trim();
				if (new_userid==''){
					alert("Type userid, please!");
					return;
				}
				var cur_password = document.getElementById("cur_password").value.trim();
				if (cur_password==''){
					alert("Type current password, please!");
					return;
				}
				var new_password = document.getElementById("new_password").value.trim();
				if (new_password==''){
					alert("Type new_password, please!");
					return;
				}				
				var rpassword = document.getElementById("rpassword").value.trim();
				if (rpassword==''){
					alert("Type confirm password, please!");
					return;
				}				
				if (new_password!=rpassword){
					alert("Re-type confirm password, please!");
					return;
				}
				
				$.ajax({
					url: "UserEdit?cur_userid="+userid+"&fullname="+new_fullname+"&email="+new_email+"&address="+new_address+"&city="+new_city+"&country="+new_country+"&userid="+new_userid+"&cur_password="+cur_password+"&new_password="+new_password,
					type: 'POST',
					async: true,
					success: function (data) {
						if (data=="success"){
							$('#responsive').modal('hide');
							$("#alert-body").text("Successfully changed your account!");
							$('#alert').modal('show');
							
							fullname = new_fullname;
							email = new_email;
							address = new_address;
							city = new_city;
							country = new_country;
							userid = new_userid;
							updateaccountinfo();
						}					
						else{
							$('#responsive').modal('hide');
							$("#alert-body").text("Failed change of your account!");
							$('#alert').modal('show');							
							updateaccountinfo();							
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
			});

			document.onreadystatechange=function(){
				if(document.readyState == "complete"){					
					//var INTERVAL_SEC = 1000 >> 0; 
					//document.getElementById("SuccessRqsts_sidebar").innerHTML =nofindcomp;
					BackSet();
					//setInterval(ScanSuccessRqst, 10000);	
					GetResult(requestid);
				};
			};
			var sidebar_head="<li class='sidebar-toggler-wrapper'><div class='sidebar-toggler hidden-phone'></div></li>";
			var nofindcomp = "<li class='resultli'><div class='resultdiv' id='resultdiv_1'><span class='resultname' id='resultname_1'>No Find Results!</span></div></li>";			
			
		}		
		
    };

}();