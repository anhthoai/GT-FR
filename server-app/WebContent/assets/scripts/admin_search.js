var Search = function () {

    return {
        // main function to initiate the module
        init: function () {
			var file_id=0, scale=1.0, org_w, org_h;
			var sel_photo=0;
			var sel_create_photo=0;
			var sel_edit_photo=0;
			var sel_create_admin_photo = 0;
			var sel_edit_admin_photo = 0;
			
			var admin_search_result = eval("");
			var update_admin_id = -1;
			
            App.initFancybox();
           
			var id_arr = new Array(0);  // if admin is checked then 1, else 0
			var admin_arr = new Array(0);
			var name_arr = new Array(0);
			var over_admin=-1;
						
			if ($.cookie('sidebar_closed') === '1'){
				$(".comname").hide();
			}
			else{
				$(".comname").show();
			}
			$("#close_btn").hide();
			$("#create_account_close_btn").hide();
			$("#chagne_account_close_btn").hide();
			$("#create_admin_close_btn").hide();
			$("#edit_admin_close_btn").hide();
			
			if (member != "agent"){
				$("#admin_search").hide();
				$("#create_new_admin_menu").hide();
			}
			if (member == "agent"){
				$("#create_new_user_menu").hide();
			}

			if (member != "admin"){
				$("#group_management").hide();
			}
			document.getElementById('table_body').innerHTML="";
			$("#ctrl_page").hide();
			
			$("#edit_admin_uploadphoto input").change(function(){
				var val = document.getElementById('edit_admin_filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				sel_edit_admin_photo = 1;
				// var formData = new FormData($(this)[0]);
				var formData = new FormData($("#edit_admin_uploadphoto")[0]);
				$.ajax({
					url: "PhotoUpload?userid="+userid,
					type: 'POST',
					data: formData,
					async: true,
					success: function (data) {
						$('#ajax').modal('hide');
						if (data=="error"){
							
						}
						else if (data=="no file"){
							
						}
						else{
							var obj = eval ("(" + data + ")");
							file_id = obj.file_id;
							scale = parseFloat(obj.scale);
							org_w = parseInt(obj.width);
							org_h = parseInt(obj.height);
							document.getElementById('input_img').src='data/'+userid+'/request/'+file_id+'_re.jpg';
							document.getElementById('input_img').onload=function(){
								$('#cropphoto').modal('show');
								FormImageCrop.init();				
							}
						}
						
					},
					cache: false,
					contentType: false,
					processData: false
				});

				return false;
			});
			
			$("#uploadphoto input").change(function(){
				var val = document.getElementById('filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				// var formData = new FormData($(this)[0]);
				var formData = new FormData($("#uploadphoto")[0]);
				$.ajax({
					url: "PhotoUpload?userid="+userid,
					type: 'POST',
					data: formData,
					async: true,
					success: function (data) {
						$('#ajax').modal('hide');
						if (data=="error"){
							
						}
						else if (data=="no file"){
							
						}
						else{
							var obj = eval ("(" + data + ")");
							file_id = obj.file_id;
							scale = parseFloat(obj.scale);
							org_w = parseInt(obj.width);
							org_h = parseInt(obj.height);
							document.getElementById('input_img').src='data/'+userid+'/request/'+file_id+'_re.jpg';
							document.getElementById('input_img').onload=function(){
								$('#cropphoto').modal('show');
								FormImageCrop.init();				
							}
						}
						
					},
					cache: false,
					contentType: false,
					processData: false
				});

				return false;
			});
			
			$("#create_admin_uploadphoto input").change(function(){
				var val = document.getElementById('create_admin_filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				sel_create_admin_photo = 1;
				// var formData = new FormData($(this)[0]);
				var formData = new FormData($("#create_admin_uploadphoto")[0]);
				$.ajax({
					url: "PhotoUpload?userid="+userid,
					type: 'POST',
					data: formData,
					async: true,
					success: function (data) {
						$('#ajax').modal('hide');
						if (data=="error"){
							sel_create_photo = 0;
						}
						else if (data=="no file"){
							
						}
						else{
							var obj = eval ("(" + data + ")");
							file_id = obj.file_id;
							scale = parseFloat(obj.scale);
							org_w = parseInt(obj.width);
							org_h = parseInt(obj.height);
							document.getElementById('input_img').src='data/'+userid+'/request/'+file_id+'_re.jpg';
							document.getElementById('input_img').onload=function(){
								$('#cropphoto').modal('show');
								FormImageCrop.init();				
							}
						}
						
					},
					cache: false,
					contentType: false,
					processData: false
				});

				return false;
			});
			
			$("#create_account_uploadphoto input").change(function(){
				var val = document.getElementById('create_account_filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				sel_create_photo = 1;
				// var formData = new FormData($(this)[0]);
				var formData = new FormData($("#create_account_uploadphoto")[0]);
				$.ajax({
					url: "PhotoUpload?userid="+userid,
					type: 'POST',
					data: formData,
					async: true,
					success: function (data) {
						$('#ajax').modal('hide');
						if (data=="error"){
							sel_create_photo = 0;
						}
						else if (data=="no file"){
							
						}
						else{
							var obj = eval ("(" + data + ")");
							file_id = obj.file_id;
							scale = parseFloat(obj.scale);
							org_w = parseInt(obj.width);
							org_h = parseInt(obj.height);
							document.getElementById('input_img').src='data/'+userid+'/request/'+file_id+'_re.jpg';
							document.getElementById('input_img').onload=function(){
								$('#cropphoto').modal('show');
								FormImageCrop.init();				
							}
						}
						
					},
					cache: false,
					contentType: false,
					processData: false
				});

				return false;
			});
			
			$("#chagne_account_uploadphoto input").change(function(){
				var val = document.getElementById('chagne_account_filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				sel_edit_photo = 1;
				// var formData = new FormData($(this)[0]);
				var formData = new FormData($("#chagne_account_uploadphoto")[0]);
				$.ajax({
					url: "PhotoUpload?userid="+userid,
					type: 'POST',
					data: formData,
					async: true,
					success: function (data) {
						$('#ajax').modal('hide');
						if (data=="error"){
							sel_edit_photo = 0;
						}
						else if (data=="no file"){
							
						}
						else{
							var obj = eval ("(" + data + ")");
							file_id = obj.file_id;
							scale = parseFloat(obj.scale);
							org_w = parseInt(obj.width);
							org_h = parseInt(obj.height);
							document.getElementById('input_img').src='data/'+userid+'/request/'+file_id+'_re.jpg';
							document.getElementById('input_img').onload=function(){
								$('#cropphoto').modal('show');
								FormImageCrop.init();				
							}
						}
						
					},
					cache: false,
					contentType: false,
					processData: false
				});

				return false;
			});

			$("form#search").submit(function(){
				
				if (member != "agent")
				{
					alert("You can't search admin");
					return;
				}
				
				var adminid = document.getElementById('adminid1').value.trim();
				var country = document.getElementById('country1').value.trim();
				var city = document.getElementById('city1').value.trim();
				var address = document.getElementById('address1').value.trim();
				var email = document.getElementById('email1').value.trim();

				var input_key="";
				if (adminid!=''){
					input_key+="adminid="+adminid;
				}
				if (country!=''){
					if (input_key!="") input_key+="&";
					input_key+="country="+country;
				}
				
				if (city!=''){
					if (input_key!="") input_key+="&";
					input_key+="city="+city;
				}
				if (address!=''){
					if (input_key!="") input_key+="&";
					input_key+="address="+address;
				}
				if (email!=''){
					if (input_key!="") input_key+="&";
					input_key+="email="+email;
				}
				if (sel_photo!=false){
					if (input_key!="") input_key+="&";
					input_key+="file_id="+file_id;
				}				
				
				$.ajax({
					url: url ="AgentSearch?userid="+userid+"&"+input_key+ "&mobile=0&searchId=" + searchId,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						// alert(data);
						if (data.trim()=="") {
							return;
						}
						var obj = eval ("(" + data + ")");
						admin_search_result = eval ("(" + data + ")");
						if (obj.searchlists=="none"){
							$("#alert-body").text("There is no result!!!");
							$('#alert').modal('show');
							
						}
						document.getElementById('table_body').innerHTML="";
						if (obj.searchlists!="none"){
							var tb_body="";
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var name = obj.searchlists[i].fullname;
								if (name==null || name=="null") name = " ";
								var email = obj.searchlists[i].email;
								if (email==null || email=="null") email = " ";
								var address = obj.searchlists[i].address;
								if (address==null || address=="null") address = " ";
								var city= obj.searchlists[i].city;
								if (city==null || city=="null") city = " ";
								var country = obj.searchlists[i].country;
								if (country==null || country =="null") country = " ";
								var user_id = obj.searchlists[i].user_id;
								if (user_id==null || user_id=="null") user_id = " ";
								var fileid = obj.searchlists[i].fileid;
								if (fileid==null || fileid=="null") fileid = " ";
								var editviewstr = "Edit";
								// var admins = obj.searchlists[i].agents;
								
								var	img_field = '<img class="res_face" src="'+'personimgs/'+fileid+".jpg"+'" alt=""/>';
							
								var result_tr='<tr class="res_tr" id="tr_'+id+'">'
													+'<td>'+img_field+'</td>'
													+'<td id="name_'+id+'">'+name+'</td>'
													+'<td id="email_'+id+'">'+email+'</td>'
													+'<td id="address_'+id+'">'+address+'</td>'
													+'<td id="city_'+id+'">'+city+'</td>'
													+'<td id="country_'+id+'">'+country+'</td>'
													+'<td id="user_id_'+id+'">'+user_id+'</td>'
													+'<td class="edit_td btn default btn-xs green-stripe" id="edit_'+id+'">'+editviewstr+'</td>'
													+'<td class="delete_td btn default btn-xs red-stripe" id="delete_'+id+'">Delete</td>'
											+'</tr>';
								tb_body+=result_tr;
							}
							// alert(document.getElementById('table_body').innerHTML);
							document.getElementById('table_body').innerHTML=tb_body;							
							
							$('.fileid_td').hide();
							
							$('.edit_td').on('mousedown', function () {
								var idstr = this.id;
								var sep_arr = idstr.split("_");
								var id = sep_arr[1];
								
								update_admin_id = id;
								var index = -1;
								for (var i=0;i<admin_search_result.searchlists.length;i++)
								{
									if (id == admin_search_result.searchlists[i].id)
									{
										index = i;
										break;
									}
								}
								// $("#person_img").attr("src","./personimgs/"+id+".jpg");
								// $('#person_view').modal('show');
								
								var name = admin_search_result.searchlists[i].fullname;
								if (name==null || name=="null") name = " ";
								var email = admin_search_result.searchlists[i].email;
								if (email==null || email=="null") email = " ";
								var address = admin_search_result.searchlists[i].address;
								if (address==null || address=="null") address = " ";
								var city= admin_search_result.searchlists[i].city;
								if (city==null || city=="null") city = " ";
								var country = admin_search_result.searchlists[i].country;
								if (country==null || country =="null") country = " ";
								var user_id = admin_search_result.searchlists[i].user_id;
								if (user_id==null || user_id=="null") user_id = " ";
								
								document.getElementById('edit_admin_fullname').value= name;
								document.getElementById('edit_admin_email').value=email;
								document.getElementById('edit_admin_address').value=address;
								document.getElementById('edit_admin_city_town').value=city;
								document.getElementById('edit_admin_country').value=country;
								document.getElementById('edit_admin_id').value=user_id;
								$("#edit_admin").modal("show");
							});	
							$('.delete_td').on('mousedown', function () {
								var idstr = this.id;
								var sep_arr = idstr.split("_");
								var id = sep_arr[1];
								var result = confirm("Do you really want to delete?");
								if (result) {
									$.ajax({
										url: url ="AgentDelete?userid="+userid+"&index="+id,
										type: 'POST',
										datatype: 'json',
										async: true,
										success: function (data) {
											// alert(data);
											if (data.trim()=="") {

												return;
											}
											var obj = eval ("(" + data + ")");
											if (obj.admindelete=="fail"){
												$("#alert-body").text("Failed to delete admin!");
												$('#alert').modal('show');
											}
											else if (obj.admindelete=="ok")
											{
												$("#alert-body").text("Successfully deleted admin!");
												$('#alert').modal('show');
												$('#tr_'+id+'').hide();
											}
										},
										cache: false,
										contentType: false,
										processData: false
									});
								}
								
							});		
						}	
					},
					cache: false,
					contentType: false,
					processData: false
				});
				searchId = -1;
				$('#btn_search').show();
				$('#btn_update').hide();
				return false;
			});// $("form#search").submit(function(){
			
			$("#sidbar_toggle").on('mousedown', function(){
				if ($.cookie('sidebar_closed') === '0'){
					$("#user_photo_pan").hide();
				}
				else{
					$("#user_photo_pan").show();
				}
			});
			$("#get_frapp").on('mousedown', function () {
				$("#download_FRAgent").modal("show");
			});
			
			$("#face_ok").on('mousedown', function () {
				if (sel_create_photo == 1)
				{
					setCreateAccountImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
					$("#create_account_close_btn").show();
					return;
				}
				if (sel_edit_photo == 1)
				{
					setEditAccountImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
					$("#chagne_account_close_btn").show();
					return;
				}
				if (sel_create_admin_photo == 1)
				{
					setCreateAgentImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
					$("#create_admin_close_btn").show();
					return;
				}
				if (sel_edit_admin_photo == 1)
				{
					setEditAgentImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
					$("#edit_admin_close_btn").show();
					return;
				}
				setsearchImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
				// document.getElementById('search_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
				$("#close_btn").show();
				sel_photo=1;
			});
			$("#close_btn").on('mousedown', function () {
				setsearchImg('assets/img/nophoto.png');
				// document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_photo=0;
				$("#close_btn").hide();
			});

			$("#create_admin_close_btn").on('mousedown', function () {
				setCreateAgentImg('assets/img/nophoto.png');
				// document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_create_admin_photo=0;
				$("#create_admin_close_btn").hide();
			});
			
			$("#edit_admin_close_btn").on('mousedown', function () {
				setEditAgentImg('assets/img/nophoto.png');
				// document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_edit_admin_photo=0;
				$("#edit_agent_close_btn").hide();
			});
			
			$("#create_account_close_btn").on('mousedown', function () {
				setCreateAccountImg('assets/img/nophoto.png');
				// document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_create_photo=0;
				$("#create_account_close_btn").hide();
			});
			
			$("#chagne_account_close_btn").on('mousedown', function () {
				setEditAccountImg('assets/img/nophoto.png');
				// document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_edit_photo=0;
				$("#chagne_account_close_btn").hide();
			});
			
			
			$("#view_ok").on('mousedown', function () {
				$("#person_view").modal('hide');
			});
			
			$("#crop_ok").on('mousedown', function () {
				
				var crop_info = parseInt(crop_x/scale)+"-"+parseInt(crop_y/scale)+"-"+parseInt(crop_w/scale)+"-"+parseInt(crop_h/scale)+"-0";
				if (crop_w==0&&crop_h==0){
					crop_info = "cancel";// +(org_w)+"-"+(org_h)+"-0";
				}
				// alert("userid="+userid+"&file_id="+file_id+"&crop_info="+crop_info);
				$('#ajax').modal('show');
				$('#scan_lab').html('Cropping Face...');
				$.ajax({
					url: "FaceDetect?userid="+userid+"&file_id="+file_id+"&crop_info="+crop_info,
					type: 'POST',
					async: true,
					success: function (data) {
						$('#ajax').modal('hide');
						if (data=="success"){
							$('#cropphoto').modal('hide');
							if (sel_create_photo == 1)
							{
								setCreateAccountImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								$("#create_account_close_btn").show();
								return;
							}
							if (sel_edit_photo == 1)
							{
								setEditAccountImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								$("#chagne_account_close_btn").show();
								return;
							}
							if (sel_create_agent_photo == 1)
							{
								setCreateAgentImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								$("#create_agent_close_btn").show();
								return;
							}
							if (sel_edit_agent_photo == 1)
							{
								setEditAgentImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								$("#edit_agent_close_btn").show();
								return;
							}
							setsearchImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
							// document.getElementById('search_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
							$("#close_btn").show();
							sel_photo=1;
							
						}
						else{
							$("#alert-body").text("Crop error");
							$('#cropphoto').modal('hide');
							$('#alert').modal('show');
						}
						
					},
					cache: false,
					contentType: false,
					processData: false
				});
			});

			$("#crop_cancel").on('mousedown', function () {
				$('#cropphoto').modal('hide');
			});

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
				sel_edit_photo = 0;
			});
			$("#save_account").on('mousedown', function () {
				
				
				var new_fullname = document.getElementById("fullname").value.trim();
// if (new_fullname==''){
// alert("Type fullname, please!");
// return;
// }
				var new_email = document.getElementById("email").value.trim();
// if (new_email==''){
// alert("Type email, please!");
// return;
// }
				var new_address = document.getElementById("address").value.trim();
// if (new_address==''){
// alert("Type address, please!");
// return;
// }
				var new_city = document.getElementById("city_town").value.trim();
// if (new_city==''){
// alert("Type city, please!");
// return;
// }
				var new_country = document.getElementById("country").value.trim();
// if (new_country==''){
// alert("Type country, please!");
// return;
// }
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
					url: "UserEdit?cur_userid="+userid+"&fullname="+new_fullname+"&email="+new_email+"&address="+new_address+"&city="+new_city+"&country="+new_country+"&userid="+new_userid+"&cur_password="+cur_password+"&new_password="+new_password + "&user-fileid=" + file_id,
					type: 'POST',
					async: true,
					success: function (data) {
						if (data=="success"){
							$('#responsive').modal('hide');
							$("#alert-body").text("Successfully changed your account! Please log in again!");
							$('#alert').modal('show');
							
							fullname = new_fullname;
							email = new_email;
							address = new_address;
							city = new_city;
							country = new_country;
							userid = new_userid;
							updateaccountinfo();
						}
						else if (data == "incorrect")
						{
							$("#alert-body").text("userid or password is incorrect!");
							$('#alert').modal('show');
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
				
				sel_edit_photo = 0;
				
			});
			
			function createaccountinfo(){
				document.getElementById("create-fullname").value=create-fullname;
				document.getElementById("create-email").value=create-email;
				document.getElementById("create-address").value=create-address;
				document.getElementById("create-city_town").value=create-city;
				document.getElementById("create-country").value=create-country;
				document.getElementById("create-userid").value=create-userid;
				document.getElementById("create-new_password").value="";
				document.getElementById("create-rpassword").value="";				
			}
			$("#create-close_account").on('mousedown', function () {
				$('#create-responsive').modal('hide');
				sel_create_photo = 0;
			});
			
			$("#create-save_account").on('mousedown', function () {
				if (member != "admin" && member != "agent")
				{
					alert("You can't create new user");
					$('#create-responsive').modal('hide');
					return;
				}
				sel_create_photo = 0;
				if (member == "admin")
				{
					var new_fullname = document.getElementById("create-fullname").value.trim();
// if (new_fullname==''){
// alert("Type fullname, please!");
// return;
// }
					var new_email = document.getElementById("create-email").value.trim();
// if (new_email==''){
// alert("Type email, please!");
// return;
// }
					var new_address = document.getElementById("create-address").value.trim();
// if (new_address==''){
// alert("Type address, please!");
// return;
// }
					var new_city = document.getElementById("create-city_town").value.trim();
// if (new_city==''){
// alert("Type city, please!");
// return;
// }
					var new_country = document.getElementById("create-country").value.trim();
// if (new_country==''){
// alert("Type country, please!");
// return;
// }
					var new_userid = document.getElementById("create-userid").value.trim();
					if (new_userid==''){
						alert("Type userid, please!");
						return;
					}
					var new_password = document.getElementById("create-new_password").value.trim();
					if (new_password==''){
						alert("Type new_password, please!");
						return;
					}				
					var rpassword = document.getElementById("create-rpassword").value.trim();
					if (rpassword==''){
						alert("Type confirm password, please!");
						return;
					}				
					if (new_password!=rpassword){
						alert("Re-type confirm password, please!");
						return;
					}
					$.ajax({
						url: "UserCreate?admin-id="+userid+"&create-userid="+new_userid+"&create-fullname="+new_fullname+"&create-email="+new_email+"&create-address="+new_address+"&create-city="+new_city+"&create-country="+new_country+"&create-new_password="+new_password + "&create-user-fileid=" + file_id,
						type: 'POST',
						async: true,
						success: function (data) {
							// alert(data);
							if (data=="success"){
								$('#create-responsive').modal('hide');
								$("#alert-body").text("Successfully created new account!");
								$('#alert').modal('show');
							}	
							else if (data=="duplicate"){
								$('#create-responsive').modal('hide');
								$("#alert-body").text("This user id already exists!");
								$('#alert').modal('show');
							}
							else{
								$('#create-responsive').modal('hide');
								$("#alert-body").text("Failed to create new account!");
								$('#alert').modal('show');													
							}
						},
						cache: false,
						contentType: false,
						processData: false
					});
				}
				else if (member == "agent")
				{
					var new_fullname = document.getElementById("create-fullname").value.trim();
// if (new_fullname==''){
// alert("Type fullname, please!");
// return;
// }
					var new_email = document.getElementById("create-email").value.trim();
// if (new_email==''){
// alert("Type email, please!");
// return;
// }
					var new_address = document.getElementById("create-address").value.trim();
// if (new_address==''){
// alert("Type address, please!");
// return;
// }
					var new_city = document.getElementById("create-city_town").value.trim();
// if (new_city==''){
// alert("Type city, please!");
// return;
// }
					var new_country = document.getElementById("create-country").value.trim();
// if (new_country==''){
// alert("Type country, please!");
// return;
// }
					var adminid_for_user = document.getElementById("create-user-admin-id").value.trim();
					if (adminid_for_user==''){
						alert("Type admin id for user, please!");
						return;
					}
					var new_userid = document.getElementById("create-userid").value.trim();
					if (new_userid==''){
						alert("Type userid, please!");
						return;
					}
					var new_password = document.getElementById("create-new_password").value.trim();
					if (new_password==''){
						alert("Type new_password, please!");
						return;
					}				
					var rpassword = document.getElementById("create-rpassword").value.trim();
					if (rpassword==''){
						alert("Type confirm password, please!");
						return;
					}				
					if (new_password!=rpassword){
						alert("Re-type confirm password, please!");
						return;
					}
					$.ajax({
						url: "UserCreate?admin-id="+adminid_for_user+"&create-userid="+new_userid+"&create-fullname="+new_fullname+"&create-email="+new_email+"&create-address="+new_address+"&create-city="+new_city+"&create-country="+new_country+"&create-new_password="+new_password + "&create-user-fileid=" + file_id,
						type: 'POST',
						async: true,
						success: function (data) {
							// alert(data);
							if (data=="success"){
								$('#create-responsive').modal('hide');
								$("#alert-body").text("Successfully created new account!");
								$('#alert').modal('show');
							}
							else if (data == "doesn't exist admin for creating user")
							{
								$('#create-responsive').modal('hide');
								$("#alert-body").text("doesn't exist admin for creating user!");
								$('#alert').modal('show');
							}
							else{
								$('#create-responsive').modal('hide');
								$("#alert-body").text("Failed to create new account!");
								$('#alert').modal('show');													
							}
						},
						cache: false,
						contentType: false,
						processData: false
					});
				}
				
			});
			
			$("#close-create-admin").on('mousedown', function () {
				$('#create-admin').modal('hide');
				sel_create_admin_photo = 0;
			});
			
			$("#save-create-admin").on('mousedown', function () {
				if (member != "agent")
				{
					alert("You can't ceate new admin");
					$('#create-admin').modal('hide');
					return;
				}
				sel_create_admin_photo = 0;
				var new_fullname = document.getElementById("create-admin-fullname").value.trim();
				if (new_fullname==''){
					alert("Type fullname, please!");
					return;
				}
				var new_email = document.getElementById("create-admin-email").value.trim();
				if (new_email==''){
					alert("Type email, please!");
					return;
				}
				var new_address = document.getElementById("create-admin-address").value.trim();
				if (new_address==''){
					alert("Type address, please!");
					return;
				}
				var new_city = document.getElementById("create-admin-city_town").value.trim();
				if (new_city==''){
					alert("Type city, please!");
					return;
				}
				var new_country = document.getElementById("create-admin-country").value.trim();
				if (new_country==''){
					alert("Type country, please!");
					return;
				}
				var new_adminid = document.getElementById("create-adminid").value.trim();
				if (new_adminid==''){
					alert("Type adminid, please!");
					return;
				}
				var new_password = document.getElementById("create-admin-new_password").value.trim();
				if (new_password==''){
					alert("Type new_password, please!");
					return;
				}				
				var rpassword = document.getElementById("create-admin-rpassword").value.trim();
				if (rpassword==''){
					alert("Type confirm password, please!");
					return;
				}				
				if (new_password!=rpassword){
					alert("Re-type confirm password, please!");
					return;
				}
				$.ajax({
					url: "AgentCreate?create-adminid="+new_adminid+"&create-agent-fullname="+new_fullname+"&create-admin-email="+new_email+"&create-admin-address="+new_address+"&create-admin-city="+new_city+
							"&create-admin-country="+new_country+"&create-admin-new_password="+new_password + "&create-admin-fileid=" + file_id,
					type: 'POST',
					async: true,
					success: function (data) {
						// alert(data);
						if (data=="success"){
							$('#create-admin').modal('hide');
							$("#alert-body").text("Successfully created new admin!");
							$('#alert').modal('show');
						}	
						else if (data=="duplicate"){
							$('#create-admin').modal('hide');
							$("#alert-body").text("This admin id already exists!");
							$('#alert').modal('show');
						}	
						else{
							$('#create-admin').modal('hide');
							$("#alert-body").text("Failed to create new admin!");
							$('#alert').modal('show');													
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
			});
			
			$("#edit_admin_close").on('mousedown', function () {
				$('#edit_admin').modal('hide');
				sel_edit_admin_photo = 0;
			});
			
			$("#edit_admin_save").on('mousedown', function () {
				
				var new_fullname = document.getElementById("edit_admin_fullname").value.trim();
				if (new_fullname==''){
					alert("Type fullname, please!");
					return;
				}
				var new_email = document.getElementById("edit_admin_email").value.trim();
				if (new_email==''){
					alert("Type email, please!");
					return;
				}
				var new_address = document.getElementById("edit_admin_address").value.trim();
				if (new_address==''){
					alert("Type address, please!");
					return;
				}
				var new_city = document.getElementById("edit_admin_city_town").value.trim();
				if (new_city==''){
					alert("Type city, please!");
					return;
				}
				var new_country = document.getElementById("edit_admin_country").value.trim();
				if (new_country==''){
					alert("Type country, please!");
					return;
				}
				var new_adminid = document.getElementById("edit_admin_id").value.trim();
				if (new_adminid==''){
					alert("Type adminid, please!");
					return;
				}
				var new_password = document.getElementById("edit_admin_new_password").value.trim();			
				var rpassword = document.getElementById("edit_admin_rpassword").value.trim();
				
				if (new_password!=rpassword){
					alert("Re-type confirm password, please!");
					return;
				}
					
				var input_key="";
				if (new_fullname!=''){
					input_key+="fullname="+new_fullname;
				}
				if (new_email!=''){
					if (input_key!="") input_key+="&";
					input_key+="email="+new_email;
				}
				if (new_address!=''){
					if (input_key!="") input_key+="&";
					input_key+="address="+new_address;
				}
				if (new_city!=''){
					if (input_key!="") input_key+="&";
					input_key+="city="+new_city;
				}
				if (new_country!=''){
					if (input_key!="") input_key+="&";
					input_key+="country="+new_country;
				}
				if (new_adminid!=''){
					if (input_key!="") input_key+="&";
					input_key+="adminid="+new_adminid;
				}
				if (new_password!=''){
					if (input_key!="") input_key+="&";
					input_key+="password="+new_password;
				}
				
				if (sel_edit_admin_photo == 1)
				{
					if (input_key!="") input_key+="&";
					input_key+="fildid="+file_id;
				}
				
				if (update_admin_id != -1)
				{
					if (input_key!="") input_key+="&";
					input_key+="id="+update_admin_id;
				}
				$.ajax({
					url: "AgentEdit?"+ input_key,
					type: 'POST',
					async: true,
					success: function (data) {
						// alert(data);
						if (data=="success"){
							$('#edit_admin').modal('hide');
							$("#alert-body").text("Successfully updated admin!");
							$('#alert').modal('show');
							
							sel_edit_admin_photo = 0;
						}					
						else{
							$('#edit_admin').modal('hide');
							$("#alert-body").text("Failed to update admin!");
							$('#alert').modal('show');													
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
			});
			
			function setsearchImg(url)
			{
				var img = document.getElementById('search_photo');
				img.src=url;
				if (img.width>img.height){
					img.style.width='196px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='196px';
				}
			}
			
			function setCreateAccountImg(url)
			{
				var img = document.getElementById('create_account_photo');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
			}
			
			function setCreateAgentImg(url)
			{
				var img = document.getElementById('create_admin_photo');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
			}
			
			function setEditAgentImg(url)
			{
				var img = document.getElementById('edit_admin_photo');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
			}
			
			function setEditAccountImg(url)
			{
				var img = document.getElementById('chagne_account_photo');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
			}
			
			function UpdateSearchList(){
				$.ajax({
					url: "SearchList",
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
							if (data.trim()=="") return;
						// alert(data);
						var obj = eval ("(" + data + ")"); 
						if (obj.searchlists!="none"){
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var editviewstr = "View";
								document.getElementById('edit_'+id).innerHTML=editviewstr;								
							}							
						}				
					},
					cache: false,
					contentType: false,
					processData: false
				});
				return;
			}
			
			
			document.onreadystatechange=function(){
				if(document.readyState == "complete"){					
// ScanAgents();
// setInterval(ScanAgents, 15000);
					$('#btn_update').hide();
				};
			};
			var sidebar_head="<li class='sidebar-toggler-wrapper'><div class='sidebar-toggler hidden-phone'></div></li>";
			var nofindcomp = "<li class='comli'><div class='comdiv' id='comdiv_1'><span class='comname' id='comname_1'>No Find Agents!</span></div></li>";			
			
		}		
		
    };

}();