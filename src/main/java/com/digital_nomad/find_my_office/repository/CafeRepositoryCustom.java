package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CafeRepositoryCustom {

    public void updateExistingCafe(Long addressId, String cafeId);
    public List<Cafe> findByAddressProvinceName(String provinceName);

}
