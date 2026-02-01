package vlsurhai.ganokbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vlsurhai.ganokbot.common.model.jpa.User;

public interface UserRepository extends JpaRepository<User, Long> {}
