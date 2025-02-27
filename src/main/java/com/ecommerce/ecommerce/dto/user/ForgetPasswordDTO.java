package com.ecommerce.ecommerce.dto.user;

import lombok.Data;

@Data
public class ForgetPasswordDTO {
    private String email;
    private String password;
    private String confirmPassword;
}
