package vn.edu.uth.quanlidaythem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.edu.uth.quanlidaythem.dto.Request.ChangePasswordRequest;
import vn.edu.uth.quanlidaythem.dto.Response.ChangePasswordResponse;
import vn.edu.uth.quanlidaythem.service.AccountService;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

   @PostMapping("/change-password")
public ResponseEntity<ChangePasswordResponse> changePassword(
        @Valid @RequestBody ChangePasswordRequest req) {
    ChangePasswordResponse res = accountService.changePassword(req);
    return res.isSuccess()
            ? ResponseEntity.ok(res)
            : ResponseEntity.badRequest().body(res);
}

}
