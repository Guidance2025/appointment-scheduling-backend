package org.rocs.asa.exception.handling;

import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.persistence.NoResultException;
import org.rocs.asa.domain.http.response.HttpResponse;
import org.rocs.asa.exception.domain.EmailNotFoundException;
import org.rocs.asa.exception.domain.UserNotFoundException;
import org.rocs.asa.exception.domain.UsernameExistsException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.MethodNotAllowedException;

import java.io.IOException;

import static org.rocs.asa.exception.constants.ExceptionConstants.*;

public class ExceptionHandling implements ErrorController {

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST,ACCOUNT_DISABLED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> accountLockedException(){
        return createHttpResponse(HttpStatus.UNAUTHORIZED,ACCOUNT_IS_LOCKED);
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<HttpResponse> methodNotAllowedException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST,METHOD_IS_NOT_ALLOWED);
    }
    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST,INTERNAL_SERVER_ERR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialException(){
        return createHttpResponse(HttpStatus.BAD_REQUEST,INCORRECT_CREDENTIAL);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException(){
        return createHttpResponse(HttpStatus.FORBIDDEN,NOT_ENOUGH_PERMISSION);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception){
        return createHttpResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }
    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception){
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception){
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException exception){
        return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }
    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<HttpResponse> usernameExistException(NoResultException exception){
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus status, String message){
        return new ResponseEntity<>(new HttpResponse(status.value(),status,status.getReasonPhrase().toUpperCase(), message.toUpperCase()),status);
    }
}
