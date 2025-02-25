package com.digital_nomad.find_my_office.domain.cafe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"review"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tbl_review_image")
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_image_id")
    private Long id;

    @Column(nullable=false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="review_id")
    @Setter
    private Review review;

    // review 변경 편의 메소드
    public void changeReview(Review review) {
        this.review = review;
        review.getReviewImages().remove(this);
    }

}
