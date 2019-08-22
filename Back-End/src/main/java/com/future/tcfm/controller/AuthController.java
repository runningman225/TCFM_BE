package com.future.tcfm.controller;

import com.future.tcfm.config.security.JwtGenerator;
import com.future.tcfm.config.security.JwtValidator;
import com.future.tcfm.model.ReqResModel.LoginRequest;
import com.future.tcfm.model.ReqResModel.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private JwtValidator jwtValidator;

    @PostMapping(value = "/signin",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity signIn(@RequestBody final LoginRequest loginRequest) {
        return jwtGenerator.loginResponse(loginRequest);
    }
    @PostMapping(value = "/refreshtoken",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getRefreshToken(
            @RequestBody TokenResponse tokenResponse
            ) {
        return jwtValidator.getRefreshToken(tokenResponse.getAccessToken(),tokenResponse.getRefreshToken());
    }
    @PutMapping(value = "/signout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity signOut(@RequestBody TokenResponse tokenResponse){
        return jwtValidator.signOut(tokenResponse.getAccessToken(),tokenResponse.getRefreshToken());
    }
}
