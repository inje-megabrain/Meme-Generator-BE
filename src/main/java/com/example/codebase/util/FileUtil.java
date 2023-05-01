package com.example.codebase.util;

import java.util.List;

public class FileUtil {

    private static List<String> allowedImageTypes = List.of("image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp");
    private static List<String> allowedVideoTypes = List.of("video/mp4", "video/mov", "video/avi", "video/quicktime", "video/webm", "video/x-msvideo", "video/x-ms-wmv");
    private static List<String> allowedAudioTypes = List.of("audio/mp3", "audio/wav", "audio/ogg", "audio/mpeg", "audio/webm", "audio/x-m4a", "audio/x-ms-wma", "audio/x-ms-wax", "audio/x-ms-wmv");

    public static boolean checkExtension(String extension) {
        return allowedImageTypes.contains("image/"+ extension) || allowedVideoTypes.contains("video/" + extension) || allowedAudioTypes.contains("audio/" + extension);
    }

    public static boolean checkImageExtension(String extension) {
        return allowedImageTypes.contains("image/"+ extension);
    }

    public static void makeDir(String s) {

    }
}
