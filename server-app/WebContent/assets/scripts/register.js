var Register = function () {

    return {
        //main function to initiate the module
        init: function () {
			var file_id=0, scale=1.0, org_w, org_h;
			var register_image_list = new Array();
			register_image_list.push(0);
			register_image_list.push(0);
			register_image_list.push(0);
			register_image_list.push(0);
			
			var sel_photo=0;
			var sel_photo1=0;
			var sel_photo2=0;
			var sel_photo3=0;
			var sel_create_photo=0;
			var sel_edit_photo=0;
			var sel_create_admin_photo = 0;
			
			var photo_id = -1;
			var register_sel_photo=0;
			var register_sel_photo1=0;
			var register_sel_photo2=0;
			var register_sel_photo3=0;
			
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
			$("#close_btn1").hide();
			$("#close_btn2").hide();
			$("#close_btn3").hide();
			$("#create_account_close_btn").hide();
			$("#chagne_account_close_btn").hide();
			$("#create_admin_close_btn").hide();
			
			if (member != "agent"){
				$("#admin_search").hide();
				$("#create_new_admin_menu").hide();
				$("#admin_row").hide();
			}

			if (member == "agent"){
				$("#create_new_user_menu").hide();
			}
			
			if (member != "admin"){
				$("#group_management").hide();
			}
			
			if (member == "admin"){
				$.ajax({
					url: url ="GroupSearch?adminid="+userid,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						//alert(data);
						if (data.trim()=="") {
							return;
						}
						var obj = eval ("(" + data + ")");
						if (obj.searchlists=="none"){
							$("#alert-body").text("There is no group!");
							$('#alert').modal('show');
							
						}
						if (obj.searchlists!="none"){
							document.getElementById("register_group_name").options.length = 0;
							$('#register_group_name')
					         .append($("<option></option>")
					                    .attr("value","")
					                    .text(""));
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var group_name = obj.searchlists[i].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								$('#register_group_name')
						         .append($("<option></option>")
						                    .attr("value",group_name)
						                    .text(group_name));
							}	
						}	

					},
					cache: false,
					contentType: false,
					processData: false
				});
			}
			else
			{
				$.ajax({
					url: url ="AgentSearch?userid="+userid,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						// alert(data);
						if (data.trim()=="") {
							return;
						}
						var obj = eval ("(" + data + ")");
						if (obj.searchlists=="none"){
							$("#alert-body").text("There is no admin!");
							$('#alert').modal('show');
							
						}
						if (obj.searchlists!="none"){
							for (var i=0;i<obj.searchlists.length;i++){
								var user_id = obj.searchlists[i].user_id;
								if (user_id==null || user_id=="null") user_id = " ";
								$('#register_admin_name')
						         .append($("<option></option>")
						                    .attr("value",user_id)
						                    .text(user_id));
							}
						}	

					},
					cache: false,
					contentType: false,
					processData: false
				});
				
				$.ajax({
					url: url ="GroupSearch?userid="+userid,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						//alert(data);
						if (data.trim()=="") {
							return;
						}
						var obj = eval ("(" + data + ")");
						if (obj.searchlists=="none"){
							$("#alert-body").text("There is no group!");
							$('#alert').modal('show');
							
						}
						if (obj.searchlists!="none"){
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var group_name = obj.searchlists[i].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								var adminid = obj.searchlists[i].adminid;
								if (adminid==null || adminid=="null") adminid = " ";
								$('#register_group_name')
						         .append($("<option></option>")
						                    .attr("value",group_name)
						                    .text(group_name));
							}	
						}	

					},
					cache: false,
					contentType: false,
					processData: false
				});
				
				$("#register_group_name").on('change',  function () {
					var register_group_name = document.getElementById("register_group_name").value.trim();
					if (register_group_name == "")
					{
						document.getElementById("register_admin_name").value = "";
					}
					else{
						$.ajax({
		    				url: url ="GroupSearch?userid="+userid+"&"+"group_name=" + register_group_name,
		    				type: 'POST',
		    				datatype: 'json',
		    				async: true,
		    				success: function (data) {
		    					//alert(data);
		    					if (data.trim()=="") {
		    						return;
		    					}
		    					var obj = eval ("(" + data + ")");
		    					if (obj.searchlists=="none"){
		    						$("#alert-body").text("There is no group");
		    						$('#alert').modal('show');
		    						
		    					}
		    					if (obj.searchlists!="none"){
		    						for (var i=0;i<obj.searchlists.length;i++){
		    							var id = obj.searchlists[i].id;
		    							var adminid = obj.searchlists[i].adminid;
		    							if (adminid==null || adminid=="null") adminid = " ";
		    							document.getElementById("register_admin_name").value = adminid;
		    						}
		    					}	
		    				},
		    				cache: false,
		    				contentType: false,
		    				processData: false
		    			});
					}
					
	            });
				
				$("#register_admin_name").on('change',  function () {
					var register_admin_name = document.getElementById("register_admin_name").value.trim();
					if (register_admin_name == "")
					{
						$.ajax({
		    				url: url ="GroupSearch?userid="+userid,
		    				type: 'POST',
		    				datatype: 'json',
		    				async: true,
		    				success: function (data) {
		    					//alert(data);
		    					if (data.trim()=="") {
		    						return;
		    					}
		    					var obj = eval ("(" + data + ")");
		    					if (obj.searchlists=="none"){
		    						$("#alert-body").text("There is no group");
		    						document.getElementById("register_group_name").options.length = 0;
		    						$('#alert').modal('show');
		    						
		    					}
		    					if (obj.searchlists!="none"){
		    						document.getElementById("register_group_name").options.length = 0;
		    						$('#register_group_name')
							         .append($("<option></option>")
							                    .attr("value","")
							                    .text(""));
		    						for (var i=0;i<obj.searchlists.length;i++){
		    							var id = obj.searchlists[i].id;
		    							var group_name = obj.searchlists[i].group_name;
		    							if (group_name==null || group_name=="null") group_name = " ";
		    							$('#register_group_name')
								         .append($("<option></option>")
								                    .attr("value",group_name)
								                    .text(group_name));
		    						}
		    					}	
		    				},
		    				cache: false,
		    				contentType: false,
		    				processData: false
		    			});
					}
					else{
						$.ajax({
		    				url: url ="GroupSearch?adminid="+register_admin_name,
		    				type: 'POST',
		    				datatype: 'json',
		    				async: true,
		    				success: function (data) {
		    					//alert(data);
		    					if (data.trim()=="") {
		    						return;
		    					}
		    					var obj = eval ("(" + data + ")");
		    					if (obj.searchlists=="none"){
		    						$("#alert-body").text("There is no group");
		    						document.getElementById("register_group_name").options.length = 0;
		    						$('#alert').modal('show');
		    						
		    					}
		    					if (obj.searchlists!="none"){
		    						document.getElementById("register_group_name").options.length = 0;
		    						$('#register_group_name')
							         .append($("<option></option>")
							                    .attr("value","")
							                    .text(""));
		    						for (var i=0;i<obj.searchlists.length;i++){
		    							var id = obj.searchlists[i].id;
		    							var group_name = obj.searchlists[i].group_name;
		    							if (group_name==null || group_name=="null") group_name = " ";
		    							$('#register_group_name')
								         .append($("<option></option>")
								                    .attr("value",group_name)
								                    .text(group_name));
		    						}
		    					}	
		    				},
		    				cache: false,
		    				contentType: false,
		    				processData: false
		    			});
					}
					
	            });
			}
			
			document.getElementById('table_body').innerHTML="";
			$("#ctrl_page").hide();
			
			$("#sidbar_toggle").on('mousedown', function(){
				if ($.cookie('sidebar_closed') === '0'){
					$("#user_photo_pan").hide();
				}
				else{
					$("#user_photo_pan").show();
				}
			});
			
			$("#uploadphoto input").change(function(){
				var val = document.getElementById('filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				sel_photo = 1;
				photo_id = 0;
				register_sel_photo = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#uploadphoto")[0]);
				$.ajax({
					url: "RegisterPhotoUpload?userid="+userid,
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

							register_image_list[0] = file_id;
							
							document.getElementById('input_img').src='data/'+userid+'/request/' + file_id + '_re.jpg';
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
			
			$("#uploadphoto1 input").change(function(){
				var val = document.getElementById('filename1').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				sel_photo1 = 1;
				photo_id = 1;
				register_sel_photo1 = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#uploadphoto1")[0]);
				$.ajax({
					url: "RegisterPhotoUpload?userid="+userid,
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
							
							register_image_list[1] = file_id;
							document.getElementById('input_img').src='data/'+userid+'/request/' + file_id + '_re.jpg';
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
			
			$("#uploadphoto2 input").change(function(){
				var val = document.getElementById('filename2').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				sel_photo2 = 1;
				photo_id = 2;
				register_sel_photo2 = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#uploadphoto2")[0]);
				$.ajax({
					url: "RegisterPhotoUpload?userid="+userid,
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

							register_image_list[2] = file_id;
							
							document.getElementById('input_img').src='data/'+userid+'/request/' + file_id + '_re.jpg';
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
			
			
			$("#uploadphoto3 input").change(function(){
				var val = document.getElementById('filename3').value;
				if (val=='') {
					$("#alert-body").text("Please add a full image!");
					$('#alert').modal('show');
					return false;
				}
				sel_photo3 = 1;
				photo_id = 3;
				register_sel_photo3 = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#uploadphoto3")[0]);
				$.ajax({
					url: "RegisterPhotoUpload?userid="+userid,
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

							register_image_list[3] = file_id;

							document.getElementById('input_img').src='data/'+userid+'/request/' + file_id + '_re.jpg';
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
				//var formData = new FormData($(this)[0]);
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
				//var formData = new FormData($(this)[0]);
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
				//var formData = new FormData($(this)[0]);
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

			$("form#register").submit(function(){
				
				var name = document.getElementById('person-name1').value.trim();
				var sex = document.getElementById('sex1').value.trim();
				var birthday = document.getElementById('birthday1').value.trim();
				var home = document.getElementById('home1').value.trim();
				var email = document.getElementById('email1').value.trim();
				var phone = document.getElementById('phone1').value.trim();
				var city = document.getElementById('city1').value.trim();
				var country = document.getElementById('country1').value.trim();
				var group_name = document.getElementById('register_group_name').value.trim();
				
				
				if (register_sel_photo == 0 && register_sel_photo1 == 0 && register_sel_photo2 == 0 && register_sel_photo3==0)
				{
					$("#alert-body").text("Please select photo!");
					$('#alert').modal('show');
					return false;
				}
				if (name==''&&sex==''&&birthday==''&&home==''&&email==''&&phone==''){
					$("#alert-body").text("Please input any field!");
					$('#alert').modal('show');
					return false;
				}		

				var input_key="";
				if (name!=''){
					input_key+="name="+name;
				}
				else{
					$("#alert-body").text("Please input person's name!");
					$('#alert').modal('show');
					return false;
				}

				//if (sex!=''){
				if (input_key!="") input_key+="&";
				input_key+="sex="+sex;
				//}
				//else{
				//	$("#alert-body").text("Please input person's sex!");
				///	$('#alert').modal('show');
				//	return false;
				//}
				var real_birth;
				if (birthday!=''){
					var sep_arr = birthday.split("-");
					if(sep_arr[0]!=null){
						if(sep_arr[0].length!=4){
							alert("please input birthday correctly!!!");
							return false;
						}
						var integer=parseInt(sep_arr[0], 10)
						if(integer>0){
						}
						else{
							alert("please input birthday Year correctly!!!");
							return false;
							
						}
						real_birth=integer;
					}
					else{
//						$("#alert-body").text("Please input person's birthday!");
//						$('#alert').modal('show');
//						return false;
						if (input_key!="") input_key+="&";
						input_key+="birthday="+'1900-01-01';
					}
					if(sep_arr[1]!=null){
						if(sep_arr[1].length>2){
							alert("please input birthday Month length correctly!!!");
							return false;
						}
						var integer=parseInt(sep_arr[1], 10)
						if(integer>0){
							if(integer>12){
								alert("please input birthday Month correctly!!!");
								return false;
								
							}
						}
						else{
							alert("please input birthday Month correctly!!!");
							return false;
							
						}
						real_birth=real_birth+"-"+integer;
					}
					else{
//						$("#alert-body").text("Please input person's birthday!");
//						$('#alert').modal('show');
//						return false;
						if (input_key!="") input_key+="&";
						input_key+="birthday="+'1900-01-01';
					}
					if(sep_arr[2]!=null){
						if(sep_arr[2].length>2){
							alert("please input birthday Day length correctly!!!");
							return false;
						}
						var integer=parseInt(sep_arr[2], 10)
						if(integer>0){
							if(integer>31){
								alert("please input birthday Day correctly!!!");
								return false;
							}
						}
						else{
							alert("please input birthday Day correctly!!!");
							return false;
							
						}
						real_birth=real_birth+"-"+integer;
					}
					else{
//						$("#alert-body").text("Please input person's birthday!");
//						$('#alert').modal('show');
//						return false;
						if (input_key!="") input_key+="&";
						input_key+="birthday="+'1900-01-01';
					}
					if(sep_arr[3]!=null){
						alert("please input birthday correctly!!!");
						return false;
					}
					if (input_key!="") input_key+="&";
					input_key+="birthday="+real_birth;
				}
				else{
//					$("#alert-body").text("Please input person's birthday!");
//					$('#alert').modal('show');
//					return false;
					if (input_key!="") input_key+="&";
					input_key+="birthday="+'1900-01-01';
				}
				
				
				//if (home!=''){
				if (input_key!="") input_key+="&";
				input_key+="home="+home;
//				}
//				else{
//					$("#alert-body").text("Please input person's home address!");
//					$('#alert').modal('show');
//					return false;
//				}
				if (email!=''){
					if (input_key!="") input_key+="&";
					input_key+="email="+email;
				}
				else{
					$("#alert-body").text("Please input person's email!");
					$('#alert').modal('show');
					return false;
				}
				if (phone!=''){
					if (input_key!="") input_key+="&";
					input_key+="phone="+phone;
				}
				else{
					$("#alert-body").text("Please input person's phone!");
					$('#alert').modal('show');
					return false;
				}
				
				//if (city!=''){
				if (input_key!="") input_key+="&";
				input_key+="city="+city;
//				}
//				else{
//					$("#alert-body").text("Please input person's city!");
//					$('#alert').modal('show');
//					return false;
//				}
				
				//if (country!=''){
				if (input_key!="") input_key+="&";
				input_key+="country="+country;
//				}
//				else{
//					$("#alert-body").text("Please input person's country!");
//					$('#alert').modal('show');
//					return false;
//				}
				

				if (input_key!="") input_key+="&";
				input_key+="file_id="+register_image_list[0];
				
				if (input_key!="") input_key+="&";
				input_key+="file_id1="+register_image_list[1];
				
				if (input_key!="") input_key+="&";
				input_key+="file_id2="+register_image_list[2];
				
				if (input_key!="") input_key+="&";
				input_key+="file_id3="+register_image_list[3];
				
				if (input_key!="") input_key+="&";
				input_key+="member="+member;
				
				
				if (member == "agent")
				{
					var adminid = document.getElementById('register_admin_name').value.trim();
					if (adminid!=''){
						if (input_key!="") input_key+="&";
						input_key+="adminid="+adminid;
					}
					else{
						$("#alert-body").text("Please input person's adminid!");
						$('#alert').modal('show');
						return false;
					}
				}
				else
				{
					if (adminid!=''){
						if (input_key!="") input_key+="&";
						input_key+="adminid="+userid;
					}
				}
				
				if (group_name!=''){
					if (input_key!="") input_key+="&";
					input_key+="group_name="+group_name;
				}
				else{
//					$("#alert-body").text("Please input person's group!");
//					$('#alert').modal('show');
//					return false;
					if (input_key!="") input_key+="&";
					input_key+="group_name=Nogroup";
				}
				
				$.ajax({
					url: url ="Register?userid="+userid+"&"+input_key+ "&mobile=0&registerId=" + registerId,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						//alert(data);
						if (data.trim()=="") {
							document.getElementById('person-name1').value= "";
							document.getElementById('sex1').value="";
							document.getElementById('birthday1').value="";
							document.getElementById('home1').value="";
							document.getElementById('email1').value="";
							document.getElementById('phone1').value="";
							setregisterImg('assets/img/nophoto_register.png');
							setregisterImg1('assets/img/nophoto_register.png');
							setregisterImg2('assets/img/nophoto_register.png');
							setregisterImg3('assets/img/nophoto_register.png');
							sel_photo=0;
							sel_photo1=0;
							sel_photo2=0;
							sel_photo3=0;
							return;
						}
						var obj = eval ("(" + data + ")");
						if (obj.registerlists=="fail"){
							$("#alert-body").text("Register failed!!!");
							$('#alert').modal('show');
							return;
						}
						if (obj.registerlists == "nofeature")
						{
							$("#alert-body").text("Register failed! Feature can't be extracted!");
							$('#alert').modal('show');
							return;
						}
						if (obj.registerlists=="ok"){
							$("#alert-body").text("Successfully registered human data on the server!!!");
							$('#alert').modal('show');
							
							document.getElementById('person-name1').value= "";
							document.getElementById('sex1').value="";
							document.getElementById('birthday1').value="";
							document.getElementById('home1').value="";
							document.getElementById('email1').value="";
							document.getElementById('phone1').value="";
							setregisterImg('assets/img/nophoto_register.png');
							setregisterImg1('assets/img/nophoto_register.png');
							setregisterImg2('assets/img/nophoto_register.png');
							setregisterImg3('assets/img/nophoto_register.png');
							sel_photo=0;
							sel_photo1=0;
							sel_photo2=0;
							sel_photo3=0;
							$("#close_btn").hide();
							$("#close_btn1").hide();
							$("#close_btn2").hide();
							$("#close_btn3").hide();
							register_sel_photo = 0;
							register_sel_photo1 = 0;
							register_sel_photo2 = 0;
							register_sel_photo3 = 0;
							
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
				registerId = -1;
				$('#btn_register').show();
				$('#btn_update').hide();
				return false;
			});//$("form#register").submit(function(){
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
				if (sel_photo == 1)
				{
					setregisterImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
					//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
					$("#close_btn").show();
					sel_photo = 0;
					return;
				}
				if (sel_photo1 == 1)
				{
					setregisterImg1('data/'+userid+'/request/'+file_id+'_cr.jpg');
					//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
					$("#close_btn1").show();
					sel_photo1 = 0;
					return;
				}
				if (sel_photo2 == 1)
				{
					setregisterImg2('data/'+userid+'/request/'+file_id+'_cr.jpg');
					//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
					$("#close_btn2").show();
					sel_photo2 = 0;
					return;
				}
				if (sel_photo3 == 1)
				{
					setregisterImg3('data/'+userid+'/request/'+file_id+'_cr.jpg');
					sel_photo3 = 0;
					//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
					$("#close_btn3").show();
					return;
				}
				
			});
			$("#close_btn").on('mousedown', function () {
				setregisterImg('assets/img/nophoto_register.png');
				//document.getElementById('register_photo').src='assets/img/nophoto_register.png';
				sel_photo=0;
				register_sel_photo = 0;
				$("#close_btn").hide();
			});
			
			$("#close_btn1").on('mousedown', function () {
				setregisterImg1('assets/img/nophoto_register.png');
				//document.getElementById('register_photo').src='assets/img/nophoto_register.png';
				sel_photo1=0;
				register_sel_photo1 = 0;
				$("#close_btn1").hide();
			});
			
			$("#close_btn2").on('mousedown', function () {
				setregisterImg2('assets/img/nophoto_register.png');
				//document.getElementById('register_photo').src='assets/img/nophoto_register.png';
				sel_photo2=0;
				register_sel_photo2 = 0;
				$("#close_btn2").hide();
			});
			
			$("#close_btn3").on('mousedown', function () {
				setregisterImg3('assets/img/nophoto_register.png');
				//document.getElementById('register_photo').src='assets/img/nophoto_register.png';
				sel_photo3=0;
				register_sel_photo3 = 0;
				$("#close_btn3").hide();
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
							document.getElementById('face_img').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
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
							if (sel_photo == 1)
							{
								setregisterImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#close_btn").show();
								sel_photo = 0;
								return;
							}
							if (sel_photo1 == 1)
							{
								setregisterImg1('data/'+userid+'/request/'+file_id+'_cr.jpg');
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#close_btn1").show();
								sel_photo1 = 0;
								return;
							}
							if (sel_photo2 == 1)
							{
								setregisterImg2('data/'+userid+'/request/'+file_id+'_cr.jpg');
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#close_btn2").show();
								sel_photo2 = 0;
								return;
							}
							if (sel_photo3 == 1)
							{
								setregisterImg3('data/'+userid+'/request/'+file_id+'_cr.jpg');
								sel_photo3 = 0;
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#close_btn3").show();
								return;
							}
							
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


			function ScanAgents(){
				$.ajax({
					url: "GetAgents",
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						var obj = eval ("(" + data + ")");
						
						//alert(data);
						var new_html=sidebar_head;
						
						var new_id_arr = new Array(0);
						var new_name_arr=new Array(0);
						var new_admin_arr=new Array(0);
						if (obj.admins!="none"){
							for (var i=0;i<obj.admins.length;i++){
								var admin = obj.admins[i].name;
								var j=0;
								for (j=0;j<admin_arr.length;j++){
									if (admin_arr[j]==admin) break; // 
								}
								
								new_admin_arr.push(admin);
								new_name_arr.push(obj.admins[i].name);
								if (j==admin_arr.length){
									if(obj.admins[i].state=="1"){
										new_html+="<li class='comli'><div class='comdiv' id='comdiv_"+(i+1)+"'><img src='assets/img/comchk.png' class='comimg' id='comimg_"+(i+1)+"'/><span class='comname' id='comname_"+(i+1)+"'>"+obj.admins[i].name+"</span></div></li>";
										new_id_arr.push(1);								
										
									}
									else{
										new_html+="<li class='comli'><div class='comdiv' id='comdiv_"+(i+1)+"'><img src='assets/img/com.png' class='comimg' id='comimg_"+(i+1)+"'/><span class='comname' id='comname_"+(i+1)+"'>"+obj.admins[i].name+"</span></div></li>";
										new_id_arr.push(0);		
									}
								}
								else{								
									new_id_arr.push(id_arr[j]);
									//alert(id_arr[j]);
									if (id_arr[j]==0){
										new_html+="<li class='comli'><div class='comdiv' id='comdiv_"+(i+1)+"'><img src='assets/img/com.png' class='comimg' id='comimg_"+(i+1)+"'/><span class='comname' id='comname_"+(i+1)+"'>"+obj.admins[i].name+"</span></div></li>";
									}
									else{
										new_html+="<li class='comli'><div class='comdiv' id='comdiv_"+(i+1)+"'><img src='assets/img/comchk.png' class='comimg' id='comimg_"+(i+1)+"'/><span class='comname' id='comname_"+(i+1)+"'>"+obj.admins[i].name+"</span></div></li>";
									}
								}																					
							}
						}
						id_arr = new Array(0);
						admin_arr = new Array(0);
						name_arr = new Array(0);
						for (i=0;i<new_id_arr.length;i++){
							id_arr.push(new_id_arr[i]);
							name_arr.push(new_name_arr[i]);
							admin_arr.push(new_admin_arr[i]);
						}						
						
						if (id_arr.length>0){
							document.getElementById("admins_sidebar").innerHTML=new_html;							
							
							$('.comdiv').on('mouseover', function () {
								if ($.cookie('sidebar_closed') === '1'){
									var id = this.id;
									var sep_arr = id.split("_");
									var num = sep_arr[1];
									$('#comname_'+num+'').show();
									over_admin = num;
								}
							});
							$('.comdiv').on('mouseout', function () {
								if ($.cookie('sidebar_closed') === '1'){
									var id = this.id;
									var sep_arr = id.split("_");
									var num = sep_arr[1];
									$('#comname_'+num+'').hide();
									over_admin =-1;
								}
							});
							
						}
						else
							document.getElementById("admins_sidebar").innerHTML =sidebar_head+nofindcomp;
						
						if ($.cookie('sidebar_closed') === '1'){
							$('.comname').hide();
							/*if (over_admin!=-1){
								$('#comname_'+over_admin).show();
							}*/
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
			
			function setregisterImg(url)
			{
				var img = document.getElementById('register_photo');
				img.src=url;
				if (img.width>img.height){
					img.style.width='196px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='196px';
				}
				register_sel_photo = 1;
			}
			
			function setregisterImg1(url)
			{
				var img = document.getElementById('register_photo1');
				img.src=url;
				if (img.width>img.height){
					img.style.width='196px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='196px';
				}
				register_sel_photo1 = 1;
			}
			
			function setregisterImg2(url)
			{
				var img = document.getElementById('register_photo2');
				img.src=url;
				if (img.width>img.height){
					img.style.width='196px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='196px';
				}
				register_sel_photo2 = 1;
			}
			
			function setregisterImg3(url)
			{
				var img = document.getElementById('register_photo3');
				img.src=url;
				if (img.width>img.height){
					img.style.width='196px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='196px';
				}
				register_sel_photo3 = 1;
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
			function UpdateRegisterList(){
				$.ajax({
					url: "RegisterList",
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
							if (data.trim()=="") return;
						//alert(data);
						var obj = eval ("(" + data + ")"); 
						if (obj.registerlists!="none"){
							for (var i=0;i<obj.registerlists.length;i++){
								var id = obj.registerlists[i].id;
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
					//ScanAgents();
					//setInterval(ScanAgents, 15000);
					$('#btn_update').hide();
				};
			};
			var sidebar_head="<li class='sidebar-toggler-wrapper'><div class='sidebar-toggler hidden-phone'></div></li>";
			var nofindcomp = "<li class='comli'><div class='comdiv' id='comdiv_1'><span class='comname' id='comname_1'>No Find Agents!</span></div></li>";			
			
		}		
		
    };

}();