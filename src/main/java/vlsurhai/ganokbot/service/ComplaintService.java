package vlsurhai.ganokbot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vlsurhai.ganokbot.common.model.jpa.Complaint;
import vlsurhai.ganokbot.repository.ComplaintRepository;

import java.time.LocalDateTime;

import static java.util.concurrent.TimeUnit.DAYS;

@Slf4j
@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    public boolean hasUserComplained(Long initiatorId, Long targetUserId, Long chatId) {
        return complaintRepository.existsByInitiatorIdAndTargetUserIdAndChatIdAndExpiresAtAfter(
                initiatorId, targetUserId, chatId, LocalDateTime.now()
        );
    }

    public long getComplaintsAmount(Long targetUserId, Long chatId) {
        return complaintRepository.countByChatIdAndTargetUserIdAndExpiresAtAfter(chatId, targetUserId, LocalDateTime.now());
    }

    @Transactional
    public void registerComplaint(Long chatId, Long initiatorId, Long targetUserId, LocalDateTime expiresAt) {
        Complaint complaint = new Complaint(chatId, targetUserId, initiatorId, expiresAt);
        complaintRepository.save(complaint);
    }

    @Transactional
    public void cleanupComplaints(Long targetUserId, Long chatId) {
        complaintRepository.deleteByTargetUserIdAndChatId(targetUserId, chatId);
    }

    @Transactional
    @Scheduled(fixedDelay = 1, timeUnit = DAYS)
    private void cleanupExpiredComplaint() {
        complaintRepository.deleteExpired(LocalDateTime.now());
    }
}
