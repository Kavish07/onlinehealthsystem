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
		        	rerturn false;
		        }
		        else
		        {
		        	alert("Your request has been sent to the Doctor. He will reply shortly.");
		        	location.href="home.jsp";
		        	rerturn true;
		        }
			}
		</script>
	</head>
	<body class="is-preload">
		<div id="page-wrapper">
			<header id="header" class="alt">
			<h1><a href="admin.jsp">Online Health System</a></h1>
				<nav>
					<a href="#menu">Menu</a>
				</nav>
			</header>
			<nav id="menu">
				<div class="inner">
					<h2>Menu</h2>
					<ul class="links">
						<li><a href="admin.jsp">Home</a></li>
						<li><a href="appointments3.jsp">Appointments</a></li>
						<li><a href="treatments3.jsp">Treatments</a></li>
						<li><a href="changepwd3.jsp">Change password</a></li>
						<li><a href="DoctorLogoutServlet?loginID=${doctor.getLoginID()}" style="width:auto;">Logout</a></li>
					</ul>
					<a href="#" class="close">Close</a>
				</div>
			</nav>

			<!-- Wrapper -->
			<section id="wrapper">
				<!-- Content -->
				<div class="wrapper">
					<div>
						<h3 class="major">Appointments</h3>
						<div class="container">
							<div class="table-wrapper">
								<table class="alt">
									<thead>
										<tr>
											<th>Date of appointment</th>
											<th>Location</th>
											<th>Department</th>
											<th>Doctor</th>
											<th>Patient</th>
											<th>Status</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="appointment" items="${listAppointments}">
											<tr id="${appointment.appointmentID}">
												<td><input type="date" name="date${appointment.appointmentID}" value="${appointment.appointmentDate}" style="color: black;" disabled/></td>
	                							<td><input type="text" name="location${appointment.appointmentID}" value="${appointment.location}" disabled/></td>
	                							<td><input type="text" name="department${appointment.appointmentID}" value="${appointment.departmentName}" disabled/></td>
	                							<td><input type="text" name="doctor${appointment.appointmentID}" value="${appointment.doctorName}" disabled /></td>
						    					<td><input type="text" name="patient${appointment.appointmentID}" value="${appointment.patientName}" disabled/></td>
	                    						<c:if test="${appointment.approved}">
	                    							<td><input type="text" name="status${appointment.appointmentID}" value="Approved" disabled/></td>
												</c:if>
												<c:if test="${!appointment.approved}">
													<td><input type="text" name="status${appointment.appointmentID}" value="Not approved" disabled/></td>
												</c:if>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
						</div>
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
