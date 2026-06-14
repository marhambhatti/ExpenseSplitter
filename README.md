# Expense Splitter Application

A desktop expense management system built with **Java Swing** and **MySQL** for tracking shared expenses, splitting bills fairly, and settling balances across groups.

---

## Student Info

| Field | Details |
|---|---|
| **Name** | Muhammad Arham Bhatti |
| **Project** | Expense Splitter |
| **Language** | Java (Swing) |
| **Database** | MySQL 8.0 |
| **IDE** | IntelliJ IDEA |

---

## Project Overview

Expense Splitter helps users manage shared costs in groups such as flatmates, office teams, or travel companions. It provides a structured way to record expenses, apply different split strategies, and keep track of who owes what.

The application follows a clean **MVC architecture** and uses:

- **Strategy Pattern** for flexible expense splitting
- **DAO Pattern** for database operations
- **Singleton Pattern** for database connection management
- **FlatLaf** for a modern UI
- **Ikonli** for scalable icons
- **LGoodDatePicker** for user-friendly date selection

---

## Features

- **User Authentication** — Register, login, and forgot-password flow
- **Group Management** — Create and manage multiple expense groups
- **Expense Tracking** — Add expenses with description, amount, date, category, payer, and participants
- **Multiple Split Strategies** — Equal, Custom, and Percentage-based splitting
- **Dashboard Overview** — See total you owe, owed to you, and net balance
- **Ledger View** — Filterable expense table with CSV export
- **Settlement Tracking** — Mark balances as settled and track outstanding items
- **Search & Filter** — Search by keyword, group, category, payer, or date range
- **Recent Activity Feed** — View recent actions on the dashboard
- **Modern UI** — FlatLaf 3.7.1 with Ikonli FontAwesome icons

---

## Installation Steps

### Prerequisites

- Java JDK 17 or higher
- MySQL Server 8.0 or higher
- IntelliJ IDEA or any Java IDE

### 1) Set Up the Database

Open your MySQL client and run:

```sql
database/expense_splitter_db.sql
```

This creates the database tables and seeds the default categories.

### 2) Configure Database Connection

Open:

```text
src/com/expensesplitter/dao/DatabaseConnection.java
```

Update the credentials to match your local MySQL setup:

```java
private static final String URL = "jdbc:mysql://localhost:3306/expense_splitter_db";
private static final String USER = "root";
private static final String PASSWORD = "";
```

### 3) Add Library Dependencies

Add the JAR files from the `lib/` folder to your project:

- `flatlaf-3.7.1.jar`
- `ikonli-core-12.3.1.jar`
- `ikonli-swing-12.3.1.jar`
- `ikonli-fontawesome5-pack-12.3.1.jar`
- `LGoodDatePicker-11.2.1.jar`
- `mysql-connector-j-8.4.0.jar`

### 4) Run the Application

Run:

```text
src/com/expensesplitter/Main.java
```

Then register a new account and start using the application.

---

## Screenshots


### Login Screen
![Login Screen](screenshots/login.png)

### Register Screen
![Register Screen](screenshots/register.png)

### Dashboard
![Dashboard](screenshots/dashboard.png)

### Groups
![Groups](screenshots/groups.png)

### Expenses
![Expenses](screenshots/expenses.png)

### Ledger
![Ledger](screenshots/ledger.png)

### Settlements
![Settlements](screenshots/settlements.png)

### Search & Filter
![Search & Filter](screenshots/search.png)

---

## Project Structure

```text
ExpenseSplitter/
├── src/
│   └── com/expensesplitter/
│       ├── Main.java
│       ├── dao/             # Database access objects
│       ├── models/          # Data models (User, Group, Expense, Settlement)
│       ├── ui/              # Swing UI frames and panels
│       ├── strategies/      # Split strategy implementations
│       ├── layouts/         # Custom layout managers
│       └── uifactory/       # Reusable UI components
├── database/
│   └── expense_splitter_db.sql
├── lib/                     # External JAR dependencies
├── screenshots/             # Application screenshots
└── README.md
```

---

## Dependencies

| Library | Version | Purpose |
|---|---:|---|
| FlatLaf | 3.7.1 | Modern flat UI look and feel |
| Ikonli Core | 12.3.1 | Icon framework |
| Ikonli Swing | 12.3.1 | Swing icon integration |
| Ikonli FontAwesome5 | 12.3.1 | FontAwesome icon pack |
| LGoodDatePicker | 11.2.1 | Date picker component |
| MySQL Connector/J | 8.4.0 | MySQL JDBC driver |

---

## Future Enhancements

- Email notifications for unsettled balances
- Android/iOS companion app
- Recurring expense support
- Multi-currency handling
- Expense charts and analytics
- Cloud synchronization
- PDF report export
- Group invitations via email link

---

## License

This project is developed for academic purposes. Replace this section with your preferred license if you plan to publish the project publicly.
