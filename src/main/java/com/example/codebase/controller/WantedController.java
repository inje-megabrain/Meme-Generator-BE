package com.example.codebase.controller;

import com.example.codebase.domain.wanted.dto.WantedCreateDTO;
import com.example.codebase.domain.wanted.dto.WantedPageDTO;
import com.example.codebase.domain.wanted.dto.WantedResponseDTO;
import com.example.codebase.domain.wanted.service.WantedService;
import com.example.codebase.util.FileUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/wanted")
public class WantedController {

    // 수배 CRUD

    private final WantedService wantedService;


    @Autowired
    public WantedController(WantedService wantedService) {
        this.wantedService = wantedService;
    }

    // 수배 등록
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity createWanted(
            @RequestPart("dto") WantedCreateDTO dto,
            @RequestPart("image") MultipartFile image)  {

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
        } catch (IOException e){
            return new ResponseEntity("이미지 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 수배 등록
        WantedResponseDTO wanted = wantedService.createWanted(dto);
        return new ResponseEntity(wanted, HttpStatus.CREATED);
    }

    // 수배 전체 조회
    @GetMapping
    public ResponseEntity getWantedList(
            @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
            @PositiveOrZero @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "desc, asc", defaultValue = "desc") @RequestParam(value = "sort_direction", defaultValue = "desc") String sortDirection
    ) {
        WantedPageDTO wantedList = wantedService.getWantedList(page, size, sortDirection);
        return new ResponseEntity(wantedList, HttpStatus.OK);
    }
}
