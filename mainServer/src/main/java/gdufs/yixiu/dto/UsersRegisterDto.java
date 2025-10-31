package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersRegisterDto {
    String phone;
    String email;
    String verificationCode;
    String role;
}
