package com.nexterp.platform.notification.domain.repository;

import com.nexterp.platform.notification.domain.model.SysNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SysNotificationRepository extends JpaRepository<SysNotification, Long> {

    /**
     * 查询用户未读通知
     *
     * @param receiverId 接收人ID
     * @return 通知列表
     */
    @Query("SELECT n FROM SysNotification n WHERE n.receiverId = :receiverId AND n.isRead = false AND n.isDeleted = false ORDER BY n.priority DESC, n.createdAt DESC")
    List<SysNotification> findUnreadByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 查询用户所有通知
     *
     * @param receiverId 接收人ID
     * @return 通知列表
     */
    @Query("SELECT n FROM SysNotification n WHERE n.receiverId = :receiverId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<SysNotification> findByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 查询待发送的通知
     *
     * @param currentTime 当前时间
     * @return 通知列表
     */
    @Query("SELECT n FROM SysNotification n WHERE n.sendStatus = 0 AND n.createdAt <= :currentTime AND n.isDeleted = false ORDER BY n.priority DESC, n.createdAt ASC")
    List<SysNotification> findPendingNotifications(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计用户未读通知数量
     *
     * @param receiverId 接收人ID
     * @return 未读数量
     */
    @Query("SELECT COUNT(n) FROM SysNotification n WHERE n.receiverId = :receiverId AND n.isRead = false AND n.isDeleted = false")
    Long countUnreadByReceiverId(@Param("receiverId") Long receiverId);
}
