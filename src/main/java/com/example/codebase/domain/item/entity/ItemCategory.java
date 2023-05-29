package com.example.codebase.domain.item.entity;

public enum ItemCategory {

    도구, 악세서리, 이모티콘, 말풍선;

    public static ItemCategory from(String category) {
        if (category.equals("도구"))
            return ItemCategory.도구;
        else if (category.equals("악세서리"))
            return ItemCategory.악세서리;
        else if (category.equals("이모티콘"))
            return ItemCategory.이모티콘;
        else if (category.equals("말풍선"))
            return ItemCategory.말풍선;
        else {
            throw new RuntimeException("올바르지 않은 카테고리입니다.");
        }
    }

}
