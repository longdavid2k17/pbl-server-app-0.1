package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;

import javax.validation.Valid;

@Controller
public class RegistrationController
{
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(RegistrationController.class);
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository,PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registrationForm(Model model)
    {
        model.addAttribute("user",new User());
        return "register_form";
    }

    @PostMapping("/register")
    public String proceed(@Valid User user, Model model)
    {
        if(user.getUsername()!=null && user.getPassword()!=null)
        {
            if(!userRepository.existsUserByUsername(user.getUsername()))
            {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setRole("ROLE_USER");
                user.setEnabled(false);
                log.info("Proceed: "+ user);
                userRepository.save(user);
                model.addAttribute("message","Pomyślnie utworzono konto!");
                return "redirect:/signin";
            }
            else
            {
                model.addAttribute("message","Konto z takim adresem email już istnieje!");
                return "register_form";
            }
        }
        else
        {
            model.addAttribute("message","Nie wypełniono formularza!");
            return "register_form";
        }
    }
}
