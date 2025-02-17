package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    // 위도와 경도 조합이 존재하는지 확인(중복 저장 방지)
    Optional<Address> findByLatitudeAndLongitude(Double latitude, Double longitude);

}
