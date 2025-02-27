package com.ecommerce.ecommerce.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "confirmation_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "confirmation_token",  nullable = false)
    private String confirmationToken;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;  // Added expiry date field

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userEntity; // Added relation with User

    public ConfirmationToken(User user) {
        this.confirmationToken = UUID.randomUUID().toString();
        this.createdDate = new Date();
        this.expiryDate = new Date(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minutes expiration
        this.userEntity = user;
    }
}
