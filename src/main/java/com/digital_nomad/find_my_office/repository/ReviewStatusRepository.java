package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.domain.cafe.entity.ReviewCrawlingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewStatusRepository extends JpaRepository<ReviewCrawlingStatus, Long> {

    boolean existsByCafeAndIsReviewed(Cafe cafe, boolean isReviewed);

    ReviewCrawlingStatus findByCafeAndIsReviewed(Cafe Cafe, boolean isReviewed);
}
