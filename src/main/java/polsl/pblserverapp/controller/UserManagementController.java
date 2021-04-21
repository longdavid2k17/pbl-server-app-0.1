package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class UserManagementController
{
    private UserRepository userRepository;
    private Logger log = LoggerFactory.getLogger(UserManagementController.class);

    public UserManagementController(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @GetMapping("/manager")
    public String managementPanel(HttpServletRequest request,Model model)
    {
        Principal principal = request.getUserPrincipal();
        User user = userRepository.findByUsername(principal.getName());
        if(user.getRole()!="ROLE_ADMIN")
        {
            return "redirect:/logged";
        }
        else
        {
            model.addAttribute("userList",userRepository.findAll());
            return "usermanagment";
        }

    }
}
