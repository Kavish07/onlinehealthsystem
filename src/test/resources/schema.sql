-- Schema for the onlinehealthsystem database, reverse-engineered from the
-- SQL embedded in com.onlinehealthsystem's DAO classes (no schema file
-- existed in the repo). Used only by the Testcontainers-backed DAO/servlet
-- integration tests to provision a disposable MySQL instance; the
-- production DAOs remain unaware this exists.
--
-- Patients.PatientID / Doctors.DoctorID are each table's own surrogate key
-- (used only by the list* queries' "p.PatientID"/"d.DoctorID" selects).
-- Appointments/Treatments.PatientID/DoctorID instead store the person's
-- Login.LoginID directly (see the join conditions "a.PatientID = p.LoginID"
-- etc. in AppointmentDAO/TreatmentDAO) - the column names collide with, but
-- are unrelated to, Patients.PatientID/Doctors.DoctorID.
--
-- Foreign keys are deliberately omitted: the exact referential-integrity
-- rules of the real schema aren't known (there's no schema file to go on),
-- and guessing them wrong would fail tests for reasons unrelated to the DAO
-- code under test. Column names/types are what the DAOs' SQL actually
-- depends on, so that's what this schema gets right.

CREATE TABLE Login (
    LoginID INT AUTO_INCREMENT PRIMARY KEY,
    UserName VARCHAR(100) NOT NULL UNIQUE,
    UserPassword VARCHAR(255) NOT NULL,
    Salt VARCHAR(255) NOT NULL
);

CREATE TABLE Departments (
    DepartmentCode VARCHAR(20) PRIMARY KEY,
    DepartmentName VARCHAR(100) NOT NULL
);

CREATE TABLE Medicines (
    MedicineCode VARCHAR(20) PRIMARY KEY,
    MedicineName VARCHAR(100) NOT NULL
);

CREATE TABLE Patients (
    PatientID INT AUTO_INCREMENT PRIMARY KEY,
    LoginID INT NOT NULL UNIQUE,
    FirstName VARCHAR(100),
    LastName VARCHAR(100),
    NationalID VARCHAR(50),
    DateOfBirth DATE,
    Sex VARCHAR(10),
    Address VARCHAR(255),
    City VARCHAR(100),
    Weight INT,
    Height INT
);

CREATE TABLE Doctors (
    DoctorID INT AUTO_INCREMENT PRIMARY KEY,
    LoginID INT NOT NULL UNIQUE,
    FirstName VARCHAR(100),
    LastName VARCHAR(100),
    NationalID VARCHAR(50),
    DateOfBirth DATE,
    Sex VARCHAR(10),
    Address VARCHAR(255),
    City VARCHAR(100),
    Weight INT,
    Height INT,
    Qualifications VARCHAR(255),
    Speciality VARCHAR(100),
    DepartmentCode VARCHAR(20)
);

CREATE TABLE LoginHistory (
    LoginHistoryID INT AUTO_INCREMENT PRIMARY KEY,
    LoginID INT NOT NULL,
    LoginDateTime DATETIME,
    LogoutDateTime DATETIME
);

CREATE TABLE Appointments (
    AppointmentId INT AUTO_INCREMENT PRIMARY KEY,
    Date DATE,
    Location VARCHAR(255),
    DepartmentCode VARCHAR(20),
    PatientID INT NOT NULL COMMENT 'stores Login.LoginID of the patient',
    DoctorID INT NOT NULL COMMENT 'stores Login.LoginID of the doctor',
    Approved BOOLEAN DEFAULT FALSE
);

CREATE TABLE Bill (
    BillID INT AUTO_INCREMENT PRIMARY KEY,
    Date DATE,
    Amount INT
);

CREATE TABLE Treatments (
    TreatmentId INT AUTO_INCREMENT PRIMARY KEY,
    Date DATE,
    Location VARCHAR(255),
    DepartmentCode VARCHAR(20),
    TreatmentName VARCHAR(100),
    MedicineCode VARCHAR(20),
    PatientID INT NOT NULL COMMENT 'stores Login.LoginID of the patient',
    DoctorID INT NOT NULL COMMENT 'stores Login.LoginID of the doctor',
    BillID INT
);
