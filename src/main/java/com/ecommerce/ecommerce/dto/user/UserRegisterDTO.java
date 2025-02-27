package com.ecommerce.ecommerce.dto.user;

import com.ecommerce.ecommerce.model.user.Role;
import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String email;
    private String password;
    private Role role;
}
