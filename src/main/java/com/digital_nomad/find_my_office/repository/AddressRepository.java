package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    // 존재하는지 주소 Entity 존재하는지 확인(중복 저장 방지)
    // 지번주소 + 동 + 호로 확인
    Optional<Address> findByOldAddressAndDongAndHo(String oldAddress, String dong, String ho);

}
