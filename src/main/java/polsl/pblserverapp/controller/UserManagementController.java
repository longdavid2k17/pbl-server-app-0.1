package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class UserManagementController
{
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(UserManagementController.class);

    public UserManagementController(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @GetMapping("/manager")
    public String managementPanel(HttpServletRequest request,Model model)
    {
        Principal principal = request.getUserPrincipal();
        User user = userRepository.findByUsername(principal.getName());
        if(!user.getRole().equals("ROLE_ADMIN"))
        {
            return "redirect:/logged";
        }
        else
        {
            model.addAttribute("user",user);
            model.addAttribute("userList",userRepository.findAll());
            return "usermanagment";
        }
    }

    @GetMapping("/manager/activate/{userId}")
    public String activateUser(@PathVariable(value = "userId") Long userId,Model model)
    {
        log.warn("Passed ID="+userId);
        if(userId==null)
        {
            model.addAttribute("message","Podane ID użytkownika jest niepoprawne!");
            log.error("Passed ID is incorrect!");
        }
        else
        {
            User user = userRepository.findByUserId(userId);
            if(user!=null && !user.isEnabled())
            {
                user.setEnabled(true);
                userRepository.save(user);
                log.info("User "+user.getUsername()+" updated!");
            }
            else
            {
                model.addAttribute("message","Nie udało się aktywować użytkownika!");
                log.error("Cannot activate indicated user!");
            }
        }
        return "redirect:/manager";
    }

    @GetMapping("/manager/changerole/{userId}")
    public String setAdminRole(@PathVariable(value = "userId") Long userId,Model model)
    {
        log.warn("Passed ID="+userId);
        if(userId==null)
        {
            model.addAttribute("message","Podane ID użytkownika jest niepoprawne!");
            log.error("Passed ID is incorrect!");
        }
        else
        {
            User user = userRepository.findByUserId(userId);
            if(user!=null)
            {
                user.setRole("ROLE_ADMIN");
                userRepository.save(user);
                log.info("User "+user.getUsername()+" updated!");
            }
            else
            {
                model.addAttribute("message","Nie udało się podnieść uprawnień użytkownika!");
                log.error("Cannot set admin role for indicated user!");
            }
        }
        return "redirect:/manager";
    }
}
