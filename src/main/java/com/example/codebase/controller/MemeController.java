package com.example.codebase.controller;

import com.example.codebase.domain.meme.dto.MemeCreateDTO;
import com.example.codebase.domain.meme.dto.MemePageDTO;
import com.example.codebase.domain.meme.dto.MemeResponseDTO;
import com.example.codebase.domain.meme.dto.MemeUpdateDTO;
import com.example.codebase.domain.meme.service.MemeService;
import com.example.codebase.util.FileUtil;
import com.example.codebase.util.SecurityUtil;
import io.swagger.annotations.ApiParam;
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

@RestController
@RequestMapping("/api/meme")
public class MemeController {

    // 수배 CRUD

    private final MemeService memeService;


    @Autowired
    public MemeController(MemeService memeService) {
        this.memeService = memeService;
    }

    // 수배 등록
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity createMeme(
            @RequestPart("dto") MemeCreateDTO dto,
            @RequestPart("image") MultipartFile image) {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
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

        // 수배 등록
        MemeResponseDTO meme = memeService.createMeme(dto);
        return new ResponseEntity(meme, HttpStatus.CREATED);
    }

    // 수배 전체 조회
    @GetMapping
    public ResponseEntity getMemeList(
            @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
            @PositiveOrZero @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "desc, asc", defaultValue = "desc") @RequestParam(value = "sort_direction", defaultValue = "desc") String sortDirection
    ) {
        MemePageDTO memeList = memeService.getMemeList(page, size, sortDirection);
        return new ResponseEntity(memeList, HttpStatus.OK);
    }

    @GetMapping("/{memeId}")
    public ResponseEntity getMeme(
            @PathVariable("memeId") Long memeId
    ) {
        MemeResponseDTO meme = memeService.getMeme(memeId);
        return new ResponseEntity(meme, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PutMapping("/{memeId}")
    public ResponseEntity updateMeme(
            @PathVariable("memeId") Long memeId,
            @RequestBody MemeUpdateDTO dto
    ) {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        try {
            if (SecurityUtil.isAdmin()) {
                MemeResponseDTO meme = memeService.updateMeme(memeId, dto);
                return new ResponseEntity(meme, HttpStatus.OK);
            }
            MemeResponseDTO meme = memeService.updateMeme(memeId, dto, loginUsername);
            return new ResponseEntity(meme, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @DeleteMapping("/{memeId}")
    public ResponseEntity deleteMeme(
            @PathVariable("memeId") Long memeId
    ) {
        try {
            String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
            if (SecurityUtil.isAdmin()) {
                memeService.deleteMeme(memeId);
                return new ResponseEntity("삭제되었습니다.", HttpStatus.OK);
            }
            memeService.deleteMeme(memeId, loginUsername);
            return new ResponseEntity("삭제되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
