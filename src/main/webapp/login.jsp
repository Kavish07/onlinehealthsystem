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
						<li><a href="login2.jsp" style="width:auto;">Doctor Login</a></li>
					</ul>
					<a href="#" class="close">Close</a>
				</div>
			</nav>

			<!-- Wrapper -->
			<section id="wrapper">
				<!-- Content -->
				<div class="wrapper">
					<div class="inner">
						<h3 class="major">Login</h3>
						<form name="loginForm" action="PatientLoginServlet" method="post">
							<div>
								<p>Please enter username and password to login.</p>
								<label for="username"><b>Username</b></label> 
								<input type="email" placeholder="Enter Email" name="username" required />
							
								<label for="password"><b>Password</b></label> 
								<input type="password" placeholder="Enter Password" name="password" required size="30" />
								<hr>
								
								<input type="checkbox" id="remember" name="remember">
								<label for="remember">Remember me</label>
								
								<p><a href="#">Forgot password?</a></p>
								
								${message}
								<div class="col-12">
									<ul class="actions">
										<li><input type="submit" value="Login" class="primary"/></li>
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
		        $("#loginForm").validate({
		            rules: {
		                username: {
		                    required: true,
		                    email: true
		                },
		         
		                password: "required",
		            },
		             
		            messages: {
		            	username: {
		                    required: "Please enter email",
		                    email: "Please enter a valid email address"
		                },
		                 
		                password: "Please enter password"
		            }
		        });
		 
		    });
		</script>
	</body>
</html>
