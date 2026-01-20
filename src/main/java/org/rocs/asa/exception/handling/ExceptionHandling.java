package org.rocs.asa.exception.handling;

import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import org.rocs.asa.domain.http.response.HttpResponse;
import org.rocs.asa.exception.domain.*;
import org.rocs.asa.exception.domain.AppointmentAlreadyExistException;
import org.rocs.asa.exception.domain.EmailNotFoundException;
import org.rocs.asa.exception.domain.UserNotFoundException;
import org.rocs.asa.exception.domain.UsernameExistsException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

import java.io.IOException;
import java.util.Map;

import static org.rocs.asa.exception.constants.ExceptionConstants.*;
@RestControllerAdvice
public class ExceptionHandling implements ErrorController {

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException(){
        return createHttpResponse(HttpStatus.UNAUTHORIZED, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> accountLockedException(LockedException exception){
        return createHttpResponse(HttpStatus.LOCKED, exception.getMessage());
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<HttpResponse> methodNotAllowedException(){
        return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, METHOD_IS_NOT_ALLOWED);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(){
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialException(){
        return createHttpResponse(HttpStatus.UNAUTHORIZED, INCORRECT_CREDENTIAL);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<HttpResponse> handleInternalAuthentication() {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, INCORRECT_CREDENTIAL);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException(){
        return createHttpResponse(HttpStatus.FORBIDDEN, INCORRECT_CREDENTIAL);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception){
        return createHttpResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception){
        return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception){
        return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException exception){
        return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<HttpResponse> usernameExistException(UsernameExistsException exception){
        return createHttpResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<HttpResponse> usernameNotFoundException(UsernameNotFoundException exception){
        return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(AppointmentAlreadyExistException.class)
    public ResponseEntity<HttpResponse> appointmentAlreadyExistException(AppointmentAlreadyExistException exception) {
        return createHttpResponse(HttpStatus.CONFLICT, exception.getMessage());
    }
    @ExceptionHandler(DeviceTokenAlreadyExist.class)
    public ResponseEntity<HttpResponse> deviceTokenAlreadyExistException(DeviceTokenAlreadyExist exception) {
        return createHttpResponse(HttpStatus.CONFLICT, exception.getMessage());
    }
    @ExceptionHandler(WeekEndException.class)
    public ResponseEntity<HttpResponse> weekEndException(WeekEndException exception) {
        return createHttpResponse(HttpStatus.CONFLICT, exception.getMessage());
    }
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<HttpResponse> rateLimitExceeded(RateLimitExceededException exception) {
        return createHttpResponse(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
    }
    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<HttpResponse> emailAlreadyExist(EmailAlreadyExistException exception) {
        return createHttpResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<HttpResponse> studentNotFound(StudentNotFoundException exception) {
        return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<String> handleOptimisticLock() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("This time slot was just booked. Please choose another time.");
    }
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<HttpResponse> postNotFoundException(PostNotFoundException exception) {
        return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> conflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal Server Error"));
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus status, String message){
        return new ResponseEntity<>(new HttpResponse(status.value(), status, status.getReasonPhrase().toUpperCase(), message.toUpperCase()), status);
    }
}