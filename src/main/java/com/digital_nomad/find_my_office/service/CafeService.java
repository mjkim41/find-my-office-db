package com.digital_nomad.find_my_office.service;

import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.repository.AddressRepository;
import com.digital_nomad.find_my_office.repository.CafeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class CafeService {

    @Autowired
    private CafeRepository cafeRepository;

    @Autowired
    private AddressService addressService;

    public CafeService(CafeRepository cafeRepository) {
        this.cafeRepository = cafeRepository;
    }

    // # 카페 정보를 전달해주면, DB에 저장하는 로직
    public void doRegularDbUpdate(Cafe cafe, Address address) {

        // (1) 주소 저장 : 없는 주소였을 시 새로 저장된 주소, 기존 주소인 경우 기존 주소를 반환
        Address savedAddress = doRegularAddressUpdate(address);
        // (2) cafe에 주소 끼어넣기(기존 주소면 기존 주소가 끼어넣어짐)
        cafe.setAddress(savedAddress);
        // (3) 카페 table 업데이트
        doRegularCafeUpdate(cafe);
    }

    private Address doRegularAddressUpdate(Address address) {
        // 기존에 있는 주소 인지 확인하여,
        //   DB에 없는 도로명 주소면 주소 table에 추가 후 반환, 있는 주소면 기존 정보 조회 후 반환
        return addressService.getOrSaveAddress(address);
    }

    // 카페 table 업데이트
    private void doRegularCafeUpdate(Cafe cafe) {

        // 이미 있는 카페인지 확인
        Optional<Cafe> foundCafe = cafeRepository.findById(cafe.getId());

        // 기존 카페인지 여부에 따라 로직 다르게 처리
        if (foundCafe.isPresent()) {
            // 기존 카페인 경우 : 주소 바뀌었으면 주소만 업데이트
            cafeRepository
                    .updateExistingCafe(cafe.getAddress().getId(), cafe.getId());
        } else {
            // 없던 카페인 경우 : 새로 저장하되, 주소는 db에 있었으면 새로
            cafeRepository.save(cafe);
        }

    }

    // 특정 시도(광역시 :시, 나머지: 도)에 속한 카페 리스트 반환
    public List<Cafe> getCafeByProvinceName(String provinceName) {
        return cafeRepository.findByAddress_ProvinceName(provinceName);
    }
}
