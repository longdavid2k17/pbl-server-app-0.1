package polsl.pblserverapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class HomePanelController
{
    private final UserRepository userRepository;

    public HomePanelController(UserRepository userRepository)
    {
        this.userRepository = userRepository;
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
