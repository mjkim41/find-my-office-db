package com.digital_nomad.find_my_office.service;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.digital_nomad.find_my_office.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public Address regularAddressDbUpdate(Address address) {
        // 위도, 경도 중복 확인
        Optional<Address> existingAddress = addressRepository.findByLatitudeAndLongitude(address.getLatitude(), address.getLongitude());

        // 중복 없으면 저장
        return existingAddress.orElseGet(() -> addressRepository.save(address));
    }
}
