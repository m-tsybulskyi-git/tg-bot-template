package ua.mtsybulskyi.template.repository;

import ua.mtsybulskyi.template.domain.Role;
import ua.mtsybulskyi.template.domain.UserData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserData, Long> {
    UserData findByChatId(long chatId);
    List<UserData> findAllByRoles(Role role);
}
