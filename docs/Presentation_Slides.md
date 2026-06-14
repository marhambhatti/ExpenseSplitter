# Expense Splitter — Presentation Slides

---

---

# SLIDE 1 — TITLE SLIDE

---

# 💸 Expense Splitter Application

**A Java Desktop Application for Managing Shared Expenses**

&nbsp;

**Presented by:** Arham Bhatti

**Technology:** Java Swing + MySQL

**Date:** June 14, 2026

---

---

# SLIDE 2 — THE PROBLEM

---

## The Problem

> *"Who paid for the groceries last week?"*
> *"Wait, did I pay you back already?"*
> *"I think you owe me from the electricity bill…"*

&nbsp;

### Managing shared expenses is painful:

- 🗣️ Verbal agreements get **forgotten**
- 📊 Spreadsheets are **hard to maintain**
- 📝 Notes apps have **no calculation or structure**
- 💸 No clear record of **who owes what**

&nbsp;

### The result:
**Confusion, arguments, and broken friendships.**

---

---

# SLIDE 3 — THE SOLUTION

---

## The Solution — Expense Splitter

A **desktop application** that lets groups of people:

| ✅ | What it does |
|----|--------------|
| 👥 | Create **groups** for any shared context |
| 📌 | Record **expenses** with all details |
| ⚖️ | **Split costs** equally, by custom amount, or by percentage |
| 📊 | Track **who owes what** automatically |
| ✔️ | **Settle debts** and mark payments done |
| 🔍 | **Search and filter** all expenses |

---

---

# SLIDE 4 — TECHNOLOGY STACK

---

## Technology Stack

### Language & UI
- **Java** — Core programming language
- **Java Swing** — Desktop GUI framework
- **FlatLaf 3.7.1** — Modern flat look and feel
- **Ikonli + FontAwesome 5** — Vector icon library

### Database
- **MySQL 8.0** — Relational database
- **MySQL Connector/J 8.4.0** — JDBC driver

### Architecture Patterns
- **MVC** — Model-View-Controller separation
- **Strategy Pattern** — Pluggable split algorithms
- **DAO Pattern** — Clean database access layer
- **Singleton** — Shared database connection

---

---

# SLIDE 5 — SYSTEM ARCHITECTURE

---

## System Architecture

```
┌───────────────────────────────────────┐
│            UI Layer (Swing)           │
│  Login · Register · Dashboard · ...  │
├───────────────────────────────────────┤
│         Business Logic Layer          │
│    EqualSplit · CustomSplit ·         │
│    PercentageSplit · Session          │
├───────────────────────────────────────┤
│       Data Access Layer (DAO)         │
│  UserDAO · GroupDAO · ExpenseDAO ·    │
│  SettlementDAO                        │
├───────────────────────────────────────┤
│      Database Layer (MySQL)           │
│   expense_splitter_db                 │
└───────────────────────────────────────┘
```

---

---

# SLIDE 6 — DATABASE DESIGN

---

## Database Design

**7 Tables in MySQL:**

| Table | Purpose |
|-------|---------|
| `users` | User accounts |
| `groups_table` | Expense groups |
| `group_members` | User ↔ Group relationships |
| `categories` | Expense categories (Meals, Travel, etc.) |
| `expenses` | Individual expense records |
| `expense_splits` | Per-user split amounts |
| `settlements` | Recorded payment transactions |

&nbsp;

**Default Categories:** Infrastructure · Meals · Software · Travel · Hardware · Office · Events · Other

---

---

# SLIDE 7 — FEATURE: AUTHENTICATION

---

## Feature 1 — User Authentication

### Login Screen
- Email + Password login
- Input validation with error messages
- Secure password hash storage (not plain text)

### Register Screen
- Full name, email, password, confirm password
- Duplicate email detection
- Password match validation

### Forgot Password
- User enters registered email
- System generates and sends a reset token
- Account can be recovered without support

---

---

# SLIDE 8 — FEATURE: DASHBOARD

---

## Feature 2 — Dashboard

### Financial Overview at a Glance

```
┌─────────────┐  ┌──────────────┐  ┌───────────────────┐
│ TOTAL        │  │ TOTAL OWED   │  │  OVERALL NET       │
│ YOU OWE      │  │ TO YOU       │  │  BALANCE           │
│  Rs. 0       │  │  Rs. 4800    │  │  + Rs. 4800        │
└─────────────┘  └──────────────┘  └───────────────────┘
```

- **Group Summary Cards** with settlement progress bars
- **Recent Activity Feed** — who added/paid what
- One-click access to all modules from the sidebar

---

---

# SLIDE 9 — FEATURE: GROUPS & EXPENSES

---

## Feature 3 — Groups & Expenses

### Groups
- Create multiple groups for different contexts
- Each group shows member count and balance
- Settlement progress bar (0% → 100%)

### Expenses
- Add expense: description, amount, category, date, payer, group
- **3 Split Strategies:**
  - 🟢 **EQUAL** — Rs. 5500 ÷ 4 = Rs. 1375 each
  - 🔵 **CUSTOM** — set individual amounts manually
  - 🟡 **PERCENTAGE** — assign % shares (must total 100%)
- Expandable split breakdown per expense
- "You Paid" / "You Owe" badges for quick context

---

---

# SLIDE 10 — FEATURE: LEDGER & SETTLEMENTS

---

## Feature 4 — Ledger & Settlements

### Ledger
- Full table of all expenses across all groups
- Filter by group, date, category, split type
- Status badges: **SETTLED · PARTIAL · UNSETTLED**
- Export to **CSV** for external record keeping
- Pagination for large datasets

### Settlements
- Outstanding balances organized by group
- Clear debt direction: "Aslam owes you" vs "You owe Muzaffar"
- **Mark Settled** button records the payment
- Running totals: total you owe, total owed to you, pending count

---

---

# SLIDE 11 — FEATURE: SEARCH

---

## Feature 5 — Search & Filter

A dedicated search panel with:

- 🔍 **Keyword search** — searches expense descriptions
- 🗂️ **Group filter** — narrow to one group
- 🏷️ **Category filter** — Meals, Software, Hardware, etc.
- 👤 **Payer filter** — see what one person has paid
- 📅 **Date range** — From / To date selectors
- **Apply Filters** + **Clear All** controls

Results displayed in a clean table with all expense details.

---

---

# SLIDE 12 — WORKING DEMO FLOW

---

## Working Demo — Step by Step

1. **Launch app** → Login screen appears
2. **Register** a new account → auto-navigates to login
3. **Login** → Dashboard shows zero balances (new account)
4. **Create a Group** → "Flat Mates" with 4 members
5. **Add Expense** → "New WiFi router", Rs. 5500, Equal split
6. **View Expenses** → See Rs. 1375/person breakdown
7. **Check Dashboard** → Balance updates to Rs. 4125 owed to you
8. **Open Settlements** → See who owes what
9. **Mark Settled** → Aslam pays Rs. 1375 → balance drops
10. **Ledger** → Filter by group, export CSV
11. **Search** → Type "wifi" → expense appears instantly

---

---

# SLIDE 13 — CHALLENGES FACED

---

## Challenges Faced

### 1. Balance Calculation Logic
**Challenge:** Computing net balance across multiple groups, with partial settlements, and multiple payers in the same group.
**Solution:** Calculated per-pair balances (payer→payee) from expense_splits, then subtracted settled amounts, resulting in a net balance per person.

### 2. Dynamic Split UI
**Challenge:** The expense dialog needed to dynamically show/hide input fields depending on whether the split type was EQUAL, CUSTOM, or PERCENTAGE.
**Solution:** Used card-panel switching and real-time amount recalculation triggered by listeners.

### 3. Real-Time Dashboard Updates
**Challenge:** The dashboard balance totals needed to stay accurate after any add/edit/delete/settle action.
**Solution:** Each DAO method triggers a refresh of the dashboard panel by re-querying the database.

### 4. FlatLaf Styling Consistency
**Challenge:** Maintaining consistent colors, fonts, and button styles across 12+ frames.
**Solution:** Created `UiStyles.java` as a centralized style utility class used by all UI components.

### 5. Settlement Status Calculation
**Challenge:** Determining SETTLED vs PARTIAL vs UNSETTLED status per expense.
**Solution:** Compared sum of settlement amounts against the required split amounts per user for each expense.

---

---

# SLIDE 14 — DESIGN PATTERNS APPLIED

---

## Design Patterns Applied

### Strategy Pattern
```
SplitStrategy (interface)
    ├── EqualSplit.java      → amount / memberCount
    ├── CustomSplit.java     → user-entered per-person amounts
    └── PercentageSplit.java → amount × (percent / 100)
```

### DAO Pattern
```
UserDAO     → findByEmail(), save(), updatePassword()
GroupDAO    → createGroup(), getGroupsForUser(), addMember()
ExpenseDAO  → addExpense(), getExpensesByGroup(), updateExpense()
SettlementDAO → addSettlement(), getSettlements(), getBalance()
```

### Singleton Pattern
```java
DatabaseConnection.getConnection()
// Returns the same shared Connection instance
```

---

---

# SLIDE 15 — WHAT I LEARNED

---

## What I Learned

- **Java Swing** — Building complex multi-screen desktop applications with custom layouts and dynamic components

- **JDBC & MySQL** — Designing relational schemas, writing parameterized queries, managing transactions

- **Design Patterns** — Practical application of Strategy, DAO, and Singleton in a real project context

- **UI/UX Design** — Creating consistent, user-friendly interfaces; color coding, badges, progress indicators all improve usability

- **Debugging** — Tracing issues across the DAO ↔ UI boundary; using database state to verify business logic

- **Project Structure** — Organizing a growing codebase into logical packages to keep it maintainable

---

---

# SLIDE 16 — FUTURE ENHANCEMENTS

---

## Future Enhancements

| # | Feature | Impact |
|---|---------|--------|
| 📧 | **Email Notifications** | Remind users of unpaid balances |
| 📱 | **Mobile App** | Access from Android/iOS via REST API |
| 🔁 | **Recurring Expenses** | Auto-add monthly bills |
| 💱 | **Multi-Currency** | Support international groups |
| 📈 | **Analytics Charts** | Visual spending breakdown |
| ☁️ | **Cloud Sync** | Real-time multi-device sync |
| 📄 | **PDF Export** | Printable settlement reports |
| 🌙 | **Dark Mode** | Built-in with FlatLaf |

---

---

# SLIDE 17 — CONCLUSION

---

## Conclusion

The **Expense Splitter Application** is a complete, working solution to a real-world problem.

### What was built:
- ✅ Full authentication system (register, login, forgot password)
- ✅ Group-based expense management
- ✅ Three split strategies (Equal, Custom, Percentage)
- ✅ Automated balance calculation
- ✅ Settlement tracking and resolution
- ✅ Advanced ledger with filters and CSV export
- ✅ Search panel with multi-dimensional filtering
- ✅ Modern UI with FlatLaf and FontAwesome icons
- ✅ Clean MVC + Strategy + DAO architecture

The project demonstrates practical skills in Java development, database design, and software engineering principles — ready for real-world use.

---

---

# SLIDE 18 — THANK YOU

---

&nbsp;

&nbsp;

# Thank You

&nbsp;

**Expense Splitter Application**

*Arham Bhatti*

&nbsp;

> *Questions are welcome.*

&nbsp;

---

*GitHub Repository:* `github.com/[your-username]/expense-splitter`

---
