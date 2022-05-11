package com.coherent.tms.config;

import com.coherent.tms.dto.UserDTO;
import com.coherent.tms.response.UserContextHolder;
import com.coherent.tms.service.UserService;
import com.coherent.tms.util.JwtUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService service;


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = httpServletRequest.getHeader("Authorization");

//        if(authorizationHeader.equalsIgnoreCase("") && authorizationHeader != null){
//            httpServletResponse.sendError(401);
//        }

        String token = null;
        String userName = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);

            String accesToken = token.replaceAll("Bearer","");
            System.out.println("accesToken::"+accesToken);
            if(!accesToken.equalsIgnoreCase("")){

                String[] split = accesToken.split("\\.");
//                String base64EncodedHeader = split[0];
//                String base64EncodedBody = split[1];
//                String base64EncodedSignature = split[2];

                Base64.Decoder decoder = Base64.getUrlDecoder();

                String header = new String(decoder.decode(split[0]));
                String payload = new String(decoder.decode(split[1]));
                System.out.println("payload::"+payload);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
                UserDTO userDetail = objectMapper.readValue(payload, UserDTO.class);

                UserContextHolder.setUserDto(userDetail);

            }
            userName = jwtUtil.extractUsername(token);
        }

        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = service.loadUserByUsername(userName);

            if (jwtUtil.validateToken(token, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
