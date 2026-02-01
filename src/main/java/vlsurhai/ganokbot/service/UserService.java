package vlsurhai.ganokbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vlsurhai.ganokbot.common.model.jpa.User;
import vlsurhai.ganokbot.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getOrCreateUser(Long userId, boolean isBot) {
        return userRepository.findById(userId).orElseGet(() -> {
            User user = new User(userId, isBot);
            return userRepository.save(user);
        });
    }
}
