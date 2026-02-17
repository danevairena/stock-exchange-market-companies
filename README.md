# 📈 Stock Exchange Market Companies API

Spring Boot REST API for managing companies and retrieving stock market data using the Finnhub API.

This project demonstrates backend development skills including REST APIs, database integration, external API integration, and secure configuration management.

---

# 🚀 Features

* Create and manage companies
* Retrieve stock price data from Finnhub API
* Store daily stock snapshots
* RESTful API design
* PostgreSQL database integration
* Secure secret management using `.env`

---

# 🛠 Tech Stack

* Java 17
* Spring Boot 3
* Spring Data JPA
* PostgreSQL
* Finnhub API
* Gradle
* dotenv-java

---

# ⚙️ Setup Instructions

## 1. Clone repository

```
git clone https://github.com/yourusername/stock-exchange-market-companies
cd stock-exchange-market-companies
```

---

## 2. Create .env file

Copy:

```
.env.example
```

Create:

```
.env
```

Fill with your values:

```
DB_URL=jdbc:postgresql://localhost:5432/stockdb
DB_USERNAME=your_username
DB_PASSWORD=your_password
FINNHUB_API_KEY=your_api_key
FINNHUB_BASE_URL=https://finnhub.io/api/v1
```

---

## 3. Run application

```
./gradlew bootRun
```

Application runs on:

```
http://localhost:8080
```

---

# 📚 API Documentation

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

# 🔐 Security

Sensitive data is stored in `.env` and excluded from version control.

---

# 🧪 Example Endpoints

## Create Company

POST `/api/companies`

## Get All Companies

GET `/api/companies`

## Get Company Stock Data

GET `/api/stocks/{symbol}`

---

# 👩‍💻 Author

Irena Daneva

---

# 📌 Project Purpose

This project was built for learning and portfolio purposes to demonstrate backend development skills with Spring Boot.
