package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reviewImages WHERE r.id = :id")
    public Review findByIdWithReviewImages(@Param("id") Long id);
}
