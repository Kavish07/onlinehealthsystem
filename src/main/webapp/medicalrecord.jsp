<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Treatments</title>
		<meta charset="ISO-8859-1">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="stylesheet" href="assets/css/main.css" />
		<link rel="stylesheet" href="assets/css/noscript.css" />
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
			
			<section id="wrapper">
				<!-- Content -->
				<div class="wrapper">
					<div class="inner">
						<h3 class="major">New medical record</h3>
						<form name="appointmentForm" action="NewTreatmentServlet" method="post">
							<div class="container">
								<label for="dateoftreatment"><b>Date of treatment</b></label>
						    	<input type="date" placeholder="Enter Date of Treatment" name="dateoftreatment" style="color: black;" required />
						    	
						    	<label for="location"><b>Location</b></label>
						    	<input type="text" name="location" value="Pamplemousses" disabled />
						    	
								<label for="department">Department</label>
								<select id="department" name="department">
									<option value="">-</option>
  									<option value="A01">Emergency Ward</option>
  									<option value="A02">Maternity Ward</option>
  									<option value="A03">Radiology Department</option>
  									<option value="A04">ICU Ward</option>
								</select>
								
								<label for="treatment"><b>Treatment</b></label>
						    	<input type="text" placeholder="Enter Treatment" name="treatment" required />
						    	
						    	<label for="medicines"><b>Medicines</b></label>
						    	<input type="text" placeholder="Enter Medicines" name="medicines" required />
								
								<label for="patient">Patient</label>
								<select id="patient" name="patient">
									<option value="">-</option>
  									<c:forEach items="${listPatients}" var="patient">
            							<option value="${patient.loginID}">${patient.firstName} ${patient.lastName}</option>
        							</c:forEach>
								</select>
								
								<label for="bill"><b>Bill amount</b></label>
						    	<input type="text" placeholder="Enter Bill amount" name="bill" required />
								
								<label for="dateofbill"><b>Date of bill</b></label>
						    	<input type="date" placeholder="Enter Date of Bill" name="dateofbill" style="color: black;" required />
								
								<label for="message">Message</label>
								<textarea name="message" id="message" rows="6"></textarea>
								
								<div>
								<ul class="actions">
									<li><input type="submit" value="Create" class="primary"/></li>
									<li><input type="reset" value="Reset" /></li>
									<li><input type="button" value="Cancel" class="cancelbtn" onclick="window.location.href='home2.jsp'"/></li>
								</ul>
								</div>
								
								<input type="hidden" name="doctorID" value="${doctor.getLoginID()}" />
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
	</body>
</html>
