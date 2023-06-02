package com.example.codebase.domain.meme.entity;


import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MemeLikeMemberId implements Serializable {

    private Long meme;

    private Long member;

}
