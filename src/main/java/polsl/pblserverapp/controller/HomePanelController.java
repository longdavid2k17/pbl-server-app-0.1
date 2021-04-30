package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.QueueConfiguration;
import polsl.pblserverapp.model.User;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class HomePanelController
{
    private final Logger log = LoggerFactory.getLogger(HomePanelController.class);
    private final UserRepository userRepository;
    private final QueueConfiguration queueConfiguration;

    public HomePanelController(UserRepository userRepository, QueueConfiguration queueConfiguration)
    {
        this.userRepository = userRepository;
        this.queueConfiguration = queueConfiguration;
    }

    @GetMapping("/")
    public String redirect()
    {
        return "redirect:/signin";
    }

    @GetMapping("/signin")
    public String loginForm()
    {
        return "login_form";
    }

    @GetMapping("/logged")
    public String home(HttpServletRequest request, Model model)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);
        }
        return "navbarUser";
    }
}
