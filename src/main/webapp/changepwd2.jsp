<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<title>Online Heath System</title>
		<meta charset="ISO-8859-1">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="stylesheet" href="assets/css/main.css" />
		<link rel="stylesheet" href="assets/css/noscript.css" />
		<script type="text/javascript" src="https://cdn.jsdelivr.net/npm/jquery-validation@1.19.0/dist/jquery.validate.min.js"></script>
		<script type="text/javascript">
			function checkForm() {
				var oldpassword = document.getElementById('old-password').value;
                var password = document.getElementById('password').value;
                var password2 = document.getElementById('password-repeat').value;
                
                if (oldpassword == password)
                {						
                	alert("Old passwordcannot be same as new password.");
                	return false;
                }
                
                if ((CheckPassword(password) == true) && (CheckPassword(password2) == true))
	            {
	                if (password != password2)
	                {						
	                	alert("Passwords do not correspond.");
	                	return false;
	                }
					else
					{
						return true;
					}
                }
                
                if ((CheckPassword(password) == false) || (CheckPassword(password2) == false))
                {
                	alert("Your password must be at least 8 characters long, contain at least one number, atleast one special character and have a mixture of uppercase and lowercase letters.");
                	return false;
                }
            }
			
			function CheckPassword(inputtxt) { 
				var paswd = /^(?=.*[0-9])(?=.*[!@#$%^&*.])[a-zA-Z0-9!@#$%^&*.]{7,15}$/;
				if (inputtxt.match(paswd))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		</script>
	</head>
	<body class="is-preload">
		<div id="page-wrapper">
			<header id="header" class="alt">
			<h1><a href="home2.jsp">Online Health System</a></h1>
				<nav>
					<a href="#menu">Menu</a>
				</nav>
			</header>
			<nav id="menu">
				<div class="inner">
					<h2>Menu</h2>
					<ul class="links">
						<li><a href="appointments2.jsp">Appointments</a></li>
						<li><a href="treatments2.jsp">Treatments</a></li>
						<li><a href="medicalrecord.jsp">New medical record</a></li>
						<li><a href="updatemedicalrecord.jsp">Update medical record</a></li>
						<li><a href="changepwd2.jsp">Change password</a></li>
						<li><a href="DoctorLogoutServlet?loginID=${doctor.getLoginID()}" style="width:auto;">Logout</a></li>
					</ul>
					<a href="#" class="close">Close</a>
				</div>
			</nav>

			<!-- Wrapper -->
			<section id="wrapper">
				<!-- Content -->
				<div class="wrapper">
					<div class="inner">
						<h3 class="major">Change password for user ${doctor.getFirstName()} ${doctor.getLastName()}</h3>
						<form name="changePwdForm"  onsubmit="return checkForm()" action="UpdatePatientPasswordServlet" method="post">
							<div class="container">
						    	<label for="old-password"><b>New Password</b></label>
						    	<input type="password" placeholder="Current Password" name="old-password" id="old-password" required />
						      
						    	<label for="password"><b>New Password</b></label>
						    	<input type="password" placeholder="New Password" name="password" id="password" required />
						
						    	<label for="password-repeat"><b>Repeat Password</b></label>
						    	<input type="password" placeholder="Repeat Password" name="password-repeat" id="password-repeat" required />
						    	
						    	<input type="hidden" name="username" value="${doctor.getUserName()}" />
						    	<input type="hidden" name="current-password" id="current-password" value="${doctor.getUserPassword()}" />
						
								${message}
								<p>&nbsp;</p>
								<div class="col-12">
									<ul class="actions">
										<li><input type="submit" value="Submit" class="primary" /></li>
										<li><input type="button" value="Cancel" class="cancelbtn" onclick="window.location.href='home2.jsp'"/></li>
									</ul>
								</div>
							</div>
						</form>
					</div>
				</div>
			</section>
		</div>

		<!-- Scripts -->
		<script src="assets/js/jquery.min.js"></script>
		<script src="assets/js/jquery.scrollex.min.js"></script>
		<script src="assets/js/browser.min.js"></script>
		<script src="assets/js/breakpoints.min.js"></script>
		<script src="assets/js/util.js"></script>
		<script src="assets/js/main.js"></script>
		<script type="text/javascript">
		    $(document).ready(function() {
		        $("#changePwdForm").validate({
		            rules: {
		            	old-password: "required",
		            	
		                password: "required",
		                
		                password-repeat: "required",
		            },
		             
		            messages: {
		            	old-password: "Please enter current password",
		            	
		                password: "Please enter new password",
		                
		                password-repeat: "Please re-enter new password",
		            }
		        });
		 
		    });
		</script>
	</body>
</html>
