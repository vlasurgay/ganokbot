package vlsurhai.ganokbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.MemberStatus;
import vlsurhai.ganokbot.common.model.jpa.Chat;
import vlsurhai.ganokbot.common.model.jpa.ChatMember;
import vlsurhai.ganokbot.repository.ChatMemberRepository;

import java.util.List;


@Service
public class ChatMemberService {

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Transactional
    public ChatMember createOrUpdateChatMember(Long userId, Long chatId, String status) {
        ChatMember member = chatMemberRepository.findByUserIdAndChatId(userId, chatId)
                .orElseGet(() -> new ChatMember(userId, chatId, status));
        member.setStatus(status);
        return chatMemberRepository.save(member);
    }

    @Transactional
    public void deleteChatMembersByChatId(Long chatId) {
        chatMemberRepository.deleteByChatId(chatId);
    }

    @Transactional
    public void deleteChatMember(Long userId, Long chatId) {
        chatMemberRepository.deleteByUserIdAndChatId(userId, chatId);
    }

    public List<Chat> findChatsByUserStatus(Long userId, String ... statuses) {
        return chatMemberRepository.findChatsByUserStatus(userId, statuses);
    }

    @Transactional
    public void downgradeAdminsNotIn(Long chatId, List<Long> keepIds) {
        chatMemberRepository.updateChatMemberStatusesNotIn(chatId, keepIds, MemberStatus.MEMBER,
                List.of(MemberStatus.ADMINISTRATOR, MemberStatus.CREATOR));
    }
}
