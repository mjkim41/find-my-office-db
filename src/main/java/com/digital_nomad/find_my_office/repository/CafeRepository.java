package com.digital_nomad.find_my_office.repository;

import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, String> {
}
