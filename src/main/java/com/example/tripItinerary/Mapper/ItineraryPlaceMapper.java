package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.tripItinerary.Entity.ItineraryPlace;
import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.Entity.Restaurant;
import com.example.tripItinerary.DTO.response.ItineraryPlaceResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;
import com.example.tripItinerary.Repo.TouristPlaceRepository;
import com.example.tripItinerary.Repo.HotelRepository;
import com.example.tripItinerary.Repo.RestaurantRepository;
import com.example.tripItinerary.enums.PlaceType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper(config = MapperConfiguration.class, componentModel = "spring")
public abstract class ItineraryPlaceMapper {

    @Autowired
    protected TouristPlaceRepository touristPlaceRepository;

    @Autowired
    protected HotelRepository hotelRepository;

    @Autowired
    protected RestaurantRepository restaurantRepository;

    // ==========================================
    // TO RESPONSE - ✅ FIX
    // ==========================================

    @Mapping(target = "placeName", source = "itineraryPlace", qualifiedByName = "getPlaceName")
    @Mapping(target = "placeAddress", source = "itineraryPlace", qualifiedByName = "getPlaceAddress")
    @Mapping(target = "placeRating", source = "itineraryPlace", qualifiedByName = "getPlaceRating")
    @Mapping(target = "placePrice", source = "itineraryPlace", qualifiedByName = "getPlacePrice")
    @Mapping(target = "contactNumber", source = "itineraryPlace", qualifiedByName = "getContactNumber")
    @Mapping(target = "websiteUrl", source = "itineraryPlace", qualifiedByName = "getWebsiteUrl")
    @Mapping(target = "placeImage", source = "itineraryPlace", qualifiedByName = "getPlaceImage")
    public abstract ItineraryPlaceResponse toResponse(ItineraryPlace itineraryPlace);

    // ==========================================
    // CUSTOM MAPPING METHODS - ✅ FIX
    // ==========================================

    @Named("getPlaceName")
    protected String getPlaceName(ItineraryPlace itineraryPlace) {
        if (itineraryPlace == null) {
            return null;
        }

        PlaceType placeType = itineraryPlace.getPlaceType();
        Long referenceId = itineraryPlace.getReferenceId();

        if (placeType == null || referenceId == null) {
            return null;
        }

        try {
            switch (placeType) {
                case TOURIST_PLACE:
                    TouristPlace touristPlace = touristPlaceRepository.findById(referenceId).orElse(null);
                    return touristPlace != null ? touristPlace.getPlaceName() : null;

                case HOTEL:
                    Hotel hotel = hotelRepository.findById(referenceId).orElse(null);
                    return hotel != null ? hotel.getHotelName() : null;

                case RESTAURANT:
                    Restaurant restaurant = restaurantRepository.findById(referenceId).orElse(null);
                    return restaurant != null ? restaurant.getRestaurantName() : null;

                default:
                    log.warn("Unknown PlaceType: {}", placeType);
                    return null;
            }
        } catch (Exception e) {
            log.error("Error fetching place name for type: {}, id: {}", placeType, referenceId, e);
            return null;
        }
    }

    @Named("getPlaceAddress")
    protected String getPlaceAddress(ItineraryPlace itineraryPlace) {
        if (itineraryPlace == null) {
            return null;
        }

        PlaceType placeType = itineraryPlace.getPlaceType();
        Long referenceId = itineraryPlace.getReferenceId();

        if (placeType == null || referenceId == null) {
            return null;
        }

        try {
            switch (placeType) {
                case TOURIST_PLACE:
                    TouristPlace touristPlace = touristPlaceRepository.findById(referenceId).orElse(null);
                    return touristPlace != null ? touristPlace.getAddress() : null;

                case HOTEL:
                    Hotel hotel = hotelRepository.findById(referenceId).orElse(null);
                    return hotel != null ? hotel.getAddress() : null;

                case RESTAURANT:
                    Restaurant restaurant = restaurantRepository.findById(referenceId).orElse(null);
                    return restaurant != null ? restaurant.getAddress() : null;

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Error fetching place address for type: {}, id: {}", placeType, referenceId, e);
            return null;
        }
    }

    @Named("getPlaceRating")
    protected java.math.BigDecimal getPlaceRating(ItineraryPlace itineraryPlace) {
        if (itineraryPlace == null) {
            return null;
        }

        PlaceType placeType = itineraryPlace.getPlaceType();
        Long referenceId = itineraryPlace.getReferenceId();

        if (placeType == null || referenceId == null) {
            return null;
        }

        try {
            switch (placeType) {
                case TOURIST_PLACE:
                    TouristPlace touristPlace = touristPlaceRepository.findById(referenceId).orElse(null);
                    return touristPlace != null ? touristPlace.getAverageRating() : null;

                case HOTEL:
                    Hotel hotel = hotelRepository.findById(referenceId).orElse(null);
                    return hotel != null ? hotel.getAverageRating() : null;

                case RESTAURANT:
                    Restaurant restaurant = restaurantRepository.findById(referenceId).orElse(null);
                    return restaurant != null ? restaurant.getAverageRating() : null;

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Error fetching place rating for type: {}, id: {}", placeType, referenceId, e);
            return null;
        }
    }

    @Named("getPlacePrice")
    protected java.math.BigDecimal getPlacePrice(ItineraryPlace itineraryPlace) {
        if (itineraryPlace == null) {
            return null;
        }

        PlaceType placeType = itineraryPlace.getPlaceType();
        Long referenceId = itineraryPlace.getReferenceId();

        if (placeType == null || referenceId == null) {
            return null;
        }

        try {
            switch (placeType) {
                case TOURIST_PLACE:
                    TouristPlace touristPlace = touristPlaceRepository.findById(referenceId).orElse(null);
                    return touristPlace != null ? touristPlace.getPrice() : null;

                case HOTEL:
                    Hotel hotel = hotelRepository.findById(referenceId).orElse(null);
                    return hotel != null ? hotel.getPricePerNight() : null;

                case RESTAURANT:
                    Restaurant restaurant = restaurantRepository.findById(referenceId).orElse(null);
                    return restaurant != null ? restaurant.getAverageCostPerPerson() : null;

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Error fetching place price for type: {}, id: {}", placeType, referenceId, e);
            return null;
        }
    }

    @Named("getContactNumber")
    protected String getContactNumber(ItineraryPlace itineraryPlace) {
        if (itineraryPlace == null) {
            return null;
        }

        PlaceType placeType = itineraryPlace.getPlaceType();
        Long referenceId = itineraryPlace.getReferenceId();

        if (placeType == null || referenceId == null) {
            return null;
        }

        try {
            switch (placeType) {
                case TOURIST_PLACE:
                    TouristPlace touristPlace = touristPlaceRepository.findById(referenceId).orElse(null);
                    return touristPlace != null ? touristPlace.getContactNumber() : null;

                case HOTEL:
                    Hotel hotel = hotelRepository.findById(referenceId).orElse(null);
                    return hotel != null ? hotel.getContactNumber() : null;

                case RESTAURANT:
                    Restaurant restaurant = restaurantRepository.findById(referenceId).orElse(null);
                    return restaurant != null ? restaurant.getContactNumber() : null;

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Error fetching contact number for type: {}, id: {}", placeType, referenceId, e);
            return null;
        }
    }

    @Named("getWebsiteUrl")
    protected String getWebsiteUrl(ItineraryPlace itineraryPlace) {
        if (itineraryPlace == null) {
            return null;
        }

        PlaceType placeType = itineraryPlace.getPlaceType();
        Long referenceId = itineraryPlace.getReferenceId();

        if (placeType == null || referenceId == null) {
            return null;
        }

        try {
            switch (placeType) {
                case TOURIST_PLACE:
                    TouristPlace touristPlace = touristPlaceRepository.findById(referenceId).orElse(null);
                    return touristPlace != null ? touristPlace.getWebsiteUrl() : null;

                case HOTEL:
                    Hotel hotel = hotelRepository.findById(referenceId).orElse(null);
                    return hotel != null ? hotel.getWebsiteUrl() : null;

                case RESTAURANT:
                    Restaurant restaurant = restaurantRepository.findById(referenceId).orElse(null);
                    return restaurant != null ? restaurant.getWebsiteUrl() : null;

                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("Error fetching website URL for type: {}, id: {}", placeType, referenceId, e);
            return null;
        }
    }

    @Named("getPlaceImage")
    protected String getPlaceImage(ItineraryPlace itineraryPlace) {
        // Agar aapke entity mein image field hai toh implement karo
        // Otherwise null return karo
        return null;
    }
}