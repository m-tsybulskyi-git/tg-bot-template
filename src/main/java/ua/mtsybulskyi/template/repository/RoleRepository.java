package ua.mtsybulskyi.template.repository;

import org.springframework.data.repository.CrudRepository;
import ua.mtsybulskyi.template.domain.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByName(String name);
}
