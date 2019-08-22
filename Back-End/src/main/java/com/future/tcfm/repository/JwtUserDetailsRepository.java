package com.future.tcfm.repository;

import com.future.tcfm.model.JwtUserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JwtUserDetailsRepository extends MongoRepository<JwtUserDetails, String> {
    JwtUserDetails findByEmail(String email);
    List<JwtUserDetails> findAllByGroupName(String gName);
    JwtUserDetails findByRefreshToken(String refreshToken);
    void deleteAllByGroupName(String gName);
    void deleteByEmail(String email);
    JwtUserDetails findByAccessTokenAndRefreshToken(String accessToken,String refreshToken);
}
