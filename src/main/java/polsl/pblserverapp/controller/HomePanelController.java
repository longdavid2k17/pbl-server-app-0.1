package polsl.pblserverapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePanelController
{
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
    public String home()
    {
        return "navbarUser";
    }
}
