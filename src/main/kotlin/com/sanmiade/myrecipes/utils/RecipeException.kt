package com.sanmiade.myrecipes.utils

class InvalidCredentialsException : RuntimeException("Invalid username or password")
class UserAlreadyExistsException(username: String) : RuntimeException("User '$username' already exists")
class PasswordMismatchException : RuntimeException("Passwords do not match")
class InvalidRefreshTokenException : RuntimeException("Invalid refresh token")
class ExpiredRefreshTokenException : RuntimeException("Refresh token expired")
class UserNotFoundException(message: String) : RuntimeException(message)
