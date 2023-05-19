package com.example.codebase.domain.meme.repository;

import com.example.codebase.domain.meme.entity.Meme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemeRepository extends JpaRepository<Meme, Long>{


    Optional<Meme> findByIdAndMember_Username(Long id, String username);


    Page<Meme> findAllByMember_Username(String username, Pageable pageable);
}
