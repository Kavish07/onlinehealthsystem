<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
	<head>
		<title>My Bills</title>
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
						<li><a href="bills.jsp">My Bills</a></li>
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
						<h3 class="major">My Bills</h3>
						<div class="container">
							<div class="table-wrapper">
								<table class="alt">
									<thead>
										<tr>
											<th>Bill date</th>
											<th>Amount</th>
											<th>Status</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach var="bill" items="${listBills}" varStatus="row">
											<tr>
												<td><input type="date" name="billDate${row.index}" value="${bill.billDate}" style="color: black;" aria-label="Bill date" disabled/></td>
												<td><input type="text" name="amount${row.index}" value="${bill.amount}" aria-label="Amount" disabled/></td>
												<td><input type="text" name="status${row.index}" value="<c:out value="${bill.paid ? 'Paid' : 'Unpaid'}" />" aria-label="Status" disabled/></td>
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
