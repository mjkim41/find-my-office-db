package com.digital_nomad.find_my_office.domain.cafe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = {"address"})
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name="tbl_cafe")
public class Cafe {

    @Id
    @Column(name="store_id")
    private String id; // 상가업소번호

    @Column(name="store_name")
    private String name; // 상호명

    String branchName; //지점명

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address; // 주소 정보 저장 객체

    @CreationTimestamp
    LocalDateTime createdAt;


}
