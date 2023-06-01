package com.example.codebase.domain.member.entity;

import com.example.codebase.domain.member.entity.oauth2.oAuthProvider;
import com.example.codebase.domain.meme.entity.Meme;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "member")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "password", length = 100, nullable = true)
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "picture")
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = true)
    private oAuthProvider oauthProvider;

    @Column(name = "oauth_provider_id", nullable = true)
    private String oauthProviderId;

    @Column(name = "activated")
    private boolean activated;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private Set<MemberAuthority> authorities;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Meme> memes = new ArrayList<>();

    public void setAuthorities(Set<MemberAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static User toUser(Member member) {
        return new User(member.getUsername(), member.getPassword(), member.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority().getAuthorityName()))
                .collect(Collectors.toList()));
    }

    public void update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        this.updatedTime = LocalDateTime.now();
    }

    public void addMeme(Meme meme) {
        this.memes.add(meme);
    }

    public void updateName(String newName) {
        this.name = newName;
        this.updatedTime = LocalDateTime.now();
    }

    public void updateActivated(boolean activated) {
        this.activated = activated;
        this.updatedTime = LocalDateTime.now();
    }
}
