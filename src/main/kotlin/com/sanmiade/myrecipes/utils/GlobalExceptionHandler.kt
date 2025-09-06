package com.sanmiade.myrecipes.utils

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = ex.message ?: "Unauthorized",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
    }

    @ExceptionHandler(value = [InvalidRefreshTokenException::class, ExpiredRefreshTokenException::class])
    fun handleRefreshErrors(ex: RuntimeException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = ex.message ?: "Unauthorized",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserExists(ex: UserAlreadyExistsException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.message ?: "Conflict",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    @ExceptionHandler(PasswordMismatchException::class)
    fun handlePasswordMismatch(ex: PasswordMismatchException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message ?: "Bad Request",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val msg = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        val body = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = if (msg.isBlank()) "Validation error" else msg,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiError> {
        val body = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "An unexpected error occurred",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}