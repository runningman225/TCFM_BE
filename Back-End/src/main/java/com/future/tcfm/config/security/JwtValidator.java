package com.future.tcfm.config.security;

import com.future.tcfm.model.JwtUserDetails;
import com.future.tcfm.repository.JwtUserDetailsRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class JwtValidator {

    @Value("${app.jwtSecret}")
    private String secretKey;


    @Value("${app.refreshTokenExpirationInMS}")
    private Long refreshTokenExpirationInMs = 10800000L;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private JwtUserDetailsRepository jwtUserDetailsRepository;

    public JwtUserDetails validate(String token) {

        JwtUserDetails jwtUserDetails;
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            jwtUserDetails = jwtUserDetailsRepository.findByEmail(body.getSubject());
        } catch (JwtException e){
            throw new JwtException(e.getMessage());
        }
        return jwtUserDetails;
    }

    /**
     * kasih user aksestoken baru
     * perpanjang durasi refreshToken user yngbersangkutan
     * @param email
     * @return
     */
    public String onSuccessAuth(String email){
        JwtUserDetails currentUser = jwtUserDetailsRepository.findByEmail(email);
        if(currentUser == null) throw new RuntimeException("Current user is null!");
        String newToken =  jwtGenerator.generateToken(email);
        currentUser.setAccessToken(newToken);
        currentUser.setRefreshTokenExpiredAt(System.currentTimeMillis()+refreshTokenExpirationInMs);
        currentUser.setLastModifiedAt(System.currentTimeMillis());
        currentUser.setGroupName(currentUser.getGroupName());
        jwtUserDetailsRepository.save(currentUser);
//        System.out.println("Refresh token expired at : "+ new Date(currentUser.getRefreshTokenExpiredAt()));
        return newToken;
    }
    public ResponseEntity getRefreshToken(String accesToken, String refreshToken){
//        System.out.println(accesToken+"\n"+refreshToken);
//        JwtUserDetails jwtUserDetails = jwtUserDetailsRepository.findByAccessTokenAndRefreshToken(accesToken,refreshToken);
        JwtUserDetails jwtUserDetails = jwtUserDetailsRepository.findByRefreshToken(refreshToken);
        if(jwtUserDetails==null){
            return new ResponseEntity<>("404 token not found", HttpStatus.NOT_FOUND);
        }
        if(jwtUserDetails.getRefreshTokenExpiredAt()<System.currentTimeMillis()){
            jwtUserDetailsRepository.delete(jwtUserDetails);
            return new ResponseEntity<>("RefreshToken is expired. Please re-login",HttpStatus.UNAUTHORIZED);
        }
        String newToken = jwtGenerator.generateToken(jwtUserDetails.getEmail());
//        String newRefreshToken = jwtGenerator.generateRefreshToken(jwtUserDetails.getId());
//        jwtUserDetails.setAccessToken(newToken);
//        jwtUserDetails.setRefreshToken(newRefreshToken);
        jwtUserDetails.setRefreshToken(refreshToken);

        jwtUserDetails.setRefreshTokenExpiredAt(new Date().getTime()+refreshTokenExpirationInMs);
        Map<String,String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken",newToken);
//        tokenMap.put("refreshToken",newRefreshToken);
        tokenMap.put("refreshToken",refreshToken);
        jwtUserDetails.setRefreshToken(refreshToken);
        jwtUserDetails.setLastModifiedAt(System.currentTimeMillis());
        jwtUserDetailsRepository.save(jwtUserDetails);
        return new ResponseEntity(tokenMap,HttpStatus.OK);
    }

    public ResponseEntity signOut(String accessToken,String refreshToken){
        JwtUserDetails jwtUserDetails = jwtUserDetailsRepository.findByAccessTokenAndRefreshToken(accessToken,refreshToken);
        if(jwtUserDetails==null){
            return new ResponseEntity("404 CurrentUser not found", HttpStatus.NOT_FOUND);
        }
        jwtUserDetailsRepository.delete(jwtUserDetails);
        return new ResponseEntity("Logout succeed",HttpStatus.OK);
    }

}