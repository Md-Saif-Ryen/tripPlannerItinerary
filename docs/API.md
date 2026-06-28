# REST API Documentation

## Authentication

POST /api/auth/register

POST /api/auth/login

POST /api/auth/logout

---

## User

GET /api/users

GET /api/users/{id}

PUT /api/users/{id}

DELETE /api/users/{id}

---

## Location

GET /api/locations

GET /api/locations/{id}

---

## Tourist Places

GET /api/tourist-places

GET /api/tourist-places/{id}

GET /api/tourist-places/location/{locationId}

---

## Restaurants

GET /api/restaurants

GET /api/restaurants/{id}

GET /api/restaurants/location/{locationId}

---

## Hotels

GET /api/hotels

GET /api/hotels/{id}

GET /api/hotels/location/{locationId}

---

## Itinerary

POST /api/itinerary/generate

PUT /api/itinerary/regenerate

GET /api/itinerary/{id}

GET /api/itinerary/user/{userId}

DELETE /api/itinerary/{id}

POST /api/itinerary/share

---

## Reviews

POST /reviews

PUT /reviews

DELETE /reviews

---

## Admin

POST /admin/location

POST /admin/place

POST /admin/hotel

POST /admin/restaurant