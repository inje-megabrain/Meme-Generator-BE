package com.example.codebase.domain.wanted.repository;

import com.example.codebase.domain.wanted.entity.Wanted;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WantedRepository extends JpaRepository<Wanted, Long>{


    Optional<Wanted> findByIdAndMember_Username(Long id, String username);

}
