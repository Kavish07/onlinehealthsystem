<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Appointments</title>
		<meta charset="ISO-8859-1">
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
		<link rel="stylesheet" href="assets/css/main.css" />
		<link rel="stylesheet" href="assets/css/noscript.css" />
		<script type="text/javascript" src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
		<script type="text/javascript">
			function checkForm() {
				var checked = $('#human').is(":checked");
		        if (checked == 0)
		        {
		        	alert("You're not human.");
		        	return false;
		        }
		        else
		        {
		        	alert("Your request has been sent to the Doctor. He will reply shortly.");
		        	location.href="home.jsp";
		        	return true;
		        }
			}
		</script>
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
						<li><a href="treatments.jsp">Treatments</a></li>
						<li><a href="changepwd.jsp">Change password</a></li>
						<li><a href="PatientLogoutServlet?loginID=${patient.getLoginID()}" style="width:auto;">Logout</a></li>
					</ul>
					<a href="#" class="close">Close</a>
				</div>
			</nav>

			<!-- Wrapper -->
			<section id="wrapper">
				<!-- Content -->
				<div class="wrapper">
					<div class="inner">
						<h3 class="major">Appointment</h3>
						<form name="appointmentForm"  onsubmit="return checkForm()" action="NewAppointmentServlet" method="post">
							<div class="container">	
								<label for="dateofappointment"><b>Date of appointment</b></label>
						    	<input type="date" placeholder="Enter Date of appointment" name="dateofappointment" style="color: black;" required />
						    	
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
								
								<label for="doctor">Doctor</label>
								<select id="doctor" name="doctor">
									<option value="">-</option>
  									<c:forEach items="${listDoctors}" var="doctor">
            							<option value="${doctor.loginID}">${doctor.firstName} ${doctor.lastName}</option>
        							</c:forEach>
								</select>
								
								<label for="message">Message</label>
								<textarea name="message" id="message" rows="6"></textarea>
								
								<input type="checkbox" id="human" name="human" value="human">
								<label for="human">Not a robot</label>
								
								<div>
								<ul class="actions">
									<li><input type="submit" value="Create" class="primary"/></li>
									<li><input type="reset" value="Reset" /></li>
									<li><input type="button" value="Cancel" class="cancelbtn" onclick="window.location.href='home.jsp'"/></li>
								</ul>
								</div>
								
								<input type="hidden" name="patientID" value="${patient.getLoginID()}" />
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
