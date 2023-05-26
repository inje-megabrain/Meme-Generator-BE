package com.example.codebase.domain.item.repository;

import com.example.codebase.domain.item.entity.Item;
import com.example.codebase.domain.item.entity.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAllByCategory(ItemCategory category, Pageable pageable);
}
