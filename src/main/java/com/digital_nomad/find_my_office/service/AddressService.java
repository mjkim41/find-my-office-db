package com.digital_nomad.find_my_office.service;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.digital_nomad.find_my_office.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    //  DB에 없는 지번+동+호 조합이면 주소 table에 추가후 주소 반환, 있는 주소면 기존 정보 조회 후 기존 주소 반환
    public Address getOrSaveAddress(Address address) {
        Optional<Address> existingAddress = addressRepository
                .findByOldAddressAndDongAndHo(address.getOldAddress(), address.getDong(), address.getHo());
        return existingAddress.orElseGet(() -> addressRepository.save(address));
    }
}
