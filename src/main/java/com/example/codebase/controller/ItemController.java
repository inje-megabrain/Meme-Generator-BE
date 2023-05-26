package com.example.codebase.controller;

import com.example.codebase.controller.dto.RestResponse;
import com.example.codebase.domain.item.dto.ItemCreateDTO;
import com.example.codebase.domain.item.dto.ItemPageDTO;
import com.example.codebase.domain.item.dto.ItemResponseDTO;
import com.example.codebase.domain.item.service.ItemService;
import com.example.codebase.domain.meme.dto.MemeResponseDTO;
import com.example.codebase.util.FileUtil;
import com.example.codebase.util.SecurityUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.PositiveOrZero;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Api(value = "Item APIs", description = "짤 아이템 관련 API")
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }


    @ApiOperation(value = "짤 아이템 생성", notes = "[관리자만] 짤 아이템 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "짤 아이템 생성", response = ItemResponseDTO.class),
            @ApiResponse(code = 400, message = "이미지 파일만 업로드 가능합니다.", response = RestResponse.class),
            @ApiResponse(code = 401, message = "로그인이 필요합니다.", response = RestResponse.class),
            @ApiResponse(code = 403, message = "권한이 없습니다.", response = RestResponse.class),
            @ApiResponse(code = 500, message = "서버 오류", response = RestResponse.class)
    })
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ADMIN')")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity createItem(
            @RequestPart("dto") ItemCreateDTO dto,
            @RequestPart("image") MultipartFile image) {

        String loginUsername = SecurityUtil.getCurrentUsername()
                .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        dto.setUsername(loginUsername);

        // dto 확장자 추출
        String originalFilename = image.getOriginalFilename();
        int index = originalFilename.lastIndexOf(".");
        String ext = originalFilename.substring(index + 1).toLowerCase();

        // image 확장자 -> jpg, png, jpeg
        if (!FileUtil.checkImageExtension(ext)) {
            return new ResponseEntity("이미지 파일만 업로드 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        // 이미지 저장
        String savePath = "./images/";
        String storeFileName = UUID.randomUUID() + "." + ext;
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        try {
            String key = savePath + now + "/" + storeFileName; // /images/시간/파일명
            File temp = new File(savePath + now + "/");

            if (!temp.exists()) {
                temp.mkdirs();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(key);
            fileOutputStream.write(image.getBytes());
            fileOutputStream.close();

            dto.setImageUrl("/images/" + now + "/" + storeFileName);
        } catch (IOException e) {
            return new ResponseEntity("이미지 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 아이템 등록
        ItemResponseDTO item = itemService.createItem(dto);
        return new ResponseEntity(item, HttpStatus.CREATED);
    }

    @ApiOperation(value = "짤 아이템 카테고리 별 전체 조회", notes = "해당 카테고리를 가지는 짤 아이템 전체를 조회합니다. [페이지네이션]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공", response = ItemPageDTO.class)
    })
    @GetMapping
    public ResponseEntity getItems(
            @RequestParam(value = "category", defaultValue = "상의") String category,
            @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
            @PositiveOrZero @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "desc, asc", defaultValue = "desc") @RequestParam(value = "sort_direction", defaultValue = "desc") String sortDirection
    ) {
        ItemPageDTO dtos = itemService.getItems(category, page, size, sortDirection);
        return new ResponseEntity(dtos, HttpStatus.OK);
    }

    @ApiOperation(value = "짤 아이템 단일 조회", notes = "해당 아이디의 짤 아이템을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "조회 성공", response = ItemResponseDTO.class)
    })
    @GetMapping("/{id}")
    public ResponseEntity getItem(@PathVariable("id") Long id) {
        ItemResponseDTO dto = itemService.getItem(id);
        return new ResponseEntity(dto, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_ADMIN')")
    @ApiOperation(value = "짤 아이템 삭제", notes = "[관리자만] 해당 아이디의 짤 아이템을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "삭제 성공"),
            @ApiResponse(code = 401, message = "로그인이 필요합니다."),
            @ApiResponse(code = 403, message = "권한이 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity deleteItem(@PathVariable("id") Long id) {
        itemService.deleteItem(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
