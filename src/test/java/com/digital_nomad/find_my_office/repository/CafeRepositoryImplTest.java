package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.domain.cafe.entity.QAddress;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.digital_nomad.find_my_office.domain.cafe.entity.QAddress.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CafeRepositoryImplTest {

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Test
    @DisplayName("Cafe 엔티티에 주소를 갱신하면, Address Entity의 Cafe 리스트로 갱신된다")
    void displayCafeList() {

        Long id = 1L;

        List<Tuple> fetch = jpaQueryFactory
                .select(address.id, address.oldAddress, address.cafes)
                .from(address)
                .where(address.id.eq(id))
                .fetch();

        fetch.forEach(System.out::println);
    }

    @Test
    @DisplayName("cafe entity를 조회하면, address 까지 함께 조회된다.")
    void cafeAddress() {

        String provinceName = "서울특별시";

        List<Cafe> byAddressProvinceName = cafeRepository.findByAddressProvinceName(provinceName);

        byAddressProvinceName.forEach(System.out::println);
    }

}