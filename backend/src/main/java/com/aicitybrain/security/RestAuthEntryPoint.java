package com.aicitybrain.security;

import com.aicitybrain.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Without this, Spring Security's default fallback for an unauthenticated request to a
 * protected endpoint is {@code Http403ForbiddenEntryPoint} (403), which is the wrong
 * status for "you never logged in" — that's 401. This also keeps the JSON body in the
 * exact same {@link ErrorResponse} shape the rest of the API uses (see
 * {@code GlobalExceptionHandler}), so callers never see two different error formats.
 */
@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
            "Authentication is required to access this resource", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
