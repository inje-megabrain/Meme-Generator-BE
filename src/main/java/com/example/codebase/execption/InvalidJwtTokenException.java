package com.example.codebase.execption;

public class InvalidJwtTokenException extends RuntimeException {
    
        public InvalidJwtTokenException(String message) {
            super(message);
        }
        

}
