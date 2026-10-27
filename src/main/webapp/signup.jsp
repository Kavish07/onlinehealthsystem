<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<title>Online Health System</title>
		<meta charset="ISO-8859-1">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="stylesheet" href="assets/css/main.css" />
		<link rel="stylesheet" href="assets/css/noscript.css" />
		<script type="text/javascript" src="https://cdn.jsdelivr.net/npm/jquery-validation@1.19.0/dist/jquery.validate.min.js"></script>
		<script type="text/javascript">
			function checkForm() {
                var password = document.getElementById('password').value;
                var password2 = document.getElementById('password-repeat').value;
                
                if ((CheckPassword(password) == true) && (CheckPassword(password2) == true))
	            {
	                if(password == password2)
	                {
						document.location.href="PatientRegisterServlet";
						retrn true;
	                }
	                else
	                {
	                	alert("Passwords do not correspond.");
	                	return false
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
			<h1><a href="index.jsp">Online Health System</a></h1>
				<nav>
					<a href="#menu">Menu</a>
				</nav>
			</header>
			<nav id="menu">
				<div class="inner">
					<h2>Menu</h2>
					<ul class="links">
						<li><a href="login.jsp" style="width:auto;">Login</a></li>
						<li><a href="signup.jsp" style="width:auto;">Sign Up</a></li>
					</ul>
					<a href="#" class="close">Close</a>
				</div>
			</nav>

			<!-- Wrapper -->
			<section id="wrapper">
				<!-- Content -->
				<div class="wrapper">
					<div class="inner">
						<h3 class="major">Sign Up</h3>
						<form name="signupForm" onsubmit="return checkForm()" action="PatientRegisterServlet" method="post">
							<div class="container">
						    	<p>Please fill in this form to create an account.</p>
						    	<label for="username"><b>Username</b></label>
						    	<input type="email" placeholder="Enter Email" name="username" required />
						
						    	<label for="password"><b>Password</b></label>
						    	<input type="password" placeholder="Enter Password" name="password" id="password" required />
						
						    	<label for="password-repeat"><b>Repeat Password</b></label>
						    	<input type="password" placeholder="Repeat Password" name="password-repeat" id="password-repeat" required />
						      
						    	<label for="firstname"><b>First name</b></label>
						    	<input type="text" placeholder="Enter First name" name="firstname" required />
						      
						    	<label for="lastname"><b>Last name</b></label>
						    	<input type="text" placeholder="Enter Last name" name="lastname" required />
						      
						    	<label for="nationalid"><b>National ID</b></label>
						    	<input type="text" placeholder="Enter National ID" name="nationalid" required />
						      
						    	<label for="dateofbirth"><b>Date of birth</b></label>
						    	<input type="date" placeholder="Enter Date of birth" name="dateofbirth" style="color: black;" required />
						
						    	<label for="sex"><b>Sex</b></label>
						    	<input type="text" placeholder="Enter Sex" name="sex" required />
						      
						    	<label for="address"><b>Address</b></label>
						    	<input type="text" placeholder="Enter Address" name="address" required />
						      
						    	<label for="city"><b>City</b></label>
						    	<input type="text" placeholder="Enter City" name="city" required />
						      
						    	<label for="weight"><b>Weight</b></label>
						    	<input type="text" placeholder="Enter Weight in kg" name="weight" />
						      
						    	<label for="height"><b>Height</b></label>
						    	<input type="text" placeholder="Enter Height in cms" name="height" />
						    	<hr>
						
						    	<p>By creating an account you agree to our <a href="#" style="color:dodgerblue">Terms &amp; Privacy</a>.</p>
						
								${message}
								<div class="col-12">
									<ul class="actions">
										<li><input type="submit" value="Sign Up" class="primary"/></li>
										<li><input type="button" value="Cancel" class="cancelbtn" onclick="window.location.href='index.jsp'"/></li>
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
		        $("#signupForm").validate({
		            rules: {
		            	username: {
		                    required: true,
		                    email: true
		                },
		         
		                password: "required",
		                
		                password-repeat: "required",
		                
		                firstname: "required",
		                
		                lastname: "required",
		                
		                nationalid: "required",
		                
		                dateofbirth: "required",
		                
		                sex: "required",
		                
		                address: "required",
		                
		                city: "required",
		                
		                weight: "required",
		                	
		                height: "required",
		            },
		             
		            messages: {
		            	username: {
		                    required: "Please enter email",
		                    email: "Please enter a valid email address"
		                },
		                 
		                password: "Please enter password",
		                
		                password-repeat: "Please re-enter password",
		                
		                firstname: "Please enter first name",
		                
		                lastname: "Please enter last name",
		                
		                nationalid: "Please enter national ID",
		                
		                dateofbirth: "Please enter date of birth",
		                
		                sex: "Please enter sex",
		                
		                address: "Please enter address",
		                
		                city: "Please enter city",
		                
		                weight: "Please enter weight",
		                	
		                height: "Please enter height",
		            }
		        });
		 
		    });
		    
		    $(".buttonClick").click(function () {

		        if ($("input[type=text]").filter(function () {
		            return this.value.match(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])([a-zA-Z0-9]{8})$/);
		        })) {
		            $("div").text("pass");
		        } else {
		            $("div").text("fail");
		        }

		    });
		</script>
	</body>
</html>
