package org.rocs.asa.repository.notification;

import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notifications,Long> {
    @Query("SELECT n FROM Notifications n WHERE n.user.userId = :userId AND n.status = 'ACTIVE' ORDER BY n.createdAt DESC")
    List<Notifications> findActiveNotificationByUserId(@Param("userId") String userId);
    List<Notifications> findNotificationsByUser_UserId(String userId);
    Long countByUser_UserIdAndIsRead(String userId, int isRead);
    List<Notifications> findByNotificationId(Long notificationId);
    List<Notifications> findNotificationsByUser_UserIdOrderByCreatedAtDesc(String userId);
}
