package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CafeRepositoryImpl implements CafeRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

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


}
