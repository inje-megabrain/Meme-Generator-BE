package com.example.codebase.domain.meme.entity;

public enum MemeType {
    MEME,
    TEMPLATE;

    public static MemeType from(String type) {
        if (type.equals("MEME")) {
            return MEME;
        } else if (type.equals("TEMPLATE")) {
            return TEMPLATE;
        } else {
            throw new RuntimeException("Invalid meme type");
        }
    }
}
