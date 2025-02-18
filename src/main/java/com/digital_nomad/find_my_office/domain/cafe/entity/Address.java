package com.digital_nomad.find_my_office.domain.cafe.entity;

import com.digital_nomad.find_my_office.repository.CafeRepository;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString(exclude = {"cafes"})
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name="tbl_address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="address_id")
    Long id;

    @Column(nullable = false)
    private Double latitude; // 경도

    @Column(nullable = false)
    private Double longitude; // 위도

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Cafe> cafes = new ArrayList<>();

    // # 구주소 관련
    // 필수
    private String oldZipCode; // 구우편번호
    private String oldAddress; // 지번주소(full 주소)
    // sub
    private String adminDistrictCode; // 행정동코드
    private String administrativeDistrictName; // 행정동명
    private String legalDistrictCode; // 법정동코드
    private String legalDistrictName; // 법정동명

    // # 신주소 관련
    // 필수
    private String newZipCode; // 신우편번호
    private String newAddressCode; // 도로명코드
    private String newAddress; // 도로명주소(도로명주소 + 건물본번지 + 건물부번지 = full 도로명주소)
    private String lotNumber; // 건물본번지(부번지)
    private String subLotNumber; // 건물부번지(번지)
    // 우편번호

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

    public void addCafe(Cafe cafe) {
        this.cafes.add(cafe);
        cafe.setAddress(this);
    }

    public void removeCafe(Cafe cafe) {
        this.cafes.remove(cafe);
        cafe.setAddress(null);
    }



}
