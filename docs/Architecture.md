# Trip Itinerary Planner - System Architecture

## Project Overview

Trip Itinerary Planner is a Full Stack intelligent travel planning application that automatically generates optimized travel itineraries based on:

- Budget
- Number of Days
- Location
- Travel Type
- Hotel Preference
- Restaurant Preference
- Tourist Place Ratings
- Distance Optimization
- Opening & Closing Time
- Popularity Score

---

# Tech Stack

## Frontend

- Flutter
- Provider / Riverpod
- Google Maps
- Dio
- Shared Preferences

---

## Backend

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- Maven

---

## Database

- MySQL

---

## Authentication

- JWT
- Spring Security

---

## Architecture

Flutter App

↓

REST API

↓

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

# Recommendation Engine

Modules

- Budget Optimizer
- Hotel Selection
- Restaurant Selection
- Tourist Place Selection
- Route Optimization
- Time Slot Planner
- Cost Calculator
- Day Planner

---

# Folder Structure

src

config

controller

dto

entity

enums

exception

mapper

repository

response

service

planner

utils

security

---

# Future Scope

- AI Generated Trips
- Google Maps Route Optimization
- Weather Based Planning
- Public Transport Recommendation
- Collaborative Trip
- Hotel Booking
- Restaurant Booking