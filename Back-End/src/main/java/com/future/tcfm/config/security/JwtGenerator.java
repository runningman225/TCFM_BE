package com.future.tcfm.config.security;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.JwtUserDetails;
import com.future.tcfm.model.ReqResModel.LoginRequest;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.JwtUserDetailsRepository;
import com.future.tcfm.repository.UserRepository;
import com.future.tcfm.service.GroupService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JwtGenerator {
    @Value("${app.jwtSecret}")
    private static String secretKey="futureProgram";

    @Value("${app.jwtExpirationInMs}")
    private static Long jwtExpirationInMs = 1800000L;

    @Value("${app.jwtExpirationInMs}")
    private static Long refreshTokenExpirationInMs = 10800000L;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GroupService groupService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUserDetailsRepository jwtUserDetailsRepository;

    public String generateToken(String email) {
        Claims claims = Jwts.claims()
                .setSubject(email);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis()+15000L))//development phase token last 2s, lagi ngetes fungsi getNewToken di FE dengan memanfaatkan refreshToken
//                .setExpiration(new Date(System.currentTimeMillis()+jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
    public String generateRefreshToken(String id){
        return UUID.randomUUID().toString()+id;
    }
    /**
     * loginHandler below
     *
     * @param loginRequest
     * @return
     */
    public ResponseEntity loginResponse(LoginRequest loginRequest){
        System.out.println(loginRequest);
        User userExist = userRepository.findByEmailAndActive(loginRequest.getEmail(),true);
        if (userExist!=null) {
            if (passwordEncoder.matches(loginRequest.getPassword(), userExist.getPassword())){
                Map responseMap = new HashMap<>();
                String refreshToken = generateRefreshToken(userExist.getIdUser());
                String accessToken = generateToken(userExist.getEmail());
                Group groupExist= groupRepository.findByNameAndActive(userExist.getGroupName(),true);
                if(groupExist == null){
                    groupExist = Group.builder().createdDate(0L).build();
                }
                responseMap.put("accessToken",accessToken);
                responseMap.put("refreshToken",refreshToken);
                responseMap.put("role",userExist.getRole());
                responseMap.put("groupName",userExist.getGroupName());
//                responseMap.put("groupCurrentPeriod",groupExist.getCurrentPeriod());
                responseMap.put("groupCreatedDate",groupExist.getCreatedDate());
                responseMap.put("imageURL",userExist.getImageURL());
                responseMap.put("currentUser",userExist);
                List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(userExist.getRole());
                JwtUserDetails jwtUserDetails = jwtUserDetailsRepository.findByEmail(loginRequest.getEmail());
                if(jwtUserDetails== null)
                    jwtUserDetails = new JwtUserDetails();
                jwtUserDetails.setEmail(userExist.getEmail());
                jwtUserDetails.setAccessToken(accessToken);
                jwtUserDetails.setRefreshToken(refreshToken);
                jwtUserDetails.setRefreshTokenExpiredAt(new Date().getTime()+refreshTokenExpirationInMs);
                jwtUserDetails.setAuthorities(grantedAuthorities);
                jwtUserDetails.setGroupName(userExist.getGroupName());
                jwtUserDetailsRepository.save(jwtUserDetails);
                return new ResponseEntity(responseMap, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("User Not Found",HttpStatus.NOT_FOUND);
    }
}