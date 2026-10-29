# OnlineHealthSystem

Patient/doctor appointment and treatment management system, currently split between a legacy Servlet/JDBC stack (`com.onlinehealthsystem`) and an in-progress Spring Boot/JPA migration (`com.system.OnlineHealthSystem`).

## Language

**Login**:
The shared credential record for any account — patient, doctor, or admin — holding one row per account's username, hashed password, and salt. One Login underlies exactly one Patient or Doctor.
_Avoid_: Account, User, Credentials

**LoginHistory**:
An audit record of a single session's login/logout timestamps for a Login. Written on login, stamped with a logout time on logout.
_Avoid_: Session, AuditLog

**Role**:
What kind of account a Login is — Patient, Doctor, or Admin. Not currently modeled as data anywhere: "Admin" today is inferred by comparing a Doctor's username against the literal `admin@gmail.com` at each check site, rather than stored as a Role.
_Avoid_: Permission, Type

**Admin**:
The Role granted to exactly one bootstrapped Doctor account, with authority over patient/doctor records beyond a regular Doctor's own. Currently a convention (one hardcoded username), not an enforced Role — see [[future-improvements-review]].
_Avoid_: Superuser, Root
