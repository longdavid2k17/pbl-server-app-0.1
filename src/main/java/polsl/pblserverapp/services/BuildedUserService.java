package polsl.pblserverapp.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;

@Service
public class BuildedUserService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BuildedUserService(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void insertUsers()
    {
        User admin = new User();
        admin.setUsername("admin@student.polsl.pl");
        admin.setPassword(passwordEncoder.encode("zaq1@wsx2020"));
        admin.setRole("ROLE_ADMIN");
        admin.setEnabled(true);
        if(!userRepository.existsUserByUsername(admin.getUsername()))
        {
            userRepository.save(admin);
        }
    }
}
