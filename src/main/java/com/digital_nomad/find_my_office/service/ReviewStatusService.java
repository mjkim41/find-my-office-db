package com.digital_nomad.find_my_office.service;

import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.domain.cafe.entity.ReviewCrawlingStatus;
import com.digital_nomad.find_my_office.repository.CafeRepository;
import com.digital_nomad.find_my_office.repository.ReviewStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReviewStatusService {

    private final ReviewStatusRepository reviewStatusRepository;
    private final CafeRepository cafeRepository;

    // # 업체별 리뷰 크롤링 여부를 저장
    public ReviewCrawlingStatus saveReviewCrawlingStatus(Cafe cafe, boolean cafeExists) {

        // 카페 중복으로 인식되지 않게, 기존 카페를 db에서 가져와서 심어줌
        Cafe foundCafe = cafeRepository.findById(cafe.getId()).orElseThrow();

        ReviewCrawlingStatus status = ReviewCrawlingStatus.builder()
                .isReviewed(true)
                .cafeExists(cafeExists)
                .cafe(foundCafe)
                .build();

        ReviewCrawlingStatus savedReviewCrawlingStatus = reviewStatusRepository.save(status);
        return savedReviewCrawlingStatus;

    }


}
