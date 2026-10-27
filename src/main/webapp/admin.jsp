<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
						<section>
							<h3 class="major">Patients list</h3>
							<form name="deleteForm" action="PatientDeleteServlet" method="post">
								<div class="table-wrapper">
									<table class="alt">
										<thead>
											<tr>
												<th>Name</th>
												<th>National ID</th>
												<th>Email</th>
												<th>Date of birth</th>
												<th>Sex</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<c:forEach var="patient" items="${listPatients}">
	                							<tr id="${patient.getLoginID()}">
	                								<td>
	                									<input type="text" name="name${patient.getLoginID()}" value="${patient.getFirstName()} ${patient.getLastName()}" disabled/>
	                									<input type="hidden" name="loginID" value="${patient.getLoginID()}" />
	                								</td>
	                								<td><input type="text" name="national${patient.getLoginID()}" value="${patient.getNationalID()}" disabled/></td>
	                								<td><input type="text" name="email${patient.getLoginID()}" value="${patient.getUserName()}" disabled/></td>
	                								<td><input type="date" name="dateofbirth${patient.getLoginID()}" value="${patient.getDateOfBirth()}" style="color: black;" disabled /></td>
						    						<td><input type="text" name="sex${patient.getLoginID()}" value="${patient.getSex()}" disabled/></td>
	                    							<td><input type="submit" value="Delete" class="primary" /></td>
								                </tr>
								            </c:forEach>
										</tbody>
									</table>
								</div>
							</form>
						</section>
						
						<section>
							<form name="registerForm" action="DoctorRegisterServlet" method="post">
								<div class="col-12">
									<ul class="actions">
										<li><a href="doctorSignup.jsp">Register new doctor</a></li>
									</ul>
								</div>
							</form>
						</section>
						
						<section>
							<h3 class="major">Doctors list</h3>
							<form name="deleteForm" action="DoctorDeleteServlet" method="post">
								<div class="table-wrapper">
									<table class="alt">
										<thead>
											<tr>
												<th>Name</th>
												<th>National ID</th>
												<th>Email</th>
												<th>Date of birth</th>
												<th>Sex</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<c:forEach var="doctor" items="${listDoctors}">
	                							<tr id="${doctor.getLoginID()}">
	                								<td>
	                									<input type="text" name="name${doctor.getLoginID()}" value="${doctor.getFirstName()} ${doctor.getLastName()}" disabled/>
	                									<input type="hidden" name="loginID" value="${doctor.getLoginID()}" />
	                								</td>
	                								<td><input type="text" name="national${doctor.getLoginID()}" value="${doctor.getNationalID()}" disabled/></td>
	                								<td><input type="text" name="email${doctor.getLoginID()}" value="${doctor.getUserName()}" disabled/></td>
	                								<td><input type="date" name="dateofbirth${doctor.getLoginID()}" value="${doctor.getDateOfBirth()}" style="color: black;" disabled /></td>
						    						<td><input type="text" name="sex${doctor.getLoginID()}" value="${doctor.getSex()}" disabled/></td>
	                    							<td><input type="submit" value="Delete" class="primary" /></td>
								                </tr>
								            </c:forEach>
										</tbody>
									</table>
								</div>
							</form>
						</section>
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
