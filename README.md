# 🌍 Trip Itinerary Planner

A Full Stack Intelligent Tourist Itinerary Planner built using **Spring Boot**, **Flutter**, and **MySQL**.

Unlike a traditional CRUD-based travel application, this project automatically generates an optimized travel itinerary based on a user's budget, number of days, travel preferences, ratings, opening hours, and distance between tourist attractions.

---

# 🚀 Features

### User Features

* User Registration & Login
* JWT Authentication
* Create & Save Trips
* Share Itinerary
* Regenerate Itinerary
* View Previous Trips

---

## Smart Recommendation Engine

* Budget Optimization
* Hotel Recommendation
* Restaurant Recommendation
* Tourist Place Recommendation
* Multi-Day Trip Planning
* Cost Optimization
* Time Slot Planning
* Distance Optimization
* Rating Based Selection
* Popularity Based Recommendation

---

## Budget Optimizer

Automatically divides total budget into:

* Hotel
* Food
* Travel
* Entry Tickets

Example:

Budget = ₹10,000

Hotel = ₹4,000

Food = ₹2,500

Travel = ₹2,000

Tickets = ₹1,500

---

## Route Optimization

The planner minimizes travelling distance by clustering nearby places together.

Instead of random ordering,

Example:

❌ India Gate → Qutub Minar → Red Fort

it generates

✅ Red Fort → Jama Masjid → Raj Ghat → India Gate

reducing travel time and transportation cost.

---

## Time Slot Planning

The itinerary also considers:

* Opening Time
* Closing Time
* Estimated Visit Duration
* Daily Available Time

---

## Tech Stack

### Frontend

* Flutter
* Provider / Riverpod
* Google Maps
* Dio

---

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

---

### Database

* MySQL

---

### Authentication

* JWT

---

## Backend Architecture

Controller

↓

DTO

↓

Mapper

↓

Service

↓

Recommendation Engine

↓

Repository

↓

MySQL

---

## Recommendation Engine

The itinerary generator consists of multiple optimization modules.

* Budget Allocation Engine
* Hotel Selection Engine
* Restaurant Selection Engine
* Tourist Place Selection Engine
* Route Optimization Engine
* Time Slot Planner
* Cost Calculator

---

## Database Tables

* Users
* Locations
* Tourist Places
* Restaurants
* Hotels
* Amenities
* Itineraries
* Reviews
* Images

---

## Folder Structure

```
trip-itinerary

docs/

src/

controller/

service/

repository/

entity/

dto/

mapper/

planner/

config/

exception/

README.md

LICENSE
```

---

## Project Documentation

Documentation is available inside the **docs** folder.

* Architecture.md
* Database.md
* API.md
* BusinessLogic.md
* Roadmap.md
* CodingStandards.md
* RecommendationEngine.md
* Deployment.md

---

## Future Improvements

* AI Generated Trips
* Weather Based Planning
* Google Maps Distance Matrix
* Public Transport Recommendation
* Collaborative Trips
* Hotel Booking Integration
* Restaurant Booking Integration
* Offline Mode

---

## Author

Developed by **Md Hessamuddin**

Backend

Spring Boot

Frontend

Flutter

Database

MySQL

---

## License

This project is licensed under the MIT License.
