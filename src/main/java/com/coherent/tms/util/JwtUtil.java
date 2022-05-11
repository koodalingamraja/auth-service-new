package com.coherent.tms.util;

import com.coherent.tms.dto.UserDTO;
import com.coherent.tms.model.User;
import com.coherent.tms.repository.UserRepository;
import com.coherent.tms.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    private String secret = "coherent";

    @Autowired
    private UserRepository userRepository;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public BaseResponse generateToken(String username) {

        BaseResponse response = new BaseResponse();

        User user = userRepository.findByUserName(username);
        if(user == null){
            response.setMessage("Invalid UserName");
            response.setStatus("failed");
            return response;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(user.getUserName());
        userDTO.setEmail(user.getEmail());

        Map<String, Object> userInfo = new ObjectMapper().convertValue(userDTO, Map.class);
        String token = createToken(userInfo, username);
        response.setMessage("Token Generated");
        response.setStatus("Success");
        response.setData(token);
        return response;
    }

    private String createToken(Map<String, Object> userInfo, String subject) {

        return Jwts.builder().setClaims(userInfo).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
