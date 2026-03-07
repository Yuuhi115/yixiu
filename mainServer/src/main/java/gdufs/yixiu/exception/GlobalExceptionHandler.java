package gdufs.yixiu.exception;


import gdufs.yixiu.util.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.fail(e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException e) {
        StringBuilder errorMsg = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errorMsg.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        });
        return Result.fail(errorMsg.toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        StringBuilder errorMsg = new StringBuilder();
        e.getConstraintViolations().forEach(violation -> {
            errorMsg.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
        });
        return Result.fail(errorMsg.toString());
    }
}
