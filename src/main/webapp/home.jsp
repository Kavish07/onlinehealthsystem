<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<title>Online Health System</title>
		<meta charset="ISO-8859-1">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="stylesheet" href="assets/css/main.css" />
		<link rel="stylesheet" href="assets/css/noscript.css" />
	</head>
	<body class="is-preload">
		<div id="page-wrapper">
			<header id="header" class="alt">
			<h1><a href="home.jsp">Online Health System</a></h1>
				<nav>
					<a href="#menu">Menu</a>
				</nav>
			</header>
			<nav id="menu">
				<div class="inner">
					<h2>Menu</h2>
					<ul class="links">
						<li><a href="appointments.jsp">Appointments</a></li>
						<!--<li><a href="ListTreatmentsServlet=${patient.getLoginID()}">Treatments</a></li> -->
						<li><a href="treatments.jsp">Treatments</a></li>
						<li><a href="changepwd.jsp">Change password</a></li>
						<li><a href="PatientLogoutServlet?loginID=${patient.getLoginID()}" style="width:auto;">Logout</a></li>
					</ul>
					<a href="#" class="close">Close</a>
				</div>
			</nav>

			<!-- Banner -->
			<section id="banner">
				<div class="inner">
					<div class="logo"><span class="icon fa-gem"></span></div>
					<h2>Welcome to the Online Health System of XYZ Medical Clinic.</h2>
				</div>
			</section>
			
			<section id="wrapper">
				<section id="one" class="wrapper spotlight style1">
					<div class="inner">
						<a href="#" class="image"><img src="images/pic01.jpg" alt="" /></a>
						<div class="content">
							<h2 class="major">Appointments</h2>
							<p>Take appointments with doctors.</p>
						</div>
					</div>
				</section>

				<section id="two" class="wrapper alt spotlight style2">
					<div class="inner">
						<a href="#" class="image"><img src="images/pic02.jpg" alt="" /></a>
						<div class="content">
							<h2 class="major">Treatments</h2>
							<p>Check your medical treatments and reports.</p>
						</div>
					</div>
				</section>
			</section>
		</div>
	
		<!-- Scripts -->
		<script src="assets/js/jquery.min.js"></script>
		<script src="assets/js/jquery.scrollex.min.js"></script>
		<script src="assets/js/browser.min.js"></script>
		<script src="assets/js/breakpoints.min.js"></script>
		<script src="assets/js/util.js"></script>
		<script src="assets/js/main.js"></script>
	</body>
</html>
