package com.example.codebase.domain.mail.service;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.util.RedisUtil;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.util.UUID;

@Service
public class MailService {
    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;

    private final MemberRepository memberRepository;

    public MailService(JavaMailSender javaMailSender, RedisUtil redisUtil, MemberRepository memberRepository) {
        this.javaMailSender = javaMailSender;
        this.redisUtil = redisUtil;
        this.memberRepository = memberRepository;
    }

    public void sendMail(String email) {
        // Member가 activate false가 아니면 이미 인증된 회원이므로 인증코드를 보낼 필요가 없음
        memberRepository.findByEmailAndActivated(email, false)
                .orElseThrow(() -> new RuntimeException("이메일 인증된 회원이거나 존재하지 않는 회원입니다."));

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[짤 생성기] 이메일 인증");
            String code = UUID.randomUUID().toString().replace("-", "");
            redisUtil.setDataAndExpire(code, email, 60 * 1000 * 5);

            StringBuilder sb = new StringBuilder();

            sb.append("<img src=\"");
            sb.append("https://meme.be.megabrain.kr/images/logo.png");
            sb.append("\"/>");
            sb.append("<br/>");
            sb.append("<h1>이메일 인증</h1>");
            sb.append("<h3>아래의 링크를 접속해주세요. </3>");
            sb.append("<h3>인증링크: ");
            sb.append("<a href=\"");
            sb.append("https://meme.megabrain.kr/auth/email" + "?code=" + code.toString());
            sb.append("\">");
            sb.append("https://meme.megabrain.kr/auth/email" + "?code=" + code.toString());
            sb.append("</a></h3>");
            sb.append("<h3>인증링크는 5분간 유효합니다.</h3>");

            helper.setText(sb.toString(), true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
