package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.domain.cafe.entity.QAddress;
import com.digital_nomad.find_my_office.domain.cafe.entity.QCafe;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.digital_nomad.find_my_office.domain.cafe.entity.QAddress.address;
import static com.digital_nomad.find_my_office.domain.cafe.entity.QCafe.cafe;

@RequiredArgsConstructor
@Repository
public class CafeRepositoryImpl implements CafeRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory jpaQueryFactory;

    // 이미 있는 카페일 때, 주소가 바뀌었으면 주소만 UPDATE
    @Override
    public void updateExistingCafe(Long newAddressId, String storeId) {

        String updateQuery = """
            UPDATE tbl_cafe C
            JOIN tbl_address A
            ON C.address_id = A.address_id
            SET C.address_id = ?
            WHERE C.store_id = ?
            AND C.address_id != ?
            """;

        int update = jdbcTemplate.update(updateQuery, newAddressId, storeId, newAddressId);


    }

    @Override
    @Transactional(readOnly = true)
    public List<Cafe> findByAddressProvinceName(String provinceName) {

        List<Cafe> fetch = jpaQueryFactory
                .selectFrom(cafe)
                .innerJoin(cafe.address, address)
                .fetchJoin()
                .where(address.provinceName.eq(provinceName))
                .orderBy(cafe.id.asc())
                .fetch();

        return fetch;

    }


}
