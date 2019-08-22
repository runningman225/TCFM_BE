package com.future.tcfm.config.security;
import com.future.tcfm.model.JwtAuthenticationToken;
import com.future.tcfm.model.JwtUserDetails;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {
    public JwtAuthenticationTokenFilter() {
        super("/api/**");
    }

    @Value("${app.jwtSecret}")
    private String secretKey = "futureProgram";

    @Autowired
    private JwtValidator validator;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException {
        String header = httpServletRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Token ")) {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,"JWTs is missing");
            return null;
        }

        String authenticationToken = header.substring(6); // ambil nilai dari token dimulai dari index ke 7
        JwtAuthenticationToken token = new JwtAuthenticationToken(authenticationToken);

        try { Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authenticationToken);
        } catch (JwtException ex) {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,ex.getMessage());
            return null;
        }
        return getAuthenticationManager().authenticate(token);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        JwtUserDetails jwtUserDetails = (JwtUserDetails)authResult.getPrincipal();
//        System.out.println(jwtUserDetails.getEmail());
        String newToken=validator.onSuccessAuth(jwtUserDetails.getEmail());
//        System.out.println("NewToken : "+newToken);
        if(newToken == null) newToken = "NULL_TOKEN";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",newToken);
        headers.add("Access-Control-Request-Headers","Authorization");
        response.setHeader("Authorization",newToken);
        response.setHeader("Access-Control-Expose-Headers","Authorization"); //agar client bisa akses header Authorization
        response.setStatus(HttpServletResponse.SC_OK);
//        System.out.println("============================================================================================================\n");
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"401 UNAUTHORIZED ACCESS");
    }
}