package org.ashkelyonok.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.ashkelyonok.apigateway.dto.ErrorResponseDto;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Component
@Order(GlobalExceptionHandler.HANDLER_ORDER)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    public static final int HANDLER_ORDER = -2;

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status;
        String errorType;
        String message = ex.getMessage();

        if (ex instanceof JwtException || ex instanceof InvalidSecurityParametersException) {
            status = HttpStatus.UNAUTHORIZED;
            errorType = "Invalid Token";
            log.warn("JWT Security error: {}", message);
        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            errorType = status.getReasonPhrase();
            log.debug("Routing or Status Error: {}", message);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorType = "Internal Server Error";
            log.error("Unexpected Gateway Error", ex);
        }

        return buildResponse(exchange, response, status, errorType, message);
    }

    private Mono<Void> buildResponse(ServerWebExchange exchange, ServerHttpResponse response, HttpStatus status, String errorType, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorType)
                .message(message)
                .path(exchange.getRequest().getURI().getPath())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing response", e);
            String rawError = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", errorType, message);
            DataBuffer buffer = response.bufferFactory().wrap(rawError.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }
}