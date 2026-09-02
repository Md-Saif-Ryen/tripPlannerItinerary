package com.example.tripItinerary.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.DeletedUser;

public interface DeletedUserRepository
        extends JpaRepository<DeletedUser, Long> {
}