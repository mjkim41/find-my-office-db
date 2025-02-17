package com.digital_nomad.find_my_office.domain.cafe.entity;

import com.digital_nomad.find_my_office.repository.CafeRepository;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Getter
@ToString(exclude = {"Cafe"})
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name="tbl_address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="address_id")
    Long id;

    @Column(nullable = false)
    private Double latitude; // 경도

    @Column(nullable = false)
    private Double longitude; // 위도

    // 카페 entity와 1:1 연결
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Cafe cafe;

    // # 구주소 관련
    // 필수
    private String oldZipCode; // 구우편번호
    private String oldAddress; // 지번주소(full 주소)
    // sub
    private String adminDistrictCode; // 행정동코드
    private String administrativeDistrictName; // 행정동명
    private String legalDistrictCode; // 법정동코드
    private String legalDistrictName; // 법정동명
    private String lotNumber; // 건물본번지(부번지) : 울산광역시 북구 매곡동 930-5에서 '930'
    private String subLotNumber; // 건물부번지(번지) : 울산광역시 북구 매곡동 930-5에서 '5'

    // # 신주소 관련
    // 도로명 관련
    private String newAddressCode; // 도로명코드
    private String newAddress; // 도로명주소(full)
    // 우편번호
    private String newZipCode; // 신우편번호

    // # 공통
    // 시, 도, 구
    private String ProvinceCode; // 시도코드(도는 도, 광역시는 시)
    private String ProvinceName; // 시도명
    private String CityCode; // 시군구코드(도는 시, 광역시는 구)
    private String CityName; // 시군구명
    // 동, 호수, 층
    private String dong; // 동정보
    private String floor; // 층정보
    private String ho; // 호정보

    @CreationTimestamp
    LocalDateTime createdAt;



}
