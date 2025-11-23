package org.rocs.asa.domain.notification;

import jakarta.persistence.*;
import lombok.Data;
import org.rocs.asa.domain.appointment.Appointment;
import org.rocs.asa.domain.user.User;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "tbl_notification")
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Column(name = "action_type", length = 64)
    private String actionType;

    @Column(name = "is_read", length = 1, nullable = false)
    private Integer isRead ;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status")
    private String status = "ACTIVE";

}
