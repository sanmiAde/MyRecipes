package com.sanmiade.myrecipes.features.profile

import com.sanmiade.myrecipes.utils.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1/account")
class ProfileController(private val profileService: ProfileService) {

    @GetMapping
    fun getAccount(@AuthenticationPrincipal user: UserPrincipal): ResponseEntity<UserResponse> {
        val user = profileService.getUserById(user.id)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @PutMapping
    fun updateAccount(
        @AuthenticationPrincipal user: UserPrincipal,
        @Validated @RequestBody profileRequest: ProfileRequest
    ): ResponseEntity<UserResponse> {
        val user = profileService.updateUser(user.id, profileRequest)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }
}