package com.ashuu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ashuu.model.MyProfile;

@Repository
public interface ProfileRepository extends JpaRepository<MyProfile, Long> {

	Optional<MyProfile> findByAdminKey(String string);

}
