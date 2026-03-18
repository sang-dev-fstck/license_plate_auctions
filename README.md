# 🚘 License Plate Auction System - Backend API

A robust RESTful API built with Spring Boot to power an online license plate auction platform. This project is designed
with a Clean Code mindset, focusing on performance, clean data processing, and a flexible architecture.

## 🛠 Tech Stack

* **Language:** Java 21
* **Core Framework:** Spring Boot 3.3.4
* **Database:** MongoDB & Spring Data MongoDB
* **Data Mapper:** MapStruct 1.5.5
* **Utilities:** Lombok, Jackson (Custom Deserializer)
* **Build Tool:** Maven

## ✨ Key Features

* **License Plate & Rule Management (CRUD):** Securely handle the flow of creating, reading, updating, and deleting
  license plates and tag rules.
* **Dynamic Search:** Flexibly search and filter license plates based on multiple criteria (province/city, price range,
  status, etc.) using MongoTemplate Criteria.
* **Data Normalization:** Automatically clean incoming data (e.g., auto-trimming trailing/leading whitespace) at the
  filter layer using Jackson's `@JsonComponent`, ensuring database integrity.
* **DTO Pattern Architecture:** Clear separation of concerns between the Database (Entity) and the Client (
  Request/Response) via MapStruct.

## 🚀 Getting Started

### Prerequisites

* [JDK 21](https://jdk.java.net/21/) or higher.
* [MongoDB](https://www.mongodb.com/try/download/community) running on the default port `27017`.

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/sang-dev-fstck/license_plate_auction.git](https://github.com/sang-dev-fstck/license_plate_auction.git)
   cd license_plate_auction
   ```

2. **Configure the Database:**
    * Open `src/main/resources/application.properties`.
    * Ensure the MongoDB connection URI points to your database:
        ```properties
        server.port=8080
        spring.data.mongodb.uri=mongodb://localhost:27017/auction_db
        ```

3. **Run the application:**
   The project uses the Maven Wrapper, so you don't need to install Maven globally. Run the following command in your
   terminal:
   ```bash
   ./mvnw spring-boot:run
   ```
   *The server will start and listen on: `http://localhost:8080`*

## 📂 Project Structure

The project follows a standard Spring Boot Layered Architecture:

* `controller/`: Handles incoming HTTP Requests, calls Services, and returns JSON Responses.
* `service/`: Contains all the Business Logic.
* `repository/`: Directly interacts with MongoDB.
* `entity/`: Defines the structure of the database collections.
* `dto/` & `mapper/`: Data Transfer Objects and MapStruct interfaces.
* `config/`: System configurations (e.g., Jackson custom rules, CORS).

---
*Developed by [Sang](https://github.com/sang-dev-fstck).*