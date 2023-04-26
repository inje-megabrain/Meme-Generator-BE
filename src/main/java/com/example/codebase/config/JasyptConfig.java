package com.example.codebase.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfig {

    @Value("${jasypt.encryptor.password}")
    private String encryptorPassword;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithSHA256And128BitAES-CBC-BC");
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setPassword(encryptorPassword);
        encryptor.setPoolSize(2);   // default 1 -> 2 recommended
        encryptor.setKeyObtentionIterations(1000);  // default 1000
        encryptor.setStringOutputType("base64");
        return encryptor;
    }
}