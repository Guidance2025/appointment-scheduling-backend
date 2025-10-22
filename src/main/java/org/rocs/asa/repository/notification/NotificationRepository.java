package org.rocs.asa.repository.notification;

import org.rocs.asa.domain.notification.Notifications;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notifications,Long> {
    List<Notifications> findByUser_UserIdOrderByCreatedAtDesc(String userId);
    Long countByUser_UserIdAndIsRead(String userId, int isRead);

}
