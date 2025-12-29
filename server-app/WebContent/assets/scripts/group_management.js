var GroupManagement = function () {

    return {
        //main function to initiate the module
        init: function () {

			var group_search_result = eval("");
			var update_group_id = -1;
			
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

			if (member != "agent"){
				$("#admin_search").hide();
				$("#create_new_admin_menu").hide();
			}

			if (member == "agent"){
				$("#create_new_user_menu").hide();
			}
			
			document.getElementById('table_body').innerHTML="";
			$("#ctrl_page").hide();
			
			$("#edit_user_uploadphoto input").change(function(){
				var val = document.getElementById('edit_user_filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');

				var formData = new FormData($("#edit_user_uploadphoto")[0]);
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

			$("#btn_register").on('mousedown', function () {
				$("#register_group").modal("show");
			});

			$("#register_group_close").on('mousedown', function () {
				$("#register_group").modal("hide");
			});
			$("#register_group_save").on('mousedown', function () {
				var new_group_name = document.getElementById("register_group_name").value.trim();
				if (new_group_name==''){
					alert("Type group name, please!");
					return;
				}
				
				$.ajax({
					url: "GroupRegister?userid="+userid+"&register_group_name="+new_group_name,
					type: 'POST',
					async: true,
					success: function (data) {
						if (data=="success"){
							$('#register_group').modal('hide');
							$("#alert-body").text("Successfully registered group!");
							$('#alert').modal('show');
						}
						else{
							$('#register_group').modal('hide');
							$("#alert-body").text("Failed to register group!");
							$('#alert').modal('show');														
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
			});
			
			$("#edit_group_close").on('mousedown', function () {
				$("#edit_group").modal("hide");
			});
			$("#edit_group_save").on('mousedown', function () {
				var new_group_name = document.getElementById("edit_group_name").value.trim();
				if (new_group_name==''){
					alert("Type group name, please!");
					return;
				}
				var input_key="";
				if (new_group_name!=''){
					input_key+="group_name="+new_group_name;
				}
				if (update_group_id != -1)
				{
					if (input_key!="") input_key+="&";
					input_key+="id="+update_group_id;
				}
				
				$.ajax({
					url: "GroupUpdate?userid="+userid+"&"+input_key,
					type: 'POST',
					async: true,
					success: function (data) {
						if (data=="success"){
							$('#edit_group').modal('hide');
							$("#alert-body").text("Successfully updated group!");
							$('#alert').modal('show');
						}
						else{
							$('#edit_group').modal('hide');
							$("#alert-body").text("Failed to update group!");
							$('#alert').modal('show');														
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
			});
			
			
			$('#btn_search').click(function(e){
			    e.preventDefault();

			    var group_name = document.getElementById('search_group_name').value.trim();
				
				var input_key="";
				if (group_name!=''){
					input_key+="group_name="+group_name;
				}
				$.ajax({
					url: url ="GroupSearch?userid="+userid+"&"+input_key,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						//alert(data);
						if (data.trim()=="") {
							return;
						}
						var obj = eval ("(" + data + ")");
						group_search_result = eval ("(" + data + ")");
						if (obj.searchlists=="none"){
							$("#alert-body").text("There is no group!");
							$('#alert').modal('show');
							
						}
						document.getElementById('table_body').innerHTML="";
						if (obj.searchlists!="none"){
							var tb_body="";
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var group_name = obj.searchlists[i].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								var adminid = obj.searchlists[i].adminid;
								if (adminid==null || adminid=="null") adminid = " ";
								
								var editviewstr = "Edit";
								var result_tr='<tr class="res_tr" id="tr_'+id+'">'
									+'<td id="group_name_'+id+'">'+group_name+'</td>'
									+'<td id="adminid_'+id+'">'+adminid+'</td>'
									+'<td class="edit_td btn default btn-xs green-stripe" id="edit_'+id+'">'+editviewstr+'</td>'
									+'<td class="delete_td btn default btn-xs red-stripe" id="delete_'+id+'">Delete</td>'
												+'</tr>';
									tb_body+=result_tr;
							}
							document.getElementById('table_body').innerHTML=tb_body;							
							
							$('.edit_td').on('mousedown', function () {
								var idstr = this.id;
								var sep_arr = idstr.split("_");
								var id = sep_arr[1];
								update_group_id = id;
								var index = -1;
								for (var i=0;i<group_search_result.searchlists.length;i++)
								{
									if (id == group_search_result.searchlists[i].id)
									{
										index = i;
										break;
									}
								}

								var group_name = group_search_result.searchlists[index].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								var adminid = group_search_result.searchlists[index].adminid;
								if (adminid==null || adminid=="null") adminid = " ";
								
								document.getElementById('edit_group_name').value= group_name;
								$("#edit_group").modal("show");
							});	
							$('.delete_td').on('mousedown', function () {
								var idstr = this.id;
								var sep_arr = idstr.split("_");
								var id = sep_arr[1];
								var result = confirm("Do you really want to delete?");
								if (result) {
									$.ajax({
										url: url ="GroupDelete?userid="+userid+"&index="+id,
										type: 'POST',
										datatype: 'json',
										async: true,
										success: function (data) {
											//alert(data);
											if (data.trim()=="") {

												return;
											}
											var obj = eval ("(" + data + ")");
											if (obj.groupdelete=="fail"){
												$("#alert-body").text("Failed to delete group!");
												$('#alert').modal('show');
											}
											else if (obj.groupdelete=="ok")
											{
												$("#alert-body").text("Successfully deleted group!");
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
				return false;
			});
			
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
				setsearchImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
				//document.getElementById('search_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
				$("#close_btn").show();
				sel_photo=1;
			});
			$("#close_btn").on('mousedown', function () {
				setsearchImg('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_photo=0;
				$("#close_btn").hide();
			});

			$("#create_admin_close_btn").on('mousedown', function () {
				setCreateAgentImg('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_create_admin_photo=0;
				$("#create_admin_close_btn").hide();
			});
			
			$("#create_account_close_btn").on('mousedown', function () {
				setCreateAccountImg('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_create_photo=0;
				$("#create_account_close_btn").hide();
			});
			
			$("#chagne_account_close_btn").on('mousedown', function () {
				setEditAccountImg('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_edit_photo=0;
				$("#chagne_account_close_btn").hide();
			});
			
			$("#edit_user_close_btn").on('mousedown', function () {
				setEditUserImg('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_edit_user_photo=0;
				$("#edit_user_close_btn").hide();
			});
			
			$("#view_ok").on('mousedown', function () {
				$("#person_view").modal('hide');
			});
			
			$("#crop_ok").on('mousedown', function () {
				
				var crop_info = parseInt(crop_x/scale)+"-"+parseInt(crop_y/scale)+"-"+parseInt(crop_w/scale)+"-"+parseInt(crop_h/scale)+"-0";
				if (crop_w==0&&crop_h==0){
					crop_info = "cancel";//+(org_w)+"-"+(org_h)+"-0";
				}
				//alert("userid="+userid+"&file_id="+file_id+"&crop_info="+crop_info);
				$('#ajax').modal('show');
				$('#scan_lab').html('Cropping Face...');
				$.ajax({
					url: "FaceDetect?userid="+userid+"&file_id="+file_id+"&crop_info="+crop_info,
					type: 'POST',
					async: true,
					success: function (data) {
						$('#ajax').modal('hide');
						if (data=="success"){
//							document.getElementById('face_img').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
//							document.getElementById('face_img').onload=function(){
//								$('#cropphoto').modal('hide');
//								$('#facephoto').modal('show');								
//							}
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
							if (sel_create_admin_photo == 1)
							{
								setCreateAgentImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								$("#create_admin_close_btn").show();
								return;
							}
							if (sel_edit_user_photo == 1)
							{
								setEditUserImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								$("#edit_user_close_btn").show();
								return;
							}
							setsearchImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
							//document.getElementById('search_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
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
				
				sel_edit_photo = 0;
				var new_fullname = document.getElementById("fullname").value.trim();
//				if (new_fullname==''){
//					alert("Type fullname, please!");
//					return;
//				}
				var new_email = document.getElementById("email").value.trim();
//				if (new_email==''){
//					alert("Type email, please!");
//					return;
//				}
				var new_address = document.getElementById("address").value.trim();
//				if (new_address==''){
//					alert("Type address, please!");
//					return;
//				}
				var new_city = document.getElementById("city_town").value.trim();
//				if (new_city==''){
//					alert("Type city, please!");
//					return;
//				}
				var new_country = document.getElementById("country").value.trim();
//				if (new_country==''){
//					alert("Type country, please!");
//					return;
//				}
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
//					if (new_fullname==''){
//						alert("Type fullname, please!");
//						return;
//					}
					var new_email = document.getElementById("create-email").value.trim();
//					if (new_email==''){
//						alert("Type email, please!");
//						return;
//					}
					var new_address = document.getElementById("create-address").value.trim();
//					if (new_address==''){
//						alert("Type address, please!");
//						return;
//					}
					var new_city = document.getElementById("create-city_town").value.trim();
//					if (new_city==''){
//						alert("Type city, please!");
//						return;
//					}
					var new_country = document.getElementById("create-country").value.trim();
//					if (new_country==''){
//						alert("Type country, please!");
//						return;
//					}
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
							//alert(data);
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
//					if (new_fullname==''){
//						alert("Type fullname, please!");
//						return;
//					}
					var new_email = document.getElementById("create-email").value.trim();
//					if (new_email==''){
//						alert("Type email, please!");
//						return;
//					}
					var new_address = document.getElementById("create-address").value.trim();
//					if (new_address==''){
//						alert("Type address, please!");
//						return;
//					}
					var new_city = document.getElementById("create-city_town").value.trim();
//					if (new_city==''){
//						alert("Type city, please!");
//						return;
//					}
					var new_country = document.getElementById("create-country").value.trim();
//					if (new_country==''){
//						alert("Type country, please!");
//						return;
//					}
					var adminid_for_user = document.getElementById("create-user-agent-id").value.trim();
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
							//alert(data);
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
					url: "AgentCreate?create-adminid="+new_adminid+"&create-admin-fullname="+new_fullname+"&create-admin-email="+new_email+"&create-admin-address="+new_address+"&create-admin-city="+new_city+
							"&create-admin-country="+new_country+"&create-admin-new_password="+new_password + "&create-admin-fileid=" + file_id,
					type: 'POST',
					async: true,
					success: function (data) {
						//alert(data);
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
			
			
			$("#edit_user_close").on('mousedown', function () {
				$('#edit_user').modal('hide');
				sel_edit_user_photo = 0;
			});
			
			$("#edit_user_save").on('mousedown', function () {
				
				var new_fullname = document.getElementById("edit_user_fullname").value.trim();
				var new_email = document.getElementById("edit_user_email").value.trim();
				var new_address = document.getElementById("edit_user_address").value.trim();
				var new_city = document.getElementById("edit_user_city_town").value.trim();
				var new_country = document.getElementById("edit_user_country").value.trim();
				var new_userid = document.getElementById("edit_user_id").value.trim();
				if (new_userid==''){
					alert("Type userid, please!");
					return;
				}
				var new_password = document.getElementById("edit_user_new_password").value.trim();			
				var rpassword = document.getElementById("edit_user_rpassword").value.trim();
				
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
				if (new_userid!=''){
					if (input_key!="") input_key+="&";
					input_key+="userid="+new_userid;
				}
				if (new_password!=''){
					if (input_key!="") input_key+="&";
					input_key+="password="+new_password;
				}
				
				if (sel_edit_user_photo == 1)
				{
					if (input_key!="") input_key+="&";
					input_key+="fildid="+file_id;
				}
				
				if (update_user_id != -1)
				{
					if (input_key!="") input_key+="&";
					input_key+="id="+update_user_id;
				}
				$.ajax({
					url: "UserUpdate?"+ input_key,
					type: 'POST',
					async: true,
					success: function (data) {
						//alert(data);
						if (data=="success"){
							$('#edit_user').modal('hide');
							$("#alert-body").text("Successfully updated user!");
							$('#alert').modal('show');
							
							sel_edit_user_photo = 0;
						}					
						else{
							$('#edit_user').modal('hide');
							$("#alert-body").text("Failed to update user!");
							$('#alert').modal('show');													
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
			});
			
			
			function setEditUserImg(url)
			{
				var img = document.getElementById('edit_user_photo');
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
						//alert(data);
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
//					ScanAgents();
//					setInterval(ScanAgents, 15000);
					$('#btn_update').hide();
				};
			};
			var sidebar_head="<li class='sidebar-toggler-wrapper'><div class='sidebar-toggler hidden-phone'></div></li>";
			var nofindcomp = "<li class='comli'><div class='comdiv' id='comdiv_1'><span class='comname' id='comname_1'>No Find Agents!</span></div></li>";			
			
		}		
		
    };

}();