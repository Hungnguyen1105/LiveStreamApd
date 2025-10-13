package com.example.livestream_apd.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    //Xử lý lỗi xác thực JWT
    @Override
    public void commence (HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error : {}", authException.getMessage());
        response.setContentType("application/json"); // tke kieu cha ve json
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message","Unauthorized");
        errorResponse.put("error", "Token hết hạn rồi");
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", request.getRequestURI());
        ObjectMapper mapper = new ObjectMapper(); // object mapper chuyen map sang json
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
