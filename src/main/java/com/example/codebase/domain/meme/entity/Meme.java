package com.example.codebase.domain.meme.entity;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.meme.dto.MemeUpdateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "meme")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Meme {

    @Id
    @Column(name = "meme_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private MemeType type;

    @Builder.Default
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "public_flag")
    private boolean publicFlag;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "meme", cascade = CascadeType.ALL)
    private List<MemeLikeMember> memeLikeMembers;

    public void update(MemeUpdateDTO dto) {
        this.name = dto.getName();
        this.updatedAt = LocalDateTime.now();
    }
    public String getUsername() {
        return this.member.getUsername();
    }

    public void updatePublicFlag(boolean flag) {
        this.publicFlag = flag;
        this.updatedAt = LocalDateTime.now();
    }

    public void incressViewCount() {
        this.viewCount++;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

}
