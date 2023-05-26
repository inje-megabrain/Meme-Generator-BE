package com.example.codebase.domain.item.service;

import com.example.codebase.controller.dto.PageInfo;
import com.example.codebase.domain.item.dto.ItemCreateDTO;
import com.example.codebase.domain.item.dto.ItemPageDTO;
import com.example.codebase.domain.item.dto.ItemResponseDTO;
import com.example.codebase.domain.item.entity.Item;
import com.example.codebase.domain.item.entity.ItemCategory;
import com.example.codebase.domain.item.repository.ItemRepository;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.meme.dto.MemeResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    private final MemberRepository memberRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, MemberRepository memberRepository) {
        this.itemRepository = itemRepository;
        this.memberRepository = memberRepository;
    }

    public ItemResponseDTO createItem(ItemCreateDTO dto) {
        Member member = memberRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        Item save = itemRepository.save(dto.toEntity(member));
        return ItemResponseDTO.from(save);
    }


    public ItemPageDTO getItems(String category, int page, int size, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Item> itemPage = itemRepository.findAllByCategory(ItemCategory.from(category), pageRequest);
        PageInfo pageInfo = PageInfo.of(page, size, itemPage.getTotalPages(), itemPage.getTotalElements());

        List<ItemResponseDTO> all = itemPage.stream()
                .map(ItemResponseDTO::from)
                .collect(Collectors.toList());

        return ItemPageDTO.of(all, pageInfo);
    }

    public ItemResponseDTO getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 없습니다."));
        return ItemResponseDTO.from(item);
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 없습니다."));
        itemRepository.delete(item);
    }
}
