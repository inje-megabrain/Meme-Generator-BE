package com.example.codebase.domain.wanted.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WantedUpdateDTO {

    private String name;

    private String description;

    private Integer prize;

}
