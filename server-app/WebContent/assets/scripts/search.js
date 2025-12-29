var Search = function () {

    return {
        //main function to initiate the module
        init: function () {
			var file_id=0, scale=1.0, org_w, org_h;
			var sel_photo=0;
			var sel_photo1=0;
			var sel_photo2=0;
			var sel_photo3=0;
			var sel_create_photo=0;
			var sel_edit_photo=0;
			var sel_create_admin_photo = 0;
			
			var photo_id = -1;
			var edit_sel_photo=0;
			var edit_sel_photo1=0;
			var edit_sel_photo2=0;
			var edit_sel_photo3=0;
			
			var edit_image_list = new Array();
			edit_image_list.push(0);
			edit_image_list.push(0);
			edit_image_list.push(0);
			edit_image_list.push(0);
			
			var update_person_id = -1;
			
            App.initFancybox();
           
			var id_arr = new Array(0);  // if admin is checked then 1, else 0
			var admin_arr = new Array(0);
			var name_arr = new Array(0);
			var over_admin=-1;
			
			var search_result = eval("");
						
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
			
			$("#edit_person_close_btn").hide();
			$("#edit_person_close_btn1").hide();
			$("#edit_person_close_btn2").hide();
			$("#edit_person_close_btn3").hide();
			
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
			
			if (member == "admin"){
				$("#admin_row").hide();
				$("#edit_admin_row").hide();
				$.ajax({
					url: url ="GroupSearch?userid="+userid + "&adminid="+userid,
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
							var tb_body="";
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var group_name = obj.searchlists[i].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								$('#search_group_name')
						         .append($("<option></option>")
						                    .attr("value",group_name)
						                    .text(group_name));
								$('#edit_group_name')
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
				$("#admin_row").show();
				$("#edit_admin_row").show();
				document.getElementById('search_admin_name').disabled = true;
				document.getElementById('edit_admin_name').disabled = true;
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
							var tb_body="";
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var group_name = obj.searchlists[i].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								var adminid = obj.searchlists[i].adminid;
								if (adminid==null || adminid=="null") adminid = " ";
								$('#search_group_name')
						         .append($("<option></option>")
						                    .attr("value",group_name)
						                    .text(group_name));
								$('#edit_group_name')
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
				
				$("#search_group_name").on('change',  function () {
					var search_group_name = document.getElementById("search_group_name").value.trim();
					if (search_group_name == "" || search_group_name == "Nogroup")
					{
						document.getElementById("search_admin_name").value = "";
					}
					else{
						$.ajax({
		    				url: url ="GroupSearch?userid="+userid+"&"+"group_name=" + search_group_name,
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
		    							document.getElementById("search_admin_name").value = adminid;
		    						}
		    					}	
		    				},
		    				cache: false,
		    				contentType: false,
		    				processData: false
		    			});
					}
					
	            });
				
				$("#edit_group_name").on('change',  function () {
					var edit_group_name = document.getElementById("edit_group_name").value.trim();
					if (edit_group_name == "")
					{
						document.getElementById("edit_admin_name").value = "";
					}
					else{
						$.ajax({
		    				url: url ="GroupSearch?userid="+userid+"&"+"group_name=" + edit_group_name,
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
		    							document.getElementById("edit_admin_name").value = adminid;
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
			
			$("#edit_person_uploadphoto input").change(function(){
				var val = document.getElementById('edit_person_filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}9
				sel_photo = 1;
				photo_id = 0;
				edit_sel_photo = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#edit_person_uploadphoto")[0]);
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
							
							edit_image_list[0] = file_id;
							
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
			
			$("#edit_person_uploadphoto1 input").change(function(){
				var val = document.getElementById('edit_person_filename1').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				sel_photo1 = 1;
				photo_id = 1;
				edit_sel_photo1 = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#edit_person_uploadphoto1")[0]);
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

							edit_image_list[1] = file_id;
							
							
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
			
			$("#edit_person_uploadphoto2 input").change(function(){
				var val = document.getElementById('edit_person_filename2').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				sel_photo2 = 1;
				photo_id = 2;
				edit_sel_photo2 = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#edit_person_uploadphoto2")[0]);
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

							edit_image_list[2] = file_id;

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
			
			
			$("#edit_person_uploadphoto3 input").change(function(){
				var val = document.getElementById('edit_person_filename3').value;
				if (val=='') {
					$("#alert-body").text("Please add a full image!");
					$('#alert').modal('show');
					return false;
				}
				sel_photo3 = 1;
				photo_id = 3;
				edit_sel_photo3 = 0;
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
				var formData = new FormData($("#edit_person_uploadphoto3")[0]);
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

							edit_image_list[3] = file_id;
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
			
			
			$("#uploadphoto input").change(function(){
				var val = document.getElementById('filename').value;
				if (val=='') {
					$("#alert-body").text("Please add a face image!");
					$('#alert').modal('show');
					return false;
				}
				$('#ajax').modal('show');
				$('#scan_lab').html('Uploading Photo...');
				//var formData = new FormData($(this)[0]);
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

			$("form#search").submit(function(){
				
				var name = document.getElementById('person-name1').value.trim();
				var sex = document.getElementById('sex1').value.trim();
				var birthday = document.getElementById('birthday1').value.trim();
				var home = document.getElementById('home1').value.trim();
				var email = document.getElementById('email1').value.trim();
				var phone = document.getElementById('phone1').value.trim();
				var city = document.getElementById('city1').value.trim();
				var country = document.getElementById('country1').value.trim();
				var group_name = document.getElementById('search_group_name').value.trim();
//				
//				if (name==''&&sex==''&&birthday==''&&home==''&&email==''&&phone==''&&sel_photo==false){
//					$("#alert-body").text("Please input any field or select face photo!");
//					$('#alert').modal('show');
//					return false;
//				}		
				var input_key="";
				if (name!=''){
					input_key+="name="+name;
				}
				if (sex!=''){
					if (input_key!="") input_key+="&";
					input_key+="sex="+sex;
				}

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
					if(sep_arr[3]!=null){
						alert("please input birthday correctly!!!");
						return false;
					}
					if (input_key!="") input_key+="&";
					input_key+="birthday="+real_birth;
				}
				
				
				if (home!=''){
					if (input_key!="") input_key+="&";
					input_key+="home="+home;
				}
				if (email!=''){
					if (input_key!="") input_key+="&";
					input_key+="email="+email;
				}
				if (phone!=''){
					if (input_key!="") input_key+="&";
					input_key+="phone="+phone;
				}
				
				if (city!=''){
					if (input_key!="") input_key+="&";
					input_key+="city="+city;
				}
				
				if (country!=''){
					if (input_key!="") input_key+="&";
					input_key+="country="+country;
				}
				
				if (group_name!=''){
					if (input_key!="") input_key+="&";
					input_key+="group_name="+group_name;
				}
				
//				if (sel_photo!=false){
//					if (input_key!="") input_key+="&";
//					input_key+="file_id="+file_id;
//				}				
				if (member == "admin")
				{
					if (input_key!="") input_key+="&";
					input_key+="adminid="+userid;
				}
				
				$.ajax({
					url: url ="Search?userid="+userid+"&"+input_key+ "&mobile=0&searchId=" + searchId,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						//alert(data);
						if (data.trim()=="") {
//							document.getElementById('person-name1').value= "";
//							document.getElementById('sex1').value="";
//							document.getElementById('birthday1').value="";
//							document.getElementById('home1').value="";
//							document.getElementById('email1').value="";
//							document.getElementById('phone1').value="";
//							setsearchImg('assets/img/nophoto.png');
//							sel_photo=0;
							return;
						}
						var obj = eval ("(" + data + ")");
						search_result = eval ("(" + data + ")");
						if (obj.searchlists=="none"){
							$("#alert-body").text("There is no result!!!");
							$('#alert').modal('show');
							return;
						}
						else if (obj.searchlists=="fail"){
							$("#alert-body").text("There is no result!!!");
							$('#alert').modal('show');
							return;
						}
						document.getElementById('table_body').innerHTML="";
						if (obj.searchlists!="none"){
							var tb_body="";
							for (var i=0;i<obj.searchlists.length;i++){
								var id = obj.searchlists[i].id;
								var name = obj.searchlists[i].name;
								if (name==null || name=="null") name = " ";
								var sex = obj.searchlists[i].sex;
								if (sex==null || sex=="null") sex = " ";
								var birth = obj.searchlists[i].birth;
								if (birth==null || birth=="null") birth = " ";
								var home= obj.searchlists[i].home;
								if (home==null || home=="null") home = " ";
								var email = obj.searchlists[i].email;
								if (email==null || email =="null") email = " ";
								var phone = obj.searchlists[i].phone;
								if (phone==null || phone=="null") phone = " ";
								var city = obj.searchlists[i].city;
								if (city==null || city=="null") city = " ";
								var country = obj.searchlists[i].country;
								if (country==null || country=="null") country = " ";
								var group_name = obj.searchlists[i].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								var fileid = obj.searchlists[i].fileid;
								if (fileid==null || fileid=="null") fileid = " ";
								var fileid1 = obj.searchlists[i].fileid1;
								if (fileid1==null || fileid1=="null") fileid1 = " ";
								var fileid2 = obj.searchlists[i].fileid2;
								if (fileid2==null || fileid2=="null") fileid2 = " ";
								var fileid3 = obj.searchlists[i].fileid3;
								if (fileid3==null || fileid3=="null") fileid3 = " ";
								
								var editviewstr = "Edit";
								//var admins = obj.searchlists[i].agents;
								
								var	img_field = '<img class="res_face" src="'+'personimgs/'+fileid+".jpg"+'" alt=""/>';
								var	img_field1 = '<img class="res_face" src="'+'personimgs/'+fileid1+".jpg"+'" alt=""/>';
								var	img_field2 = '<img class="res_face" src="'+'personimgs/'+fileid2+".jpg"+'" alt=""/>';
								var	img_field3 = '<img class="res_face" src="'+'personimgs/'+fileid3+".jpg"+'" alt=""/>';
							
								var result_tr='<tr class="res_tr" id="tr_'+id+'">'
													+'<td>'+img_field+'</td>'
													+'<td>'+img_field1+'</td>'
													+'<td>'+img_field2+'</td>'
													+'<td>'+img_field3+'</td>'
													+'<td id="name_'+id+'">'+name+'</td>'
													+'<td id="sex_'+id+'">'+sex+'</td>'
													+'<td id="birth_'+id+'">'+birth+'</td>'
													+'<td id="home_'+id+'">'+home+'</td>'
													+'<td id="city_'+id+'">'+city+'</td>'
													+'<td id="country_'+id+'">'+country+'</td>'
													+'<td id="email_'+id+'">'+email+'</td>'
													+'<td id="phone_'+id+'">'+phone+'</td>'
													+'<td id="group_name_'+id+'">'+group_name+'</td>'
													+'<td class="edit_td btn default btn-xs green-stripe" id="edit_'+id+'">'+editviewstr+'</td>'
													+'<td class="delete_td btn default btn-xs red-stripe" id="delete_'+id+'">Delete</td>'
											+'</tr>';
								tb_body+=result_tr;
							}
							//alert(document.getElementById('table_body').innerHTML);
							document.getElementById('table_body').innerHTML=tb_body;							
							
							$('.fileid_td').hide();
							
							$('.edit_td').on('mousedown', function () {
								var idstr = this.id;
								var sep_arr = idstr.split("_");
								var id = sep_arr[1];
								
								update_person_id = id;
								var index = -1;
								for (var i=0;i<search_result.searchlists.length;i++)
								{
									if (id == search_result.searchlists[i].id)
									{
										index = i;
										break;
									}
								}
								//$("#person_img").attr("src","./personimgs/"+id+".jpg");
								//$('#person_view').modal('show');
								var name = search_result.searchlists[index].name;
								if (name==null || name=="null") name = " ";
								var sex = search_result.searchlists[index].sex;
								if (sex==null || sex=="null") sex = " ";
								var birth = search_result.searchlists[index].birth;
								if (birth==null || birth=="null") birth = " ";
								var home= search_result.searchlists[index].home;
								if (home==null || home=="null") home = " ";
								var email = search_result.searchlists[index].email;
								if (email==null || email =="null") email = " ";
								var phone = search_result.searchlists[index].phone;
								if (phone==null || phone=="null") phone = " ";
								var city = search_result.searchlists[index].city;
								if (city==null || city=="null") city = " ";
								var country = search_result.searchlists[index].country;
								if (country==null || country=="null") country = " ";
								var group_name = obj.searchlists[index].group_name;
								if (group_name==null || group_name=="null") group_name = " ";
								
								document.getElementById('edit_person-name').value= name;
								document.getElementById('edit_person-birthday').value=birth;
								document.getElementById('edit_person-home').value=home;
								document.getElementById('edit_person-email').value=email;
								document.getElementById('edit_person-phone').value=phone;
								document.getElementById('edit_person-city_town').value=city;
								document.getElementById('edit_person-country').value=country;
								document.getElementById('edit_person-sex').value=sex;
								document.getElementById('edit_group_name').value=group_name;
								
//								document.getElementById('edit_person-name').readOnly = true;
//								document.getElementById('edit_person-birthday').readOnly = true;
//								document.getElementById('edit_person-sex').disabled = true;
								
								$('#EditPerson').modal('show');
								
								
							});	
							$('.delete_td').on('mousedown', function () {
								
								var result = confirm("Do you really want to delete?");
								if (result) {
								    //Logic to delete the item
									var idstr = this.id;
									var sep_arr = idstr.split("_");

									var id = sep_arr[1];
									
									$.ajax({
										url: url ="PersonDelete?userid="+userid+"&index="+id,
										type: 'POST',
										datatype: 'json',
										async: true,
										success: function (data) {
											//alert(data);
											if (data.trim()=="") {

												return;
											}
											var obj = eval ("(" + data + ")");
											if (obj.persondelete=="fail"){
												$("#alert-body").text("Failed to delete person!");
												$('#alert').modal('show');
											}
											else if (obj.persondelete=="ok")
											{
												$("#alert-body").text("Successfully deleted person!");
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
						//
//						document.getElementById('person-name1').value= "";
//						document.getElementById('sex1').value="";
//						document.getElementById('birthday1').value="";
//						document.getElementById('home1').value="";
//						document.getElementById('email1').value="";
//						document.getElementById('phone1').value="";
//						setsearchImg('assets/img/nophoto.png');
//						=0;
					},
					cache: false,
					contentType: false,
					processData: false
				});
				searchId = -1;
				$('#btn_search').show();
				$('#btn_update').hide();
				return false;
			});//$("form#search").submit(function(){
			
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
			
//			$("#face_ok").on('mousedown', function () {
//				if (sel_create_photo == 1)
//				{
//					setCreateAccountImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
//					$("#create_account_close_btn").show();
//					return;
//				}
//				if (sel_edit_photo == 1)
//				{
//					setEditAccountImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
//					$("#chagne_account_close_btn").show();
//					return;
//				}
//				if (sel_create_admin_photo == 1)
//				{
//					setCreateAgentImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
//					$("#create_admin_close_btn").show();
//					return;
//				}
//				setsearchImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
//				//document.getElementById('search_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
//				$("#close_btn").show();
//				sel_photo=1;
//			});
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
			
			$("#edit_person_close_btn").on('mousedown', function () {
				setEditPersonImg('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_photo=0;
				edit_sel_photo = 0
				$("#edit_person_close_btn").hide();
			});
			
			$("#edit_person_close_btn1").on('mousedown', function () {
				setEditPersonImg1('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_photo1=0;
				edit_sel_photo1 = 0				
				$("#edit_person_close_btn1").hide();
			});
			
			$("#edit_person_close_btn2").on('mousedown', function () {
				setEditPersonImg2('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_photo2=0;
				edit_sel_photo2 = 0	
				$("#edit_person_close_btn2").hide();
			});
			
			$("#edit_person_close_btn3").on('mousedown', function () {
				setEditPersonImg3('assets/img/nophoto.png');
				//document.getElementById('search_photo').src='assets/img/nophoto.png';
				sel_photo3=0;
				edit_sel_photo3 = 0	
				$("#edit_person_close_btn3").hide();
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
							if (sel_photo == 1)
							{
								setEditPersonImg('data/'+userid+'/request/'+file_id+'_cr.jpg');
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#edit_person_close_btn").show();
								sel_photo = 0;
								return;
							}
							if (sel_photo1 == 1)
							{
								setEditPersonImg1('data/'+userid+'/request/'+file_id+'_cr.jpg');
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#edit_person_close_btn1").show();
								sel_photo1 = 0;
								return;
							}
							if (sel_photo2 == 1)
							{
								setEditPersonImg2('data/'+userid+'/request/'+file_id+'_cr.jpg');
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#edit_person_close_btn2").show();
								sel_photo2 = 0;
								return;
							}
							if (sel_photo3 == 1)
							{
								setEditPersonImg3('data/'+userid+'/request/'+file_id+'_cr.jpg');
								sel_photo3 = 0;
								//document.getElementById('register_photo').src='data/'+userid+'/request/'+file_id+'_cr.jpg';
								$("#edit_person_close_btn3").show();
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
			
			$("#close_person").on('mousedown', function () {
				$('#EditPerson').modal('hide');
			});
			
			$("#save_person").on('mousedown', function () {
				
				var name = document.getElementById('edit_person-name').value.trim();
				var sex = document.getElementById('edit_person-sex').value.trim();
				var birthday = document.getElementById('edit_person-birthday').value.trim();
				var home = document.getElementById('edit_person-home').value.trim();
				var email = document.getElementById('edit_person-email').value.trim();
				var phone = document.getElementById('edit_person-phone').value.trim();
				var city = document.getElementById('edit_person-city_town').value.trim();
				var country = document.getElementById('edit_person-country').value.trim();
				var group_name = document.getElementById('edit_group_name').value.trim();

				var input_key="";
				if (name!=''){
					input_key+="name="+name;
				}

				//if (sex!=''){
					if (input_key!="") input_key+="&";
					input_key+="sex="+sex;
				//}
//				else{
//					$("#alert-body").text("Please input person's sex!");
//					$('#alert').modal('show');
//					return false;
//				}
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
						$("#alert-body").text("Please input person's birthday!");
						$('#alert').modal('show');
						return false;
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
				if (group_name!=''){
					if (input_key!="") input_key+="&";
					input_key+="group_name="+group_name;
				}
				else{
					$("#alert-body").text("Please input person's group!");
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
				
				if (edit_sel_photo == 1){
					if (input_key!="") input_key+="&";
					input_key+="file_id="+edit_image_list[0];
				}
				if (edit_sel_photo1 == 1){
					if (input_key!="") input_key+="&";
					input_key+="file_id1="+edit_image_list[1];
				}
				if (edit_sel_photo2 == 1){
					if (input_key!="") input_key+="&";
					input_key+="file_id2="+edit_image_list[2];
				}
				if (edit_sel_photo3 == 1){
					if (input_key!="") input_key+="&";
					input_key+="file_id3="+edit_image_list[3];
				}

				
				if (update_person_id!=-1){
					if (input_key!="") input_key+="&";
					input_key+="id="+update_person_id;
				}
				
				$.ajax({
					url: url ="PersonEdit?userid="+userid+"&"+input_key,
					type: 'POST',
					datatype: 'json',
					async: true,
					success: function (data) {
						//alert(data);
						if (data.trim()=="") {
							document.getElementById('edit_person-name').value= "";
							document.getElementById('edit_person-birthday').value="";
							document.getElementById('edit_person-home').value="";
							document.getElementById('edit_person-email').value="";
							document.getElementById('edit_person-phone').value="";
							document.getElementById('edit_person-city_town').value="";
							document.getElementById('edit_person-country').value="";
							document.getElementById('edit_person-sex').value="";
							setEditPersonImg('assets/img/nophoto_register.png');
							setEditPersonImg1('assets/img/nophoto_register.png');
							setEditPersonImg2('assets/img/nophoto_register.png');
							setEditPersonImg3('assets/img/nophoto_register.png');
							sel_photo=0;
							sel_photo1=0;
							sel_photo2=0;
							sel_photo3=0;
							$("#edit_person_close_btn").hide();
							$("#edit_person_close_btn1").hide();
							$("#edit_person_close_btn2").hide();
							$("#edit_person_close_btn3").hide();
							edit_sel_photo = 0;
							edit_sel_photo1 = 0;
							edit_sel_photo2 = 0;
							edit_sel_photo3 = 0;
							return;
						}
						var obj = eval ("(" + data + ")");
						if (obj.personeditlists=="fail"){
							$("#alert-body").text("Update failed!!!");
							$('#alert').modal('show');
							return;
						}
						if (obj.personeditlists == "nofeature")
						{
							$("#alert-body").text("Update failed! Feature can't be extracted!");
							$('#alert').modal('show');
							return;
						}
						if (obj.personeditlists=="ok"){
							$("#alert-body").text("Successfully updated human data on the server!!!");
							$('#alert').modal('show');
							
							document.getElementById('edit_person-name').value= "";
							document.getElementById('edit_person-birthday').value="";
							document.getElementById('edit_person-home').value="";
							document.getElementById('edit_person-email').value="";
							document.getElementById('edit_person-phone').value="";
							document.getElementById('edit_person-city_town').value="";
							document.getElementById('edit_person-country').value="";
							document.getElementById('edit_person-sex').value="";
							setEditPersonImg('assets/img/nophoto_register.png');
							setEditPersonImg1('assets/img/nophoto_register.png');
							setEditPersonImg2('assets/img/nophoto_register.png');
							setEditPersonImg3('assets/img/nophoto_register.png');
							sel_photo=0;
							sel_photo1=0;
							sel_photo2=0;
							sel_photo3=0;
							$("#edit_person_close_btn").hide();
							$("#edit_person_close_btn1").hide();
							$("#edit_person_close_btn2").hide();
							$("#edit_person_close_btn3").hide();
							edit_sel_photo = 0;
							edit_sel_photo1 = 0;
							edit_sel_photo2 = 0;
							edit_sel_photo3 = 0;
							$('#EditPerson').modal('hide');
							
						}
					},
					cache: false,
					contentType: false,
					processData: false
				});
				return false;
			});//$("form#register").submit(function(){

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
			
			function setEditPersonImg(url)
			{
				var img = document.getElementById('edit_person_photo');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
				edit_sel_photo = 1;
			}
			
			function setEditPersonImg1(url)
			{
				var img = document.getElementById('edit_person_photo1');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
				edit_sel_photo1 = 1;
			}
			
			function setEditPersonImg2(url)
			{
				var img = document.getElementById('edit_person_photo2');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
				edit_sel_photo2 = 1;
			}
			
			function setEditPersonImg3(url)
			{
				var img = document.getElementById('edit_person_photo3');
				img.src=url;
				if (img.width>img.height){
					img.style.width='164px';
					img.style.height='auto';
				}
				else{
					img.style.width='auto';
					img.style.height='164px';
				}
				edit_sel_photo3 = 1;
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