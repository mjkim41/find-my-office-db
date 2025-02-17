package com.digital_nomad.find_my_office.service;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.repository.AddressRepository;
import com.digital_nomad.find_my_office.repository.CafeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CafeService {


    @Autowired
    private CafeRepository cafeRepository;

    // 없으면 저장
    public Cafe regularCafeDbUpdate(Cafe cafe) {
        Optional<Cafe> existingCafe = cafeRepository.findById(cafe.getId());
        return existingCafe.orElseGet(() -> cafeRepository.save(cafe));
    }

}
