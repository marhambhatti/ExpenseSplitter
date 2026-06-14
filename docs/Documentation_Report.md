# Expense Splitter Application
## Project Documentation Report

---

&nbsp;

&nbsp;

&nbsp;

# COVER PAGE

---

**Project Title:** Expense Splitter Application

**Student Name:** Arham Bhatti

**Programming Language:** Java (Swing)

**Database:** MySQL 8.0

**IDE:** IntelliJ IDEA

**Submission Date:** June 14, 2026

---

&nbsp;

&nbsp;

---

## Table of Contents

1. Introduction
2. Objectives
3. Problem Statement
4. System Architecture & Design
5. Features
6. Screenshots
7. Testing
8. Conclusion
9. Future Enhancements

---

&nbsp;

---

## 1. Introduction

Managing shared expenses among a group of people — whether flatmates, colleagues, or travel companions — is a common and often frustrating challenge. Tracking who paid what, how much each person owes, and when debts are settled requires either constant manual bookkeeping or trust in memory, both of which are error-prone.

The **Expense Splitter Application** is a desktop Java application built to solve this problem. It provides a structured, database-backed system where users can create groups, record shared expenses, choose how costs are divided, and track settlements — all through a clean and modern graphical interface.

The application is developed using **Java Swing** for the GUI layer, **MySQL** as the relational database backend, and a collection of well-known open-source libraries:

- **FlatLaf 3.7.1** — for a modern, flat UI look and feel
- **Ikonli 12.3.1** with FontAwesome 5 — for scalable vector icons
- **LGoodDatePicker 11.2.1** — for user-friendly date input
- **MySQL Connector/J 8.4.0** — for JDBC database connectivity

The project follows standard software engineering principles including the **MVC (Model-View-Controller)** architecture, the **Strategy design pattern** for pluggable split algorithms, and the **DAO (Data Access Object)** pattern for clean separation between business logic and database operations.

---

&nbsp;

---

## 2. Objectives

The primary objectives of the Expense Splitter Application are:

1. **Provide a user-friendly interface** for recording and tracking shared expenses without requiring technical knowledge.

2. **Support multiple splitting strategies** — Equal split, Custom amounts per person, and Percentage-based allocation — to accommodate real-world scenarios.

3. **Enable group-based expense management** so that different sets of people (flatmates, office colleagues, travel groups) can have their expenses tracked separately.

4. **Automate balance calculation** so users always know their net financial position across all groups — how much they owe and how much they are owed.

5. **Track settlements** to allow users to mark debts as paid and maintain a clear history of financial transactions.

6. **Provide filtering and search capabilities** so users can find specific expenses quickly by group, category, date range, or payer.

7. **Export data** — allow ledger data to be exported to CSV for external use or record keeping.

8. **Apply secure authentication** including hashed password storage and a reset-token-based forgot password flow.

9. **Demonstrate clean software architecture** through proper separation of concerns: models, DAOs, strategies, and UI components in distinct packages.

---

&nbsp;

---

## 3. Problem Statement

When people share living or working spaces, shared expenses are inevitable — rent, groceries, utility bills, team lunches, and equipment purchases all need to be tracked and divided fairly.

The current common approaches each have significant drawbacks:

| Approach | Problem |
|----------|---------|
| Verbal agreements | Easily forgotten, no record |
| Spreadsheets | Hard to maintain, no automation, not real-time |
| General notes apps | No structure, no calculations |
| Cash payments | No audit trail, hard to track partial payments |

**Specific problems this application solves:**

- **Uneven payment tracking:** When one person pays for a group expense, others need to be reminded of their share — this system stores it permanently.
- **Multiple simultaneous debts:** A person may owe money to three people and be owed by two others. Manual tracking leads to confusion. The app calculates a net balance per person.
- **No historical record:** Without a system, there is no way to review past expenses or dispute claims. This app stores all expenses in a database with dates, categories, and payer information.
- **Split fairness:** Not all expenses are split equally. Sometimes one person uses more of a resource. The custom and percentage split modes allow proportional allocation.
- **Settlement ambiguity:** Without marking payments as settled, it is unclear whether a debt has been paid. The Settlements module provides a clear "Mark Settled" workflow.

---

&nbsp;

---

## 4. System Architecture & Design

### 4.1 Architecture Overview

The application uses a layered architecture:

```
┌─────────────────────────────────┐
│         UI Layer (Swing)        │  LoginFrame, MainFrame, GroupFrame,
│                                 │  ExpensesPanel, LedgerDetailsFrame,
│                                 │  SettlementFrame, SearchFilterFrame
├─────────────────────────────────┤
│       Business Logic Layer      │  Split Strategies (Equal, Custom,
│                                 │  Percentage), Session management
├─────────────────────────────────┤
│     Data Access Layer (DAO)     │  UserDAO, GroupDAO, ExpenseDAO,
│                                 │  SettlementDAO
├─────────────────────────────────┤
│       Database Layer (MySQL)    │  expense_splitter_db
└─────────────────────────────────┘
```

### 4.2 Design Patterns Used

**Strategy Pattern** — The `SplitStrategy` interface is implemented by three classes:
- `EqualSplit` — divides the total amount evenly among all participants
- `CustomSplit` — allows specifying an exact amount for each participant
- `PercentageSplit` — divides based on percentage allocation per person

**DAO Pattern** — Each database entity has a corresponding DAO:
- `UserDAO` — handles user registration, login, and lookup
- `GroupDAO` — manages group creation and membership
- `ExpenseDAO` — stores and retrieves expenses and splits
- `SettlementDAO` — records and queries settlement transactions

**Singleton Pattern** — `DatabaseConnection` provides a single shared JDBC connection.

### 4.3 Database Schema

The database consists of 7 tables:

| Table | Purpose |
|-------|---------|
| `users` | Stores user accounts (id, name, email, password_hash) |
| `groups_table` | Stores expense groups |
| `group_members` | Many-to-many: users ↔ groups |
| `categories` | Predefined expense categories (Meals, Travel, etc.) |
| `expenses` | Individual expense records |
| `expense_splits` | Per-user split amounts for each expense |
| `settlements` | Recorded payment transactions between users |

### 4.4 Package Structure

```
com.expensesplitter
├── Main.java                    Entry point
├── models/                      POJO data classes
│   ├── User.java
│   ├── Group.java
│   ├── Expense.java
│   ├── Settlement.java
│   └── Category.java
├── dao/                         Database access
│   ├── DatabaseConnection.java
│   ├── UserDAO.java
│   ├── GroupDAO.java
│   ├── ExpenseDAO.java
│   └── SettlementDAO.java
├── strategies/                  Split algorithms
│   ├── SplitStrategy.java       (interface)
│   ├── EqualSplit.java
│   ├── CustomSplit.java
│   └── PercentageSplit.java
├── ui/                          Swing UI screens
│   ├── LoginFrame.java
│   ├── RegisterFrame.java
│   ├── MainFrame.java
│   ├── GroupFrame.java
│   ├── ExpensesPanel.java
│   ├── ExpenseDialog.java
│   ├── LedgerDetailsFrame.java
│   ├── SettlementFrame.java
│   ├── SearchFilterFrame.java
│   ├── DatePickerField.java
│   ├── FrameNavigation.java
│   └── UiStyles.java
├── uifactory/
│   └── SideBar.java
└── layouts/
    └── WrapLayout.java
```

---

&nbsp;

---

## 5. Features

### 5.1 User Authentication
- **Registration:** New users provide full name, email, and password (with confirmation). Passwords are stored as hashed values.
- **Login:** Email and password authentication with validation.
- **Forgot Password:** Users enter their registered email and receive a reset token to recover access.

### 5.2 Dashboard
The main dashboard provides a real-time financial overview:
- **Total You Owe** — sum of all unsettled debts across all groups
- **Total Owed to You** — sum of all amounts others owe you
- **Overall Net Balance** — net financial position
- **Group Summary Cards** — quick balance and settlement percentage per group
- **Recent Activity Feed** — chronological log of expense additions and settlements

### 5.3 Group Management
- Create named groups (e.g., "Flat Mates", "Office Team")
- View all groups with member count and balance summary
- Color-coded group avatars auto-generated from group name initials
- Sort groups by default, name, or balance

### 5.4 Expense Management
- Add expenses with: description, amount, category, group, date, payer
- Three split types selectable per expense:
  - **EQUAL** — splits total evenly across all participants
  - **CUSTOM** — assign specific amounts to each person
  - **PERCENTAGE** — assign percentages that must sum to 100%
- View full split breakdown per expense (who paid what)
- Edit and delete existing expenses

### 5.5 Ledger
- Tabular view of all expenses across all groups
- Columns: date, description, category, amount, payer, group, split type, status
- Filter by: group, date range, category, split type
- Search by keyword
- Status badges: SETTLED, PARTIAL, UNSETTLED
- Export to CSV functionality
- Pagination support

### 5.6 Settlements
- Lists all outstanding balances grouped by group
- Shows direction of debt ("X owes you" vs "You owe X")
- "Mark Settled" button to record a payment
- Summary cards: total you owe, total owed to you, number of pending items

### 5.7 Search & Filter
- Full-text search across expense descriptions
- Multi-filter: group, category, payer, date from/to
- Results table with all relevant expense details
- Clear all filters with one click

---

&nbsp;

---

## 6. Screenshots

### 6.1 Login Screen
The application opens with a clean login form. Users enter their email and password. A "Forgot Password?" link is available for account recovery, and "Register New Account" navigates to registration.

*(See: Login Screen screenshot)*

---

### 6.2 Forgot Password Dialog
Users who have forgotten their password enter their registered email address. The system sends a reset token that can be used to regain access.

*(See: Forgot Password screenshot)*

---

### 6.3 Register Screen
New users fill in their full name, email address, password, and password confirmation. The system validates all fields before creating the account.

*(See: Register Screen screenshot)*

---

### 6.4 Dashboard
After login, the dashboard shows a financial overview. The header displays the logged-in user's name, current date/time, and a logout button. The main area shows total owed, owed to you, net balance, group summary cards, and a recent activity feed.

*(See: Dashboard screenshot)*

---

### 6.5 Groups Panel
Lists all groups the user belongs to, with a balance summary and settlement progress bar for each. A "+ Create Group" button opens a dialog to name and create a new group.

*(See: Groups screenshot)*

---

### 6.6 Expenses Panel
Displays all expenses across all groups. Each entry shows the group tag, date, split type badge, payer, and the per-person amount. Expandable rows show the full split breakdown. "You Paid" and "You Owe" badges provide at-a-glance context.

*(See: Expenses screenshot)*

---

### 6.7 Ledger Details
A comprehensive filterable table of all expenses. Includes pagination, sorting, CSV export, and row-level View/Edit/Delete actions.

*(See: Ledger screenshot)*

---

### 6.8 Settlements
Shows all outstanding balances organized by group. Green entries indicate amounts owed to the user; red entries indicate amounts the user owes. "Mark Settled" buttons record payments.

*(See: Settlements screenshot)*

---

### 6.9 Search & Filter
A dedicated search panel with keyword search and multi-dimensional filtering. Results are displayed in a table with all expense details.

*(See: Search screenshot)*

---

### 6.10 Create New Group Dialog
A simple modal dialog for creating a new expense group by entering the group name.

*(See: Create Group screenshot)*

---

### 6.11 Add New Expense Dialog
A form to add a new expense: description, amount, category dropdown, group/workspace, date picker, payer selector, and participants with split breakdown preview.

*(See: Add Expense screenshot)*

---

&nbsp;

---

## 7. Testing

### 7.1 Testing Approach

The application was tested manually through functional testing of each module. Each feature was tested with valid inputs, invalid inputs, and edge cases.

---

### 7.2 Authentication Testing

| Test Case | Input | Expected Result | Pass/Fail |
|-----------|-------|-----------------|-----------|
| Valid login | Correct email and password | Redirects to Dashboard | ✅ Pass |
| Invalid password | Correct email, wrong password | Error message shown | ✅ Pass |
| Unregistered email | Email not in database | "User not found" message | ✅ Pass |
| Empty fields | Both fields blank | Validation error shown | ✅ Pass |
| Register new user | Valid name, email, password | Account created, login works | ✅ Pass |
| Duplicate email | Register with existing email | "Email already registered" error | ✅ Pass |
| Password mismatch | Password ≠ Confirm Password | Validation error shown | ✅ Pass |

---

### 7.3 Group Management Testing

| Test Case | Input | Expected Result | Pass/Fail |
|-----------|-------|-----------------|-----------|
| Create group | Valid group name | Group appears in list | ✅ Pass |
| Create group | Empty name field | Validation error | ✅ Pass |
| View group balances | Multiple expenses recorded | Correct balance displayed | ✅ Pass |
| Group summary card | After adding expense | Balance updates on dashboard | ✅ Pass |

---

### 7.4 Expense Testing

| Test Case | Input | Expected Result | Pass/Fail |
|-----------|-------|-----------------|-----------|
| Add equal split expense | Rs. 4000, 4 members | Rs. 1000 each shown | ✅ Pass |
| Add custom split | Manual amounts entered | Amounts stored correctly | ✅ Pass |
| Add percentage split | Percentages summing to 100% | Correct amounts calculated | ✅ Pass |
| Invalid amount | Letters in amount field | Validation error | ✅ Pass |
| Missing required fields | Empty description | Form validation triggered | ✅ Pass |
| Edit expense | Change amount | Updated in ledger and balance | ✅ Pass |
| Delete expense | Remove an expense | Removed from all views | ✅ Pass |

---

### 7.5 Balance Calculation Testing

| Test Case | Scenario | Expected Result | Pass/Fail |
|-----------|----------|-----------------|-----------|
| Net balance — owed to you | You paid Rs. 5500 equally among 4 | Owed to you: Rs. 4125 | ✅ Pass |
| Net balance — you owe | Another member paid Rs. 2400 equally | You owe: Rs. 600 | ✅ Pass |
| Settlement reduces balance | Mark Rs. 1375 as settled | Balance decreases by Rs. 1375 | ✅ Pass |
| Fully settled group | All debts marked settled | Group shows "Settled 100%" | ✅ Pass |

---

### 7.6 Ledger & Search Testing

| Test Case | Input | Expected Result | Pass/Fail |
|-----------|-------|-----------------|-----------|
| Filter by group | Select "Flat Mates" | Only Flat Mates expenses shown | ✅ Pass |
| Filter by date range | May 1 – May 10, 2025 | Only matching expenses shown | ✅ Pass |
| Search by keyword | "Grocery" | Grocery run expense returned | ✅ Pass |
| Export CSV | Click Export CSV | CSV file downloaded | ✅ Pass |
| Clear filters | Click Clear | All expenses shown again | ✅ Pass |

---

### 7.7 Settlement Testing

| Test Case | Input | Expected Result | Pass/Fail |
|-----------|-------|-----------------|-----------|
| View outstanding | After adding expenses | Correct pending items shown | ✅ Pass |
| Mark as settled | Click "Mark Settled" | Balance removed from list | ✅ Pass |
| Pending count | Multiple unsettled items | Correct count displayed | ✅ Pass |

---

### 7.8 Edge Cases

| Test Case | Expected Behavior | Pass/Fail |
|-----------|-------------------|-----------|
| Zero amount expense | Validation error — amount must be > 0 | ✅ Pass |
| Single member group | No split needed — full amount to one person | ✅ Pass |
| Large amounts (Rs. 999,999) | Displayed correctly without overflow | ✅ Pass |
| Special characters in description | Stored and displayed correctly | ✅ Pass |

---

&nbsp;

---

## 8. Conclusion

The Expense Splitter Application successfully meets all defined objectives. It provides a fully functional desktop solution for tracking and splitting shared expenses among groups of users.

Key accomplishments:

- A **complete authentication system** with registration, login, and password recovery.
- **Group-based expense management** supporting up to three split strategies, giving users full flexibility in how costs are divided.
- **Automated balance calculation** that always reflects the current net financial state across all groups without manual effort.
- A **comprehensive ledger** with advanced filtering, searching, and CSV export — making the data accessible and auditable.
- A **settlement workflow** that closes the loop by allowing users to record payments and track debt resolution.
- **Clean software architecture** through consistent use of MVC, Strategy, DAO, and Singleton design patterns, making the codebase maintainable and extensible.
- A **modern user interface** using FlatLaf and Ikonli icons that provides a professional, pleasant user experience.

The project demonstrates practical application of Java programming, object-oriented design principles, relational database design, and UI development skills.

---

&nbsp;

---

## 9. Future Enhancements

The following enhancements are planned or recommended for future versions:

| # | Enhancement | Description |
|---|-------------|-------------|
| 1 | **Email Notifications** | Send automated reminders to group members when new expenses are added or settlements are due |
| 2 | **Mobile Companion App** | Develop an Android/iOS app backed by a REST API to allow expense tracking on the go |
| 3 | **Recurring Expenses** | Auto-schedule monthly bills (rent, internet, subscriptions) to repeat without manual entry |
| 4 | **Multi-Currency Support** | Allow expenses in different currencies with live exchange rate conversion |
| 5 | **Visual Analytics** | Add pie/bar chart views for expense breakdown by category, group, or time period |
| 6 | **Cloud Synchronization** | Move to an online database so group members can sync in real time from different devices |
| 7 | **PDF Report Export** | Generate formatted settlement reports and expense summaries as printable PDFs |
| 8 | **Group Invitations** | Invite new members to a group via email link instead of requiring them to already be registered |
| 9 | **Dark Mode** | Full dark theme support using FlatLaf's built-in dark look and feel variants |
| 10 | **Expense Attachments** | Attach receipt images or documents to expense entries for verification |

---

*End of Documentation Report*
