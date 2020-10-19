package ua.mtsybulskyi.template.data;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.mtsybulskyi.template.domain.Privilege;
import ua.mtsybulskyi.template.domain.Privileges;
import ua.mtsybulskyi.template.domain.Role;
import ua.mtsybulskyi.template.domain.Roles;
import ua.mtsybulskyi.template.repository.PrivilegeRepository;
import ua.mtsybulskyi.template.repository.RoleRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private final Security security;
    boolean alreadySetup = false;

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    public SetupDataLoader(Security security, RoleRepository roleRepository, PrivilegeRepository privilegeRepository) {
        this.security = security;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) return;
        security.updateAdminTextKey();

        Privilege changeRolesPrivilege = createPrivilegeIfNotFound(Privileges.CHANGE_ROLES_PRIVILEGE.toString());
        Privilege defaultPrivileges = createPrivilegeIfNotFound(Privileges.DEFAULT_PRIVILEGES.toString());


        List<Privilege> adminPrivileges = Arrays.asList(changeRolesPrivilege, defaultPrivileges);
        List<Privilege> workerPrivileges = Arrays.asList(changeRolesPrivilege, defaultPrivileges);
        List<Privilege> userPrivileges = Arrays.asList(defaultPrivileges);

        createRoleIfNotFound(Roles.ADMIN_ROLE.toString(), adminPrivileges);
        createRoleIfNotFound(Roles.WORKER_ROLE.toString(), workerPrivileges);
        createRoleIfNotFound(Roles.USER_ROLE.toString(), userPrivileges);

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
