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
					<div>
						<h3 class="major">Update medical record</h3>
						<form name="appointmentForm" action="UpdateTreatmentServlet" method="post">
							<div class="container">
								<div class="table-wrapper">
									<table class="alt">
										<thead>
											<tr>
												<th>Date of treatment</th>
												<th>Location</th>
												<th>Department</th>
												<th>Patient</th>
												<th>Doctor</th>
												<th>Treatment Name</th>
												<th>Medicines</th>
												<th>Bill date</th>
												<th>Bill amount</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<c:forEach var="treatment" items="${listAllTreatments}">
												<tr id="${treatment.treatmentID}">
													<td>
														<input type="hidden" name="treatmentID" value="${treatment.treatmentID}" />
														<input type="date" name="dateoftreatment" value="${treatment.treatmentDate}" style="color: black;" />
													</td>
	                								<td><input type="text" name="location" value="${treatment.location}" /></td>
	                								<td>
	                									<select id="department" name="department">
															<option value="${treatment.departmentCode}">${treatment.departmentName}</option>
  															<option value="A01">Emergency Ward</option>
  															<option value="A02">Maternity Ward</option>
  															<option value="A03">Radiology Department</option>
  															<option value="A04">ICU Ward</option>
														</select>
	                								</td>
	                								<td>
	                									<input type="text" name="patient" value="${treatment.patientName}" disabled />
	                									<input type="hidden" name="patientID" value="${treatment.patientID}" />
	                								</td>
	                								<td>
	                									<input type="text" name="doctor" value="${treatment.doctorName}" disabled />
	                									<input type="hidden" name="doctorID" value="${treatment.doctorID}" />
	                								</td>
						    						<td><input type="text" name="treatment" value="${treatment.treatmentName}" /></td>
	                    							<td><input type="text" name="medicines" value="${treatment.medicineName}" /></td>
	                    							<td>
	                    								<input type="date" name="dateofbill" value="${treatment.billDate}" style="color: black;" />
	                    								<input type="hidden" name="billID" value="${treatment.billID}" />
	                    							</td>
													<td><input type="text" name="bill" value="${treatment.billAmount}" /></td>
													<td><input type="submit" value="Update" class="primary" /></td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
									
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
	</body>
</html>
