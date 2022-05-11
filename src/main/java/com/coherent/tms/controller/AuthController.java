package com.coherent.tms.controller;

import com.coherent.tms.dto.AuthDTO;
import com.coherent.tms.dto.UserDTO;
import com.coherent.tms.response.BaseResponse;
import com.coherent.tms.response.UserContextHolder;
import com.coherent.tms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/authenticate")
    public BaseResponse generateToken(@RequestBody AuthDTO authDTO) throws Exception {
        return jwtUtil.generateToken(authDTO.getUserName());
    }

    @GetMapping("/")
    public String welcome() {
        UserDTO userDTO = UserContextHolder.getUserDto();
//        System.out.println(userDTO.getUserName());
        return "Welcome to coherent";
    }
}
