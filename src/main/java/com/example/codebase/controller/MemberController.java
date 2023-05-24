package com.example.codebase.controller;

import com.example.codebase.domain.member.dto.CreateMemberDTO;
import com.example.codebase.domain.member.dto.MemberResponseDTO;
import com.example.codebase.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(value = "Member APIs", description = "Member APIs")
@RestController
@RequestMapping("/api/member")
@Validated
public class MemberController {

    private MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @ApiOperation(value = "회원 가입", notes = "회원 가입을 합니다.")
    @PostMapping("")
    public ResponseEntity createMember(@Valid @RequestBody CreateMemberDTO createMemberDTO) {
        try {
            MemberResponseDTO memberResponseDTO = memberService.createMember(createMemberDTO);
            return new ResponseEntity(memberResponseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @ApiOperation(value = "전체 회원 조회", notes = "등록된 전체 회원을 조회합니다.")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity getAllMember() {
        try {
            List<MemberResponseDTO> members = memberService.getAllMember();
            return new ResponseEntity(members, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "회원 탈퇴", notes = "회원 탈퇴")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity deleteMember(@PathVariable String username) {
        try {
            String loginUesrname = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));

            // 관리자가 아닌 경우 자신의 계정만 삭제 가능
            if (!loginUesrname.equals(username) && !SecurityUtil.isAdmin()) {
                return new ResponseEntity("자신의 계정만 삭제할 수 있습니다.", HttpStatus.BAD_REQUEST);
            }

            memberService.deleteMember(username);
            return new ResponseEntity(username + " 삭제되었습니다.", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation("내 정보 조회")
    @GetMapping("/{username}")
    public ResponseEntity getMyInfo(@PathVariable String username) {
        try {
            MemberResponseDTO memberResponseDTO = memberService.getMember(username);
            return new ResponseEntity(memberResponseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
