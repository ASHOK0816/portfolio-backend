# 🚀 Portfolio Backend (Spring Boot)

This is the backend service for my personal portfolio project, built using **Spring Boot**.
It provides REST APIs for managing user data, projects, contact messages, and other dynamic content.

---

## 📌 Tech Stack

* ☕ Java 17+
* 🌱 Spring Boot
* 🛢️ MySQL / PostgreSQL
* 🔐 Spring Security (JWT Authentication)
* 📦 Maven
* 🌐 REST API

---

## 📂 Project Structure

```
src/
 ├── controller/     # REST Controllers
 ├── service/        # Business Logic
 ├── repository/     # Database Layer
 ├── model/          # Entity Classes
 └── config/         # Security & Config
```

---

## ⚙️ Features

* 🔐 User Authentication & Authorization (JWT)
* 📄 Manage Portfolio Projects (CRUD)
* 📬 Contact Form API
* 👤 Profile Management
* 🗄️ Database Integration
* 🌍 RESTful API Design

---

## 🔑 Environment Variables

Create a `.env` file (DO NOT commit this file):

```
DB_URL=your_database_url
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key
EMAIL_USERNAME=your_email
EMAIL_PASSWORD=your_email_password
```

---

## ▶️ Run Locally

### 1. Clone the repository

```
git clone https://github.com/your-username/portfolio-backend.git
cd portfolio-backend
```

### 2. Configure Database

Update `application.properties` or use environment variables.

### 3. Run the application

```
mvn spring-boot:run
```

---

## 🔗 API Endpoints (Sample)

| Method | Endpoint           | Description          |
| ------ | ------------------ | -------------------- |
| POST   | /api/auth/login    | User login           |
| POST   | /api/auth/register | User registration    |
| GET    | /api/projects      | Get all projects     |
| POST   | /api/projects      | Add new project      |
| POST   | /api/contact       | Send contact message |

---

## 🛡️ Security Note

* Sensitive data is stored using environment variables
* `.env` file is excluded via `.gitignore`
* Secrets are never committed to version control

---

## 📌 Future Improvements

* 🚀 Deploy on cloud (AWS / Render)
* 📊 Add analytics dashboard
* 🔔 Notification system
* 🧪 Unit & Integration Testing

---

## 👨‍💻 Author

**Ashok Pawar**

* GitHub: https://github.com/ASHOK0816

---

## ⭐ Show Your Support

If you like this project, give it a ⭐ on GitHub!
