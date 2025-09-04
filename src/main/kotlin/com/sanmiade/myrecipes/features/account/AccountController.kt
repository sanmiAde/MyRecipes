package com.sanmiade.myrecipes.features.account

import com.sanmiade.myrecipes.utils.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1/account")
class AccountController(private val accountService: AccountService) {

    @GetMapping
    fun getAccount(@AuthenticationPrincipal user: UserPrincipal): ResponseEntity<UserResponse> {
        val user = accountService.getUserById(user.id)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }
}