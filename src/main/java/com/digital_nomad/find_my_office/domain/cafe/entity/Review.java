package com.digital_nomad.find_my_office.domain.cafe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"reviewCrawlingStatus", "reviewImages"})
@Table(name="tbl_review_detail")
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Long id;

    @Column(nullable = false)
    private String reviewer;

    @OneToMany(mappedBy = "review", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    @Setter
    private List<ReviewImage> reviewImages = new ArrayList<>();

    @Column(nullable = true, name="review_content")
    private String content;

    @Column(nullable = false, name="review_date")
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="review_crawling_id")
    @Setter
    private ReviewCrawlingStatus reviewCrawlingStatus;

    // reviewImage 수정 편의메소드
    public void addReviewImage(ReviewImage reviewImage) {
        this.reviewImages.add(reviewImage);
        reviewImage.setReview(this);
    }

    public void deleteReviewImage(ReviewImage reviewImage) {
        this.reviewImages.remove(reviewImage);
        reviewImage.setReview(null);
    }

    // reviewCrwalingStatus 수정 편의 메소드
    public void changeReviewCrawlingStatus(ReviewCrawlingStatus reviewCrawlingStatus) {
        this.reviewCrawlingStatus = reviewCrawlingStatus;
        reviewCrawlingStatus.getReviews().remove(this);
    }



}
