package com.ecommerce.ecommerce.repository.user;

import com.ecommerce.ecommerce.model.user.ConfirmationToken;
import com.ecommerce.ecommerce.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("confirmationTokenRepository")
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {
    ConfirmationToken findByConfirmationToken(String confirmationToken);
    ConfirmationToken findByUserEntity(User user);
}