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
import javax.validation.constraints.Pattern;
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
        MemberResponseDTO memberResponseDTO = memberService.createMember(createMemberDTO);
        return new ResponseEntity(memberResponseDTO, HttpStatus.CREATED);
    }

    @ApiOperation(value = "전체 회원 조회", notes = "등록된 전체 회원을 조회합니다.")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity getAllMember() {
        List<MemberResponseDTO> members = memberService.getAllMember();
        return new ResponseEntity(members, HttpStatus.OK);
    }

    @ApiOperation(value = "회원 탈퇴", notes = "회원 탈퇴")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity deleteMember(@PathVariable String username) {
        String loginUesrname = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));

        // 관리자가 아닌 경우 자신의 계정만 삭제 가능
        if (!loginUesrname.equals(username) && !SecurityUtil.isAdmin()) {
            return new ResponseEntity("자신의 계정만 삭제할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        memberService.deleteMember(username);
        return new ResponseEntity(username + " 삭제되었습니다.", HttpStatus.OK);
    }

    @ApiOperation("내 정보 조회")
    @GetMapping("/{username}")
    public ResponseEntity getMyInfo(@PathVariable String username) {
        MemberResponseDTO memberResponseDTO = memberService.getMember(username);
        return new ResponseEntity(memberResponseDTO, HttpStatus.OK);
    }


    @ApiOperation(value = "이름(닉네임) 수정", notes = "내 이름을 수정합니다.")
    @PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_USER')")
    @PutMapping("/name")
    public ResponseEntity updateName(
            @Pattern(regexp = "^[가-힣a-zA-Z]{2,10}$", message = "이름은 2자 이상 10자 이하의 한글 또는 영어로 입력해주세요.")
            @RequestParam String newName) {
        String loginUesrname = SecurityUtil.getCurrentUsername().orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
        memberService.updateName(loginUesrname, newName);
        return new ResponseEntity(loginUesrname + " 에서 " + newName + "으로 수정되었습니다", HttpStatus.OK);
    }

    @ApiOperation(value = "이메일 중복 체크 API", notes = "이메일 중복 체크 API")
    @GetMapping("/email")
    public ResponseEntity checkEmail(@RequestParam String email) {
        boolean isExist = memberService.checkEmail(email);
        if (isExist) {
            throw new RuntimeException("이미 사용중인 이메일입니다.");
        }
        return new ResponseEntity("사용가능한 이메일입니다.", HttpStatus.OK);
    }

    @ApiOperation(value = "아이디 중복 체크 API", notes = "아이디 중복 체크 API")
    @GetMapping("/username")
    public ResponseEntity checkUsername(@RequestParam String username) {
        boolean isExist = memberService.checkUsername(username);
        if (isExist) {
            throw new RuntimeException("이미 사용중인 아이디입니다.");
        }
        return new ResponseEntity("사용가능한 아이디입니다.", HttpStatus.OK);
    }
}
