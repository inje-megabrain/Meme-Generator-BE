package com.example.codebase.config;

import com.example.codebase.MemeGeneratorApplication;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MemeGeneratorApplication.class)
@ActiveProfiles("test")
class JasyptConfigTest {

    @Autowired
    @Qualifier("jasyptStringEncryptor")
    private StringEncryptor stringEncryptor;

    @DisplayName("Jasypt 암호화")
    @Test
    void encrypt() {
        String text = "hell world";
        System.out.println(String.format("ENC(%s)", stringEncryptor.encrypt(text)));
    }

    @DisplayName("Jasypt 복호화")
    @Test
    void decrypt() {
        String text = "ko4AjdHG0A9aqM/Xh5TRwUWVsTFFmwXw/3p1tnUWTjo=";
        System.out.println(String.format("%s", stringEncryptor.decrypt(text)));
    }

}