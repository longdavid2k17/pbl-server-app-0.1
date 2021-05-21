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
        admin.setUsername("dawid@student.polsl.pl");
        admin.setPassword(passwordEncoder.encode("zaq1@wsx2020"));
        admin.setRole("ROLE_ADMIN");
        admin.setEnabled(true);
        User user = new User();
        user.setUsername("adam@student.polsl.pl");
        user.setPassword(passwordEncoder.encode("zaq1@wsx2020"));
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        User user2 = new User();
        user2.setUsername("sebastian@student.polsl.pl");
        user2.setPassword(passwordEncoder.encode("zaq1@wsx2020"));
        user2.setRole("ROLE_USER");
        user2.setEnabled(false);
        if(!userRepository.existsUserByUsername(admin.getUsername()))
        {
            userRepository.save(admin);
        }
        if(!userRepository.existsUserByUsername(user.getUsername()))
        {
            userRepository.save(user);
        }
        if(!userRepository.existsUserByUsername(user2.getUsername()))
        {
            userRepository.save(user2);
        }
    }
}
