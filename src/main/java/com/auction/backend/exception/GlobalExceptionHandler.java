package com.auction.backend.exception;

import com.auction.backend.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j // [NO MAGIC]: Lombok tự động tạo ra một biến 'log' để ghi chép
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Đã sửa lại kiểu dữ liệu thành AppException
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException e) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 2. Bắt lỗi Validation (Hoàn hảo)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Dữ liệu đầu vào không hợp lệ, vui lòng kiểm tra lại!");
        error.setErrors(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 3. Trạm gác cuối cùng chặn mọi lỗi
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        // Chữ "e" ở cuối cùng sẽ báo cho Slf4j tự động in toàn bộ stack trace màu đỏ ra console.
        log.error("Lỗi hệ thống không xác định: ", e);

        // Giấu nhẹm nguyên nhân thật sự, chỉ trả về cho Frontend câu xin lỗi
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Hệ thống đang gặp sự cố, vui lòng thử lại sau!");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}