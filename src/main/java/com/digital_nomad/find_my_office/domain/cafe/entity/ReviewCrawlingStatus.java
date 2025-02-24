package com.digital_nomad.find_my_office.domain.cafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@ToString(exclude = {"cafe", "reviews"})
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "tbl_review_crawling_status")
public class ReviewCrawlingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_crawling_id")
    private Long id;

    private boolean isReviewed;

    private boolean cafeExists; // 네이버 플레이스에서 카페 조회 되는지 여부

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name="cafe_id", referencedColumnName = "store_id")
    private Cafe cafe;

    @OneToMany(mappedBy = "reviewCrawlingStatus", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    // 편의메소드
    public void addReview(Review review) {
        this.reviews.add(review);
        review.setReviewCrawlingStatus(this);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
        review.setReviewCrawlingStatus(null);
    }




}
