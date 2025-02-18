package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import org.springframework.stereotype.Repository;

@Repository
public interface CafeRepositoryCustom {

    public void updateExistingCafe(Long addressId, String cafeId);

}
