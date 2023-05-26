package com.example.codebase.advice;

import com.example.codebase.controller.dto.ErrorCode;
import com.example.codebase.controller.dto.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.util.Objects;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleRuntimeException(RuntimeException e) {
        RestResponse response = new RestResponse(false, e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccessDeniedException(AccessDeniedException e) {
        RestResponse response = new RestResponse(false, e.getMessage(), HttpStatus.FORBIDDEN.value());
        return new ResponseEntity(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity handleAuthenticationException(AuthenticationException e) {
        RestResponse response = new RestResponse(false, e.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity handleInternal(final NullPointerException e) {
        RestResponse response = new RestResponse(false, e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        RestResponse response = makeErrorResponse(e.getBindingResult());
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MissingServletRequestPartException.class})
    public ResponseEntity handleMissingServletRequestPartException(final MissingServletRequestPartException e) {
        RestResponse response = new RestResponse(false, e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity handleIOException(IOException e) {
        RestResponse response = new RestResponse(false, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception e) {
        RestResponse response = new RestResponse(false, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    private static RestResponse makeErrorResponse(BindingResult bindingResult) {
        String code = "";
        String description = "";
        String detail = "";

        //에러가 있다면
        if (bindingResult.hasErrors()) {
            //DTO에 설정한 message값을 가져온다.
            detail = bindingResult.getFieldError().getDefaultMessage();

            //DTO에 유효성체크를 걸어놓은 어노테이션명을 가져온다.
            String bindResultCode = bindingResult.getFieldError().getCode();

            switch (Objects.requireNonNull(bindResultCode)) {
                case "NotNull" :
                    code = ErrorCode.NOT_NULL.getCode();
                    description = ErrorCode.NOT_NULL.getDescription();
                    break;
                case "NotBlank":
                    code = ErrorCode.NOT_BLANK.getCode();
                    description = ErrorCode.NOT_NULL.getDescription();
                    break;
                case "Pattern":
                    code = ErrorCode.PATTERN.getCode();
                    description = ErrorCode.PATTERN.getDescription();
                    break;
                case "Email":
                    code = ErrorCode.EMAIL.getCode();
                    description = ErrorCode.EMAIL.getDescription();
                    break;
            }
        }
        return new RestResponse(false, description, detail, code);
    }


}
