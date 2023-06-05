package com.example.codebase.controller;

import com.example.codebase.domain.meme.dto.*;
import com.example.codebase.domain.meme.entity.MemeType;
import com.example.codebase.domain.meme.service.MemeService;
import com.example.codebase.util.FileUtil;
import com.example.codebase.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Api(value = "Meme APIs", description = "Meme APIs")
@RestController
@RequestMapping("/api/meme")
public class MemeController {

    // 수배 CRUD

    private final MemeService memeService;


    @Autowired
    public MemeController(MemeService memeService) {
        this.memeService = memeService;
    }

    // 짤 등록
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity createMeme(
            @Valid @RequestPart("dto") MemeCreateDTO dto,
            @RequestPart("image") MultipartFile image) throws IOException {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        dto.setUsername(loginUsername);

        int tagsSize = dto.getTags().split(" ").length;
        if (tagsSize == 0 || tagsSize > 5) {
            throw new RuntimeException("태그는 1~5개 입력해주세요.");
        }

        // dto 확장자 추출
        String originalFilename = image.getOriginalFilename();
        int index = originalFilename.lastIndexOf(".");
        String ext = originalFilename.substring(index + 1).toLowerCase();

        // image 확장자 -> jpg, png, jpeg
        if (!FileUtil.checkImageExtension(ext)) {
            throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
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
            throw new IOException("이미지 저장에 실패했습니다.");
        }

        // 짤 등록
        MemeResponseDTO meme = memeService.createMeme(dto);
        return new ResponseEntity(meme, HttpStatus.CREATED);
    }

    // 짤 전체 조회
    @GetMapping
    public ResponseEntity getMemeList(
            @ApiParam(value = "MEME,TEMPLATE", defaultValue = "MEME")
            @RequestParam(value = "type", defaultValue = "MEME") String type,
            @ApiParam(value = "0", defaultValue = "0")
            @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
            @ApiParam(value = "10", defaultValue = "10")
            @PositiveOrZero @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "desc, asc", defaultValue = "desc") @RequestParam(value = "sort_direction", defaultValue = "desc") String sortDirection,
            @ApiParam(value = "최신순(createdAt), 좋아요순(likeCount), 조회수순(viewCount)", defaultValue = "createdAt") @RequestParam(value = "sort_type", defaultValue = "createdAt") String sortType
    ) {
        Optional<String> loginUsername = SecurityUtil.getCurrentUsername();
        MemePageDTO memeList = memeService.getMemeList(MemeType.from(type), page, size, sortType, sortDirection, loginUsername);
        return new ResponseEntity(memeList, HttpStatus.OK);
    }

    @GetMapping("/{memeId}")
    public ResponseEntity getMeme(
            @PathVariable("memeId") Long memeId
    ) {
        Optional<String> loginUsername = SecurityUtil.getCurrentUsername();
        MemeResponseDTO meme = memeService.getMeme(memeId, loginUsername);
        return new ResponseEntity(meme, HttpStatus.OK);
    }

    @GetMapping("/member/{username}")
    public ResponseEntity getMemberMeme(
            @PathVariable("username") String username,
            @ApiParam(value = "0", defaultValue = "0")
            @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
            @ApiParam(value = "10", defaultValue = "10")
            @PositiveOrZero @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "desc, asc", defaultValue = "desc")
            @RequestParam(value = "desc, asc", defaultValue = "desc") String sortDirection
    ) {
        MemePageDTO memeList = memeService.getMemberMeme(username, page, size, sortDirection);
        return new ResponseEntity(memeList, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PutMapping("/{memeId}")
    public ResponseEntity updateMeme(
            @PathVariable("memeId") Long memeId,
            @RequestBody MemeUpdateDTO dto
    ) {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        if (SecurityUtil.isAdmin()) {
            MemeResponseDTO meme = memeService.updateMeme(memeId, dto);
            return new ResponseEntity(meme, HttpStatus.OK);
        }
        MemeResponseDTO meme = memeService.updateMeme(memeId, dto, loginUsername);
        return new ResponseEntity(meme, HttpStatus.OK);
    }

    @ApiOperation(value = "짤 공개 여부 수정", notes = "짤 공개 여부 수정")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PutMapping("/{memeId}/public")
    public ResponseEntity updateMemePublicFlag(
            @PathVariable("memeId") Long memeId,
            @RequestParam("flag") boolean flag
    ) {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        MemeResponseDTO meme = memeService.updateMemePublicFlag(memeId, flag, loginUsername);
        return new ResponseEntity(meme, HttpStatus.OK);
    }


    @ApiOperation(value = "짤 삭제", notes = "짤 삭제")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @DeleteMapping("/{memeId}")
    public ResponseEntity deleteMeme(
            @PathVariable("memeId") Long memeId
    ) {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        memeService.deleteMeme(memeId, loginUsername);
        return new ResponseEntity("삭제되었습니다.", HttpStatus.OK);
    }

    @ApiOperation(value = "짤 좋아요", notes = "짤 좋아요")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PostMapping("/{memeId}/like")
    public ResponseEntity likeMeme(
            @PathVariable("memeId") Long memeId
    ) {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        MemeLikeMemberReposenDTO meme = memeService.likeMeme(memeId, loginUsername);
        if (meme.getIsLiked()) {
            return new ResponseEntity(meme, HttpStatus.OK);
        }
        return new ResponseEntity(meme, HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "로그인한 사용자가 좋아요한 밈 전체 조회", notes = "사용자가 좋아요한 밈 전체 조회 [좋아요한 순]")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @GetMapping("/likes")
    public ResponseEntity getLikeMemes(
            @ApiParam(value = "0", defaultValue = "0")
            @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
            @ApiParam(value = "10", defaultValue = "10")
            @PositiveOrZero @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "desc, asc", defaultValue = "desc")
            @RequestParam(value = "desc, asc", defaultValue = "desc") String sortDirection
    ) {
        String loginUsername = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        MemePageDTO likeMemes = memeService.getLikeMemes(loginUsername, page, size, sortDirection);
        return new ResponseEntity(likeMemes, HttpStatus.OK);
    }

    @ApiOperation(value = "짤 검색", notes = "짤 검색, 주어진 키워드에 해당하는 짤 제목명, 작성자명을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity searchMeme(
            @ApiParam(value = "짤명, 작성자명", defaultValue = "")
            @RequestParam("keyword") String keyword,
            @ApiParam(value = "0", defaultValue = "0")
            @PositiveOrZero @RequestParam(value = "page", defaultValue = "0") int page,
            @ApiParam(value = "10", defaultValue = "10")
            @PositiveOrZero @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "desc, asc", defaultValue = "desc")
            @RequestParam(value = "desc, asc", defaultValue = "desc") String sortDirection
    ) {
        MemePageDTO memeList = memeService.searchMeme(keyword, page, size, sortDirection);
        return new ResponseEntity(memeList, HttpStatus.OK);
    }

    @ApiOperation(value = "사용자의 총 조회수와 좋아요 수 조회", notes = "[TOKEN 필요] 총 조회수와 좋아요 수 조회")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @GetMapping("/counts")
    public ResponseEntity getCount() {
        String username = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        MemeCountDTO memeCount = memeService.getMemeCount(username);
        return new ResponseEntity(memeCount, HttpStatus.OK);
    }
}
