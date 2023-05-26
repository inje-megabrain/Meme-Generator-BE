package com.example.codebase.domain.item.entity;

public enum ItemCategory {

    상의, 하의, 이모티콘, 말풍선;

    public static ItemCategory from(String category) {
        if (category.equals("상의"))
            return 상의;
        else if (category.equals("하의"))
            return 하의;
        else if (category.equals("이모티콘"))
            return 이모티콘;
        else if (category.equals("말풍선"))
            return 말풍선;
        else {
            throw new RuntimeException("올바르지 않은 카테고리입니다.");
        }
    }

}
