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
					<div>
						<h3 class="major">Treatment</h3>
						<div class="container">
							<div class="table-wrapper">
								<table class="alt">
									<thead>
										<tr>
											<th>Date of treatment</th>
											<th>Location</th>
											<th>Department</th>
											<th>Doctor</th>
											<th>Treatment Name</th>
											<th>Medicines</th>
											<th>Bill date</th>
											<th>Bill amount</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="treatment" items="${listTreatments}">
											<tr id="${treatment.treatmentID}">
												<td><input type="date" name="treatementDate${treatment.treatmentID}" value="${treatment.treatmentDate}" style="color: black;" disabled/></td>
	                							<td><input type="text" name="location${treatment.treatmentID}" value="${treatment.location}" disabled/></td>
	                							<td><input type="text" name="department${treatment.treatmentID}" value="${treatment.departmentName}" disabled/></td>
	                							<td><input type="text" name="doctor${treatment.treatmentID}" value="${treatment.doctorName}" disabled /></td>
						    					<td><input type="text" name="treatmentName${treatment.treatmentID}" value="${treatment.treatmentName}" disabled/></td>
	                    						<td><input type="text" name="medicines${treatment.treatmentID}" value="${treatment.medicineName}" disabled/></td>
	                    						<td><input type="date" name="billDate${treatment.treatmentID}" value="${treatment.billDate}" style="color: black;" disabled/></td>
												<td><input type="text" name="billAmount${treatment.treatmentID}" value="${treatment.billAmount}" disabled/></td>
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
