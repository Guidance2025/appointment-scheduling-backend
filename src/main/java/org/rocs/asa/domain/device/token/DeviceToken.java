package org.rocs.asa.domain.device.token;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.user.User;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tbl_user_device_token")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_type", length = 32)
    private String deviceType;

    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
