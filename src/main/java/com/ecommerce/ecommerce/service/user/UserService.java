package com.ecommerce.ecommerce.service.user;

import com.ecommerce.ecommerce.model.user.User;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<?> saveUser(User user);

    ResponseEntity<?> confirmEmail(String confirmationToken);
}