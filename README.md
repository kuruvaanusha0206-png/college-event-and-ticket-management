# UniVibe – College Event and Ticket Management System
**Academic Edition | Core Java, Swing & JDBC**

---

## 1. Project Overview
**UniVibe** is an enterprise-grade campus event organization, publishing, and ticketing management desktop application built using **Core Java (Java SE)**, **Java Swing**, and **JDBC**. It offers dedicated role-based portals for **Campus Administrators**, **Event Organizers**, and **Students**.

The project is designed to strictly demonstrate the practical application of 16 mandatory Core Java concepts, Object-Oriented Design (OOP), database transaction atomicity, procedural visual 2D QR admit pass generation, multithreaded reporting workers, and robust file I/O.

---

## 2. Default Demonstration Credentials
The application includes one-click autofill buttons on the login screen for instantaneous evaluator access:

| Role | Username | Password | Full Name / Description |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | System Administrator (Full Governance & Approvals) |
| **Organizer** | `org_sarah` | `org123` | Prof. Sarah Jenkins (CSE Department, ACM Chapter) |
| **Organizer** | `org_rahul` | `org123` | Dr. Rahul Sharma (ECE Department, Cultural Club) |
| **Student** | `student_alex` | `student123` | Alex Mercer (Roll No: CS2024-001, Sem 5) |
| **Student** | `student_priya` | `student123` | Priya Patel (Roll No: IT2024-042, Sem 5) |
| **Student** | `student_david` | `student123` | David Kim (Roll No: EC2024-019, Sem 3) |

---

## 3. Core Java Concepts Demonstrated

| # | Core Java Concept | Concrete Implementation in UniVibe |
| :-: | :--- | :--- |
| **1** | **Classes & Objects** | Encapsulated entity classes: `User`, `Student`, `Organizer`, `Admin`, `Event`, `Booking`, `Ticket`, `Payment`, `Feedback`. |
| **2** | **Encapsulation** | Private variables, getters/setters with validations (e.g., star ratings constrained 1-5, positive seat counts). |
| **3** | **Inheritance** | Base abstract class `User` extended by `Student`, `Organizer`, and `Admin`. |
| **4** | **Polymorphism** | Dynamic method dispatch: `user.getRole()`, `user.getDashboardTitle()`, polymorphic DAO mapping. |
| **5** | **Abstraction** | Abstract class `User` defining abstract contract methods implemented by subclasses. |
| **6** | **Interfaces** | `Payable`, `Printable`, `Searchable`, `Reportable`, `Auditable`. |
| **7** | **Constructor Overloading** | Default, convenience, and full-entity constructors across all model classes. |
| **8** | **Method Overloading & Overriding** | Overloading: `EventManager.searchEvents(...)`, `Payment.formatReceipt(...)`; Overriding: `equals()`, `hashCode()`, `toString()`. |
| **9** | **Static & Final Keywords** | `DatabaseConnection.getInstance()`, `final` constants, static mathematical counters, final utility classes (`PasswordHasher`, `QRMatrixGenerator`, `FileHandler`). |
| **10** | **Arrays & Strings** | 2D `boolean[][]` matrix in `QRMatrixGenerator`, regex email/phone validation in `ValidationHelper`, tabular string formatting. |
| **11** | **Collections** | `ArrayList` (tickets/events), `HashMap` (category revenue & feedback caches), `HashSet` (unique venues & attendee tracking). |
| **12** | **Exception Handling** | Robust `try-catch-finally` with `try-with-resources` ensuring zero connection/stream leaks. |
| **13** | **User-Defined Exceptions** | `SeatUnavailableException`, `DuplicateBookingException`, `InvalidBookingException`, `AuthenticationException`, `EventNotFoundException`. |
| **14** | **File Handling** | Ticket generation to disk (`tickets/*.txt`), audit trail appending (`logs/univibe_audit.log`), CSV/TXT report exports (`reports/*`). |
| **15** | **Multithreading** | `ReportGenerator` implements `Runnable` executing on background worker threads without freezing the Swing GUI Event Dispatch Thread. |
| **16** | **JDBC & Transactions** | Atomic transactions via `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()` ensuring seat reservation consistency. |

---

## 4. Quick Execution Instructions

### Option A: Using Windows Batch Scripts
1. Open PowerShell or Command Prompt in this folder.
2. Run `build.bat` to compile all source files into the `bin\` directory.
3. Run `run.bat` to launch the GUI Application.
4. Run `test.bat` to execute the automated 23-point system test suite.

### Option B: Using Command Line Directly
```powershell
# 1. Compile
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/univibe/interfaces/*.java src/com/univibe/exception/*.java src/com/univibe/model/*.java src/com/univibe/util/*.java src/com/univibe/database/*.java src/com/univibe/dao/*.java src/com/univibe/service/*.java src/com/univibe/ui/*.java src/com/univibe/test/*.java src/com/univibe/Main.java

# 2. Run Application
java -cp "bin;lib/*" com.univibe.Main

# 3. Run Automated System Tests
java -cp "bin;lib/*" com.univibe.test.UniVibeSystemTest
```

### Database Setup:
- **MySQL (Primary)**: Import `sql/univibe_schema.sql` into your local MySQL server (port 3306). Configure credentials in `univibe_db.properties`.
- **Zero-Config Fallback (Embedded)**: If MySQL is not running, the application automatically initializes an embedded local database (`univibe.db`) so evaluators can run the program immediately without pre-installing or starting MySQL!
