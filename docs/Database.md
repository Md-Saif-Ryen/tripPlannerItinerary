# Database Design

## Tables

1. users
2. locations
3. itineraries
4. itinerary_days
5. itinerary_places
6. tourist_places
7. tourist_place_images
8. tourist_place_reviews
9. restaurants
10. restaurant_images
11. restaurant_reviews
12. hotels
13. hotel_images
14. hotel_reviews
15. amenities
16. hotel_amenities

---

# Relationships

User

↓

Many Itineraries

Location

↓

Many Tourist Places

↓

Many Restaurants

↓

Many Hotels

Itinerary

↓

Many Days

Day

↓

Many Places

Hotel

↓

Many Amenities

---

# Optimization Fields

Tourist Place

- Weight
- Rating
- Popularity
- Opening Time
- Closing Time
- Entry Fee
- Visit Time

Restaurant

- Cost
- Rating
- Cuisine
- Veg/Non-Veg

Hotel

- Price
- Rating
- Rooms
- Amenities
- Check-in
- Check-out

---

# Indexes

- Rating
- Popularity
- Location
- Price
- Visit Order

---

# Future Database Improvements

- Nearby Place Cache
- AI Recommendation Cache
- Trip History
- Weather Cache
- Google Distance Matrix Cache