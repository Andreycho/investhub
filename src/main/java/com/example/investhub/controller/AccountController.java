package com.example.investhub.controller;

import com.example.investhub.model.dto.response.ResetAccountResponse;
import com.example.investhub.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/reset")
    public ResponseEntity<ResetAccountResponse> resetAccount(@AuthenticationPrincipal UserDetails userDetails) {
        ResetAccountResponse response = accountService.resetAccount(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}