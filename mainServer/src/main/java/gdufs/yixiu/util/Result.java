package gdufs.yixiu.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;
    public static <T> Result<T> success(T data) {return new Result<>(200,"success",data);}
    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }
    public static <T> Result<T> insufficientPermissions() {
        return new Result<>(403, "Permission denied", null);
    }
    public static <T> Result<T> fail(Integer code, String msg) { return new Result<>(code, msg, null);}
}
