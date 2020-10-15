package ua.mtsybulskyi.template.data;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.mtsybulskyi.template.domain.Privilege;
import ua.mtsybulskyi.template.domain.Role;
import ua.mtsybulskyi.template.repository.PrivilegeRepository;
import ua.mtsybulskyi.template.repository.RoleRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    boolean alreadySetup = false;

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    public SetupDataLoader( RoleRepository roleRepository, PrivilegeRepository privilegeRepository) {
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) return;
        Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

        List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, writePrivilege);
        List<Privilege> userPrivileges = Arrays.asList(readPrivilege);


        createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        createRoleIfNotFound("ROLE_USER", userPrivileges);

        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege();
            privilege.setName(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }

        return role;
    }
}
