<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
String userid = (String)session.getAttribute("userid");
if (userid ==null) 
	userid = request.getParameter("userid");
if (userid==null){//session losted
	RequestDispatcher mDispatcher = request.getRequestDispatcher("/index.jsp");
	mDispatcher.forward(request, response);
	return;
}
String password = (String)session.getAttribute("password");
String fullname = (String)session.getAttribute("fullname");
String email = (String)session.getAttribute("email");
String address = (String)session.getAttribute("address");
String city = (String)session.getAttribute("city");
String country = (String)session.getAttribute("country");
String is_toast =(String)session.getAttribute("toast_run");
%>
<!---->
<!DOCTYPE html>
<!-- 
Template Name: Metronic - Responsive Admin Dashboard Template build with Twitter Bootstrap 3.0.3
Version: 1.5.5
Author: KeenThemes
Website: http://www.keenthemes.com/
Purchase: http://themeforest.net/item/metronic-responsive-admin-dashboard-template/4021469?ref=keenthemes
-->
<!--[if IE 8]> <html lang="en" class="ie8 no-js"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9 no-js"> <![endif]-->
<!--[if !IE]><!-->
<html lang="en" class="no-js">
<!--<![endif]-->
<!-- BEGIN HEAD -->
<head>
<meta charset="utf-8"/>
<title>FaceData Register | Results</title>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<meta content="" name="description"/>
<meta content="" name="author"/>
<meta name="MobileOptimized" content="320">
<!-- BEGIN GLOBAL MANDATORY STYLES -->
<link href="assets/plugins/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
<link href="assets/plugins/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
<link href="assets/plugins/uniform/css/uniform.default.css" rel="stylesheet" type="text/css"/>
<!-- END GLOBAL MANDATORY STYLES -->
<!-- BEGIN PAGE LEVEL STYLES -->
<link rel="stylesheet" type="text/css" href="assets/plugins/bootstrap-toastr/toastr.min.css"/>
<link href="assets/plugins/jquery-file-upload/css/jquery.fileupload-ui.css" rel="stylesheet"/>
<link href="assets/plugins/jcrop/css/jquery.Jcrop.min.css" rel="stylesheet"/>
<link rel="stylesheet" type="text/css" href="assets/plugins/select2/select2_metro.css"/>
<link href="assets/css/pages/image-crop.css" rel="stylesheet"/>
<!-- END PAGE LEVEL STYLES -->
<!-- BEGIN THEME STYLES -->
<link href="assets/css/style-metronic.css" rel="stylesheet" type="text/css"/>
<link href="assets/css/style.css" rel="stylesheet" type="text/css"/>
<link href="assets/css/style-responsive.css" rel="stylesheet" type="text/css"/>
<link href="assets/css/plugins.css" rel="stylesheet" type="text/css"/>
<link href="assets/css/themes/default.css" rel="stylesheet" type="text/css" id="style_color"/>
<link href="assets/css/pages/search.css" rel="stylesheet" type="text/css"/>
<link href="assets/css/custom.css" rel="stylesheet" type="text/css"/>
<!-- END THEME STYLES -->

</head>
<!-- END HEAD -->
<!-- BEGIN BODY -->
<body class="page-header-fixed">
<!-- BEGIN HEADER -->
<div class="header navbar navbar-inverse navbar-fixed-top">
	<!-- BEGIN TOP NAVIGATION BAR -->
	<div class="header-inner">
		<!-- BEGIN LOGO -->
		
		<a class="navbar-brand" href="index.jsp">
		<img src="assets/img/logo.png" alt="logo" class="img-responsive"/>
		</a>
		
		<!-- END LOGO -->
		<!-- BEGIN RESPONSIVE MENU TOGGLER -->
		<a href="javascript:;" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
		<img src="assets/img/menu-toggler.png" alt=""/>
		</a>
		<div class="hor-menu">
			<ul class="nav navbar-nav">
				<li>
					<a href="search.jsp"> Search Page </a>
				</li>
			</ul>
		</div>
		<div class="hor-menu">
			<ul class="nav navbar-nav">
				<li>
					<a href="register.jsp"> Register Page </a>
				</li>
			</ul>
		</div>
		<div class="hor-menu">
			<ul class="nav navbar-nav">
				<li>
					<a href="agents.jsp"> Agents Page </a>
				</li>
			</ul>
		</div>
		<!-- END RESPONSIVE MENU TOGGLER -->
		<ul class="nav navbar-nav pull-right">
			
			<!-- BEGIN USER LOGIN DROPDOWN -->
			<li class="dropdown user">
				<a href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-close-others="true">
				<i class="fa fa-user"></i> 
				<span class="username" id = "fullname_span">
				</span>
				<i class="fa fa-angle-down"></i>
				</a>
				<ul class="dropdown-menu">
					<li>
						<a data-toggle="modal" href="#responsive"><i class="fa fa-edit"></i> Edit Acount</a>
					</li>
					<li>
						<a href="index.jsp"><i class="fa fa-key"></i> Log Out</a>
					</li>
				</ul>
			</li>
			<!-- END USER LOGIN DROPDOWN -->
		</ul>
		<!-- END TOP NAVIGATION MENU -->
	</div>
	<!-- END TOP NAVIGATION BAR -->
</div>
<!-- END HEADER -->
<div class="clearfix">
</div>
<!-- BEGIN CONTAINER -->
<div class="page-container">
	<!-- BEGIN SIDEBAR -->
	<div class="page-sidebar-wrapper">
		<div class="page-sidebar navbar-collapse collapse">
			<!-- BEGIN SIDEBAR MENU -->
			<ul class="page-sidebar-menu" id = "admins_sidebar">
				<li class="sidebar-toggler-wrapper">
					<!-- BEGIN SIDEBAR TOGGLER BUTTON -->
					<div class="sidebar-toggler hidden-phone">
					</div>
					<!-- BEGIN SIDEBAR TOGGLER BUTTON -->
				</li>

			</ul>
			<!-- END SIDEBAR MENU -->
		</div>
	</div>
	<!-- END SIDEBAR -->
	<!-- BEGIN CONTENT -->
	<div class="page-content-wrapper">
		<div class="page-content">			
			<!-- BEGIN PAGE HEADER-->
			<div class="row">
				<div class="col-md-9">
					<!-- BEGIN PAGE TITLE & BREADCRUMB-->
					<h3 class="page-title">
						Register Page						
											
					</h3>
					
					<!-- END PAGE TITLE & BREADCRUMB-->
				</div>
				<div class="col-md-3">
					<div class="booking-app" id="get_frapp">
						<a>
						<i class="fa fa-share pull-left"></i>
						<span>
							Get FR agent app!
						</span>
						</a>
					</div>
				</div>
			    </div>
			<!-- END PAGE HEADER-->
			<!-- BEGIN PAGE CONTENT-->
			<div class="row">
				<div class="col-md-12">
					<div class="tabbable tabbable-custom tabbable-full-width">						
						<div class="tab-content">							
							<!--start register-pane-->
							<div id="tab_1_5" class="tab-pane active">
								<div class="row register-form-default">
										<div class="col-md-3" id="register_img_panel">
										<form id="uploadphoto" method="POST" enctype="multipart/form-data">
											<div id="photo_pan">
											<img src="assets/img/nophoto_register.png" id = "register_photo"></img>
											<img src="assets/img/close.png" id = "close_btn"></img>
											</div>
											<span class="btn green fileinput-button">
												<i class="fa fa-plus"></i>
												<input type="file" name="files" id="filename">
											</span>
											<button class="btn blue start">
											<i class="fa fa-upload"></i>
											<span>
												Upload
											</span>
											</button>
										</form>	
										</div>
										<div class="col-md-9">
										<form id="register" method="POST">										
											<div class="input-group">
												<div class="row form-group">
													<div class="row">
														<div class="col-md-6">
															<label class="control-label">Name</label>
															<div class="input-icon">
																<i class="fa fa-font"></i>
																<input class="form-control" size="16" type="text" id = "person-name1"/>
															</div>
														</div>
														<div class="col-md-3">
															<label class="control-label">Sex</label>
																<select name="sex" id="sex1" class="select2 form-control">
																	<option value=""></option>
																	<option value="man">man</option>
																	<option value="woman">woman</option>
																</select>
														</div>
														<div class="col-md-3">
															<label class="control-label">Birthday</label>
															<div >
																<input class="form-control" size="16" type="text" placeholder="2018-6-28" id="birthday1">
															</div>
														</div>
													
													</div>
													<div class="row">
														<div class="col-md-12">
															<label class="control-label">Home</label>
															<div class="input-icon">
																<i class="fa fa-home"></i>
																<input class="form-control" size="16" type="text" id="home1"/>
															</div>
														</div>
													</div>
													<div class="row">
														<div class="col-md-6">
															<label class="control-label">Email</label>
															<div class="input-icon">
																<i class="fa fa-envelope"></i>
																<input class="form-control" size="16" type="text" id="email1">
															</div>
														</div>	
													</div>
													<div class="row">
														<div class="col-md-8">
															<label class="control-label">Phone</label>
															<div class="input-icon">
																<i class="fa fa-phone"></i>
																<input class="form-control" size="16" type="text" id="phone1">
															</div>
														</div>		
														<div class="col-md-4">
															<label class="control-label">&nbsp</label>
															<span class="input-group-btn">
															<button class="btn green" id = "btn_register">
																Register &nbsp; <i class="m-icon-swapright m-icon-white" ></i>
															</button>
															</span>
														</div>
													</div>
												</div>		
											</div>											
										</form>
									</div>
								</div>
							</div>
							<!--end tab-pane-->
						</div>
					</div>
				</div>
				<!--end tabbable-->
			</div>		
			<!-- END PAGE CONTENT-->
		</div>
	</div>
	<!-- END CONTENT -->
</div>
<!-- END CONTAINER -->
<!-- BEGIN FOOTER -->
<div class="footer">
	<div class="footer-inner">
		
	</div>
	<div class="footer-tools">
		<span class="go-top">
			<i class="fa fa-angle-up"></i>
		</span>
	</div>
</div>
<!-- END FOOTER -->
<!-- BEGIN JAVASCRIPTS(Load javascripts at bottom, this will reduce page load time) -->
<!-- BEGIN CORE PLUGINS -->
<!--[if lt IE 9]>
<script src="assets/plugins/respond.min.js"></script>
<script src="assets/plugins/excanvas.min.js"></script> 
<![endif]-->
<script src="assets/plugins/jquery-1.10.2.min.js" type="text/javascript"></script>
<script src="assets/plugins/jquery-migrate-1.2.1.min.js" type="text/javascript"></script>
<script src="assets/plugins/bootstrap/js/bootstrap.min.js" type="text/javascript"></script>
<script src="assets/plugins/bootstrap-hover-dropdown/twitter-bootstrap-hover-dropdown.min.js" type="text/javascript"></script>
<script src="assets/plugins/jquery-slimscroll/jquery.slimscroll.min.js" type="text/javascript"></script>
<script src="assets/plugins/jquery.blockui.min.js" type="text/javascript"></script>
<script src="assets/plugins/jquery.cokie.min.js" type="text/javascript"></script>
<script src="assets/plugins/uniform/jquery.uniform.min.js" type="text/javascript"></script>
<script src="assets/plugins/bootstrap-toastr/toastr.min.js"></script>
<script src="assets/plugins/jcrop/js/jquery.color.js"></script>
<script src="assets/plugins/jcrop/js/jquery.Jcrop.js"></script>
<!-- END CORE PLUGINS -->

<script src="assets/scripts/app.js"></script>
<script src="assets/scripts/register.js"></script>
<script src="assets/scripts/form-image-crop.js"></script>
<script>
jQuery(document).ready(function() {    
   App.init();
   Register.init();
  
 	<%		
		if(fullname != null){	
			String success_toast="ShowToast('success', 'You are able to search and register any faces freely.','Wecome to "+fullname+"!', 'toast-top-right');";%>
			<%		
			if(is_toast.contains("OK")){%>	
				<%=success_toast%>
				<%session.setAttribute("toast_run", "NO");%>
			<%
			}
			%>
	<%
		}
	%>
});
</script>
<!-- END JAVASCRIPTS -->
<!-- BEGIN SAMPLE PORTLET CONFIGURATION MODAL FORM-->
<div id="responsive" class="modal fade" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">Change Your Acount</h4>
			</div>
			<div class="modal-body">
				<div class="scroller" style="height:400px" data-always-visible="1" data-rail-visible1="1">
					<div class="col-md-6">
						<div class="form-group">
							<label class="control-label">Full Name</label>
							<div class="input-icon">
								<i class="fa fa-font"></i>
								<input class="form-control placeholder-no-fix" type="text" value=<%=fullname%> id="fullname"/>
							</div>
						</div>
						<div class="form-group">
							<!--ie8, ie9 does not support html5 placeholder, so we just show field title for that-->
							<label class="control-label">Email</label>
							<div class="input-icon">
								<i class="fa fa-envelope"></i>
								<input class="form-control placeholder-no-fix" type="text" value=<%=email%> id="email"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label">Address</label>
							<div class="input-icon">
								<i class="fa fa-check"></i>
								<input class="form-control placeholder-no-fix" type="text" value=<%=address%> id="address"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label">City/Town</label>
							<div class="input-icon">
								<i class="fa fa-location-arrow"></i>
								<input class="form-control placeholder-no-fix" type="text" value=<%=city%> id="city_town"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label">Country</label>
							<select id="country" class="select2 form-control">
								<option value=""></option>
									<option value="Afghanistan">Afghanistan</option>
									<option value="Albania">Albania</option>
									<option value="Algeria">Algeria</option>
									<option value="American Samoa">American Samoa</option>
									<option value="Andorra">Andorra</option>
									<option value="Angola">Angola</option>
									<option value="Anguilla">Anguilla</option>
									<option value="Antarctica">Antarctica</option>
									<option value="Argentina">Argentina</option>
									<option value="Armenia">Armenia</option>
									<option value="Aruba">Aruba</option>
									<option value="Australia">Australia</option>
									<option value="Austria">Austria</option>
									<option value="Azerbaijan">Azerbaijan</option>
									<option value="Bahamas">Bahamas</option>
									<option value="Bahrain">Bahrain</option>
									<option value="Bangladesh">Bangladesh</option>
									<option value="Barbados">Barbados</option>
									<option value="Belarus">Belarus</option>
									<option value="Belgium">Belgium</option>
									<option value="Belize">Belize</option>
									<option value="Benin">Benin</option>
									<option value="Bermuda">Bermuda</option>
									<option value="Bhutan">Bhutan</option>
									<option value="Bolivia">Bolivia</option>
									<option value="Bosnia and Herzegowina">Bosnia and Herzegowina</option>
									<option value="BBotswanaW">Botswana</option>
									<option value="Bouvet Island">Bouvet Island</option>
									<option value="Brazil">Brazil</option>
									<option value="British Indian Ocean Territory">British Indian Ocean Territory</option>
									<option value="Brunei Darussalam">Brunei Darussalam</option>
									<option value="Bulgaria">Bulgaria</option>
									<option value="Burkina Faso">Burkina Faso</option>
									<option value="Burundi">Burundi</option>
									<option value="Cambodia">Cambodia</option>
									<option value="Cameroon">Cameroon</option>
									<option value="Canada">Canada</option>
									<option value="Cape Verde">Cape Verde</option>
									<option value="Cayman Islands">Cayman Islands</option>
									<option value="Central African Republic">Central African Republic</option>
									<option value="Chad">Chad</option>
									<option value="Chile">Chile</option>
									<option value="China">China</option>
									<option value="Christmas Island">Christmas Island</option>
									<option value="Cocos (Keeling) Islands">Cocos (Keeling) Islands</option>
									<option value="Colombia">Colombia</option>
									<option value="Comoros">Comoros</option>
									<option value="Congo">Congo</option>
									<option value="Congo, the Democratic Republic of the">Congo, the Democratic Republic of the</option>
									<option value="Cook Islands">Cook Islands</option>
									<option value="Costa Rica">Costa Rica</option>
									<option value="Cote d'Ivoire">Cote d'Ivoire</option>
									<option value="Croatia (Hrvatska)">Croatia (Hrvatska)</option>
									<option value="Cuba">Cuba</option>
									<option value="Cyprus">Cyprus</option>
									<option value="Czech Republic">Czech Republic</option>
									<option value="Denmark">Denmark</option>
									<option value="Djibouti">Djibouti</option>
									<option value="Dominica">Dominica</option>
									<option value="Dominican Republic">Dominican Republic</option>
									<option value="Ecuador">Ecuador</option>
									<option value="Egypt">Egypt</option>
									<option value="El Salvador">El Salvador</option>
									<option value="Equatorial Guinea">Equatorial Guinea</option>
									<option value="Eritrea">Eritrea</option>
									<option value="Estonia">Estonia</option>
									<option value="Ethiopia">Ethiopia</option>
									<option value="Falkland Islands (Malvinas)">Falkland Islands (Malvinas)</option>
									<option value="Faroe Islands">Faroe Islands</option>
									<option value="Fiji">Fiji</option>
									<option value="Finland">Finland</option>
									<option value="France">France</option>
									<option value="French Guiana">French Guiana</option>
									<option value="French Polynesia">French Polynesia</option>
									<option value="French Southern Territories">French Southern Territories</option>
									<option value="Gabon">Gabon</option>
									<option value="Gambia">Gambia</option>
									<option value="Georgia">Georgia</option>
									<option value="Germany">Germany</option>
									<option value="Ghana">Ghana</option>
									<option value="Gibraltar">Gibraltar</option>
									<option value="Greece">Greece</option>
									<option value="Greenland">Greenland</option>
									<option value="Grenada">Grenada</option>
									<option value="Guadeloupe">Guadeloupe</option>
									<option value="Guam">Guam</option>
									<option value="Guatemala">Guatemala</option>
									<option value="Guinea">Guinea</option>
									<option value="Guinea-Bissau">Guinea-Bissau</option>
									<option value="Guyana">Guyana</option>
									<option value="Haiti">Haiti</option>
									<option value="Heard and Mc Donald Islands">Heard and Mc Donald Islands</option>
									<option value="Holy See (Vatican City State)">Holy See (Vatican City State)</option>
									<option value="Honduras">Honduras</option>
									<option value="Hong Kong">Hong Kong</option>
									<option value="Hungary">Hungary</option>
									<option value="Iceland">Iceland</option>
									<option value="India">India</option>
									<option value="Indonesia">Indonesia</option>
									<option value="Iran (Islamic Republic of)">Iran (Islamic Republic of)</option>
									<option value="Iraq">Iraq</option>
									<option value="Ireland">Ireland</option>
									<option value="Israel">Israel</option>
									<option value="Italy">Italy</option>
									<option value="Jamaica">Jamaica</option>
									<option value="Japan">Japan</option>
									<option value="Jordan">Jordan</option>
									<option value="Kazakhstan">Kazakhstan</option>
									<option value="Kenya">Kenya</option>
									<option value="Kiribati">Kiribati</option>
									<option value="Korea, Democratic People's Republic of">Korea, Democratic People's Republic of</option>
									<option value="Korea, Republic of">Korea, Republic of</option>
									<option value="Kuwait">Kuwait</option>
									<option value="Kyrgyzstan">Kyrgyzstan</option>
									<option value="Lao People's Democratic Republic">Lao People's Democratic Republic</option>
									<option value="Latvia">Latvia</option>
									<option value="Lebanon">Lebanon</option>
									<option value="Lesotho">Lesotho</option>
									<option value="Liberia">Liberia</option>
									<option value="Libyan Arab Jamahiriya">Libyan Arab Jamahiriya</option>
									<option value="Liechtenstein">Liechtenstein</option>
									<option value="Lithuania">Lithuania</option>
									<option value="Luxembourg">Luxembourg</option>
									<option value="Macau">Macau</option>
									<option value="Macedonia, The Former Yugoslav Republic of">Macedonia, The Former Yugoslav Republic of</option>
									<option value="Madagascar">Madagascar</option>
									<option value="Malawi">Malawi</option>
									<option value="Malaysia">Malaysia</option>
									<option value="Maldives">Maldives</option>
									<option value="Mali">Mali</option>
									<option value="Malta">Malta</option>
									<option value="Marshall Islands">Marshall Islands</option>
									<option value="Martinique">Martinique</option>
									<option value="Mauritania">Mauritania</option>
									<option value="Mauritius">Mauritius</option>
									<option value="Mayotte">Mayotte</option>
									<option value="Mexico">Mexico</option>
									<option value="Micronesia, Federated States of">Micronesia, Federated States of</option>
									<option value="Moldova, Republic of">Moldova, Republic of</option>
									<option value="Monaco">Monaco</option>
									<option value="Mongolia">Mongolia</option>
									<option value="Montserrat">Montserrat</option>
									<option value="Morocco">Morocco</option>
									<option value="Mozambique">Mozambique</option>
									<option value="Myanmar">Myanmar</option>
									<option value="Namibia">Namibia</option>
									<option value="Nauru">Nauru</option>
									<option value="Nepal">Nepal</option>
									<option value="Netherlands">Netherlands</option>
									<option value="Netherlands Antilles">Netherlands Antilles</option>
									<option value="New Caledonia">New Caledonia</option>
									<option value="New Zealand">New Zealand</option>
									<option value="Nicaragua">Nicaragua</option>
									<option value="Niger">Niger</option>
									<option value="Nigeria">Nigeria</option>
									<option value="Niue">Niue</option>
									<option value="Norfolk Island">Norfolk Island</option>
									<option value="Northern Mariana Islands">Northern Mariana Islands</option>
									<option value="Norway">Norway</option>
									<option value="Oman">Oman</option>
									<option value="Pakistan">Pakistan</option>
									<option value="Palau">Palau</option>
									<option value="Panama">Panama</option>
									<option value="Papua New Guinea">Papua New Guinea</option>
									<option value="Paraguay">Paraguay</option>
									<option value="Peru">Peru</option>
									<option value="Philippines">Philippines</option>
									<option value="Pitcairn">Pitcairn</option>
									<option value="Poland">Poland</option>
									<option value="Portugal">Portugal</option>
									<option value="Puerto Rico">Puerto Rico</option>
									<option value="Qatar">Qatar</option>
									<option value="Reunion">Reunion</option>
									<option value="Romania">Romania</option>
									<option value="Russian Federation">Russian Federation</option>
									<option value="Rwanda">Rwanda</option>
									<option value="Saint Kitts and Nevis">Saint Kitts and Nevis</option>
									<option value="Saint LUCIA">Saint LUCIA</option>
									<option value="Saint Vincent and the Grenadines">Saint Vincent and the Grenadines</option>
									<option value="Samoa">Samoa</option>
									<option value="San Marino">San Marino</option>
									<option value="Sao Tome and Principe">Sao Tome and Principe</option>
									<option value="Saudi Arabia">Saudi Arabia</option>
									<option value="Senegal">Senegal</option>
									<option value="Seychelles">Seychelles</option>
									<option value="Sierra Leone">Sierra Leone</option>
									<option value="Singapore">Singapore</option>
									<option value="Slovakia (Slovak Republic)">Slovakia (Slovak Republic)</option>
									<option value="Slovenia">Slovenia</option>
									<option value="Solomon Islands">Solomon Islands</option>
									<option value="Somalia">Somalia</option>
									<option value="South Africa">South Africa</option>
									<option value="South Georgia and the South Sandwich Islands">South Georgia and the South Sandwich Islands</option>
									<option value="Spain">Spain</option>
									<option value="Sri Lanka">Sri Lanka</option>
									<option value="St. Helena">St. Helena</option>
									<option value="St. Pierre and Miquelon">St. Pierre and Miquelon</option>
									<option value="Sudan">Sudan</option>
									<option value="Suriname">Suriname</option>
									<option value="Svalbard and Jan Mayen Islands">Svalbard and Jan Mayen Islands</option>
									<option value="Swaziland">Swaziland</option>
									<option value="Sweden">Sweden</option>
									<option value="Switzerland">Switzerland</option>
									<option value="Syrian Arab Republic">Syrian Arab Republic</option>
									<option value="Taiwan, Province of China">Taiwan, Province of China</option>
									<option value="Tajikistan">Tajikistan</option>
									<option value="Tanzania, United Republic of">Tanzania, United Republic of</option>
									<option value="Thailand">Thailand</option>
									<option value="Togo">Togo</option>
									<option value="Tokelau">Tokelau</option>
									<option value="Tonga">Tonga</option>
									<option value="Trinidad and Tobago">Trinidad and Tobago</option>
									<option value="Tunisia">Tunisia</option>
									<option value="Turkey">Turkey</option>
									<option value="Turkmenistan">Turkmenistan</option>
									<option value="Turks and Caicos Islands">Turks and Caicos Islands</option>
									<option value="Tuvalu">Tuvalu</option>
									<option value="Uganda">Uganda</option>
									<option value="Ukraine">Ukraine</option>
									<option value="United Arab Emirates">United Arab Emirates</option>
									<option value="United Kingdom">United Kingdom</option>
									<option value="United States">United States</option>
									<option value="United States Minor Outlying Islands">United States Minor Outlying Islands</option>
									<option value="Uruguay">Uruguay</option>
									<option value="Uzbekistan">Uzbekistan</option>
									<option value="Vanuatu">Vanuatu</option>
									<option value="Venezuela">Venezuela</option>
									<option value="Viet Nam">Viet Nam</option>
									<option value="Virgin Islands (British)">Virgin Islands (British)</option>
									<option value="Virgin Islands (U.S.)">Virgin Islands (U.S.)</option>
									<option value="Wallis and Futuna Islands">Wallis and Futuna Islands</option>
									<option value="Western Sahara">Western Sahara</option>
									<option value="Yemen">Yemen</option>
									<option value="Zambia">Zambia</option>
									<option value="Zimbabwe">Zimbabwe</option>
							</select>
						</div>
					</div>
					<div class="col-md-6">
					
					<div class="form-group">
						<label class="control-label">User ID</label>
						<div class="input-icon">
							<i class="fa fa-user"></i>
							<input class="form-control placeholder-no-fix" type="text" autocomplete="off" value=<%=userid%> id="userid"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label visible-ie8 visible-ie9">Current Password</label>
						<div class="input-icon">
							<i class="fa fa-lock"></i>
							<input class="form-control placeholder-no-fix" type="password" autocomplete="off" id="cur_password" placeholder="Current Password"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label visible-ie8 visible-ie9">New Password</label>
						<div class="input-icon">
							<i class="fa fa-lock"></i>
							<input class="form-control placeholder-no-fix" type="password" autocomplete="off" id="new_password" placeholder="New Password"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label visible-ie8 visible-ie9">Re-type Your Password</label>
						<div class="controls">
							<div class="input-icon">
								<i class="fa fa-check"></i>
								<input class="form-control placeholder-no-fix" type="password" autocomplete="off" placeholder="Re-type Your Password" id="rpassword"/>
							</div>
						</div>
					</div>				
				</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" id="close_account" class="btn default">Close</button>
				<button type="button" class="btn green" id="save_account">Save changes</button>
			</div>
		</div>
	</div>
</div>
<!-- /.modal -->
<div class="modal fade" id="viewperson" tabindex="-1" role="basic" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">View Person</h4>
			</div>
			<div class="modal-body">
				<div class="tab-pane active">
				<div class="row">
					<div class="col-md-6 col-sm-12 responsive-1024">
						<img id = "view_img"/>
					</div>
					
					<div class="row form-group">
													
						<div class="col-md-5">
							<label class="control-label" id="name_lab">Name : name</label>							
						</div>
						<div class="col-md-5">
							<label class="control-label" id="sex_lab">Sex : man</label>
						</div>
						<div class="col-md-5">
							<label class="control-label" id="birthday_lab">Birthday : 2018-6-28</label>
						</div>
						<div class="col-md-5">
							<label class="control-label" id="home_lab">Home : home</label>					
						</div>	
						<div class="col-md-5">
							<label class="control-label" id="email_lab">Email : email</label>					
						</div>							
						<div class="col-md-5">
							<label class="control-label" id="phone_lab">Phone : 12345</label>					
						</div>
					</div>
					
				</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn blue" data-dismiss="modal">OK</button>
			</div>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>

<!-- /.modal -->
<div class="modal fade" id="cropphoto" tabindex="-1" role="basic" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">Crop Face</h4>
			</div>
			<div class="modal-body">
				<div class="tab-pane active">
				<div class="row">
					<div class="col-md-7 col-sm-12 responsive-1024">
						<h4>Crop face rectangle to get good performance by mouse dragging.</h4>
						<img id = "input_img"/>
					</div>
					
				</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn default" id="crop_cancel">No</button>
				<button type="button" class="btn blue" id="crop_ok">Yes</button>
			</div>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>


<!-- /.modal -->
<div class="modal fade" id="alert" tabindex="-1" role="basic" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">Alert</h4>
			</div>
			<div class="modal-body" id="alert-body">
				 
			</div>
			<div class="modal-footer">
				<button type="button" class="btn red" data-dismiss="modal">OK</button>				
			</div>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>

<!-- /.modal -->
<div class="modal fade" id="facephoto" tabindex="-1" role="basic" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">Face Photo</h4>
			</div>
			<div class="modal-body" id="alert-body">
				 <img id = "face_img"/>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn red" data-dismiss="modal" id="face_ok">OK</button>				
			</div>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>


<!-- /.modal -->
<div class="modal fade" id="download_FRAgent" tabindex="-1" role="basic" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
				<h4 class="modal-title">Download FR agent Apps</h4>
			</div>
			<div class="modal-body" id="alert-body">
				<h4 >Face Recognition App </h4>
				<a href="DownloadFRApp?FRAgent_type=fr_app">FRAgent.zip</a>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn red" data-dismiss="modal" id="face_ok">OK</button>				
			</div>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>
<div class="modal fade" id="ajax" tabindex="-1" role="basic" aria-hidden="true">
	<div id="scan_lab">Scaning Computers...</div>
	<img src="assets/img/ajax-modal-loading.gif" alt="" class="loading">
</div>

<!-- END SAMPLE PORTLET CONFIGURATION MODAL FORM-->

<script>
 
var select = document.getElementById("country");
select.value = "<%=country%>";
var full_name_span = document.getElementById("fullname_span");
full_name_span.innerHTML="<%=fullname%>";

var userid = "<%=userid%>";
var fullname = "<%=fullname%>";
var address="<%=address%>";
var email = "<%=email%>";
var city = "<%=city%>";
var country = "<%=country%>";
var isEdit = false;
var registerId = -1;
document.getElementById("fullname").value=fullname;
document.getElementById("email").value=email;
document.getElementById("address").value=address;
document.getElementById("city_town").value=city;
document.getElementById("country").value=country;
document.getElementById("userid").value=userid;
</script>
</body>
<!-- END BODY -->
</html>