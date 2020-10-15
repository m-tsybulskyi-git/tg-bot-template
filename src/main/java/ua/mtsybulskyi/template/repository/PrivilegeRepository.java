package ua.mtsybulskyi.template.repository;

import org.springframework.data.repository.CrudRepository;
import ua.mtsybulskyi.template.domain.Privilege;

public interface PrivilegeRepository extends CrudRepository<Privilege, Long> {
    Privilege findByName(String name);
}
