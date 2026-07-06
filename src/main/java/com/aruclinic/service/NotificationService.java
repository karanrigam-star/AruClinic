package com.aruclinic.service;

import com.aruclinic.entity.Notification;
import com.aruclinic.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for notification operations.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void clearNotificationsByUserId(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteNotificationById(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deletePrescriptionNotification(Long userId, String prescriptionId) {
        List<Notification> list = notificationRepository.findByUserId(userId);
        for (Notification n : list) {
            if (n.getTitle() != null && n.getTitle().contains(prescriptionId)) {
                notificationRepository.delete(n);
            }
        }
    }
}