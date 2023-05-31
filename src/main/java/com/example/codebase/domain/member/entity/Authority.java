package com.example.codebase.domain.member.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Authority {

    @Id
    @Column(name = "authority_name", length = 50)
    private String authorityName;

    @OneToMany(mappedBy = "authority")
    private Set<MemberAuthority> memberAuthorities;

    public static Authority of(String roleGuest) {
        return Authority.builder()
                .authorityName(roleGuest)
                .build();
    }
}
