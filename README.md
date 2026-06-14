# 💸 Expense Splitter Application

---

## 👤 Student Info

| Field        | Details               |
|--------------|-----------------------|
| **Name**     | Muhammad Arham Bhatti |
| **Project**  | Expense Splitter      |
| **Language** | Java (Swing)          |
| **Database** | MySQL                 |

---

## 📋 Project Description

Expense Splitter is a desktop application built with 
**Java Swing** and **MySQL** 
that allows groups of people to track shared expenses and settle balances fairly. 
Whether you're managing household bills with flatmates, splitting costs on a team 
outing, or tracking travel expenses, Expense Splitter gives you a clean, 
organized view of who owes what.

The application follows a clean MVC architecture using the
**Strategy design pattern** for flexible expense splitting (Equal, Custom, Percentage),
the **DAO pattern** for database access, and **FlatLaf**
for a modern UI look and feel.

---

## ✨ Features

- **User Authentication** — Register, login, and forgot password (reset token via email)
- **Group Management** — Create and manage multiple expense groups (e.g., Flat Mates, Office Team)
- **Expense Tracking** — Add expenses with description, amount, date, category, payer, and participants
- **Multiple Split Strategies** — Equal split, Custom amounts, and Percentage-based splitting
- **Dashboard Overview** — Financial snapshot showing total you owe, owed to you, and net balance
- **Ledger View** — Filterable table of all expenses with export to CSV
- **Settlement Tracking** — Track outstanding balances per group and mark payments as settled
- **Search & Filter** — Search expenses by keyword, group, category, date range, or payer
- **Recent Activity Feed** — Live activity log on the dashboard
- **Modern UI** — Built with FlatLaf 3.7.1 and Ikonli FontAwesome icons

---

## 🛠️ Installation Steps

### Prerequisites
- Java JDK 17 or higher
- MySQL Server 8.0 or higher
- IntelliJ IDEA (recommended) or any Java IDE

### Step 1 — Set Up the Database
1. Open MySQL Workbench or any MySQL client
2. Run the script located at:
   ```
   database/expense_splitter_db.sql
   ```
   This creates the database, all tables, and seeds the default categories.

### Step 2 — Configure Database Connection
Open the file:
```
src/com/expensesplitter/dao/DatabaseConnection.java
```
Update the connection details to match your MySQL setup:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/expense_splitter_db";
private static final String USER     = "root";      
private static final String PASSWORD = ""; 
```

### Step 3 — Add Library Dependencies
In IntelliJ IDEA:
1. Go to **File → Project Structure → Libraries**
2. Add all `.jar` files from the `lib/` folder:
   - `flatlaf-3.7.1.jar`
   - `ikonli-core-12.3.1.jar`
   - `ikonli-swing-12.3.1.jar`
   - `ikonli-fontawesome5-pack-12.3.1.jar`
   - `LGoodDatePicker-11.2.1.jar`
   - `mysql-connector-j-8.4.0.jar`

### Step 4 — Run the Application
- Run `src/com/expensesplitter/Main.java`
- The Login screen will appear
- Register a new account and start using the app

---

## 📸 Screenshots

### Login Screen
![Login](screenshots/login.png)

### Register Screen
![Register](screenshots/register.png)

### Dashboard
![Dashboard](screenshots/dashboard.png)

### Groups
![Groups](screenshots/groups.png)

### Expenses
![Expenses](screenshots/expenses.png)

### Ledger
![Ledger](screenshots/ledger.png)

### Settlements
![Settlements](screenshots/settlements![img.png](img.png).png)

### Search & Filter
![Search](![img.png](img.png)screenshots/search.png)

---

## 🔮 Future Enhancements

- **Email Notifications** — Automated reminders for unsettled balances
- **Mobile Application** — Android/iOS companion app with REST API backend
- **Recurring Expenses** — Auto-add monthly bills (rent, internet, etc.)
- **Currency Support** — Multi-currency expenses with live exchange rates
- **Expense Charts** — Visual analytics (pie/bar charts) per group or category
- **Cloud Sync** — Online database support so groups can sync in real time
- **Export to PDF** — Generate printable settlement reports
- **Group Invitations** — Invite members via email link

---

## 📁 Project Structure

```
ExpenseSplitter/
├── src/
│   └── com/expensesplitter/
│       ├── Main.java
│       ├── dao/             # Database Access Objects
│       ├── models/          # Data models (User, Group, Expense, Settlement)
│       ├── ui/              # All Swing UI frames and panels
│       ├── strategies/      # Split strategy implementations
│       ├── layouts/         # Custom layout managers
│       └── uifactory/       # Reusable UI components (Sidebar)
├── database/
│   └── expense_splitter_db.sql
├── lib/                     # External JAR dependencies
├── screenshots/             # Application screenshots
└── README.md
```

---

## 📦 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| FlatLaf | 3.7.1 | Modern flat UI look and feel |
| Ikonli Core | 12.3.1 | Icon framework |
| Ikonli Swing | 12.3.1 | Swing icon integration |
| Ikonli FontAwesome5 | 12.3.1 | FontAwesome icon pack |
| LGoodDatePicker | 11.2.1 | Date picker component |
| MySQL Connector/J | 8.4.0 | MySQL JDBC driver |
