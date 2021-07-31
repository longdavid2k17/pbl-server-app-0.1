package polsl.pblserverapp.controller;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;
import polsl.pblserverapp.services.UtilService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;

@Controller
public class HomePanelController
{
    private final UserRepository userRepository;
    private final UtilService utilService;

    public HomePanelController(UserRepository userRepository,UtilService utilService)
    {
        this.userRepository = userRepository;
        this.utilService = utilService;
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
            try
            {
                model.addAttribute("version",utilService.getVersion());
            }
            catch (XmlPullParserException | IOException e)
            {
                e.printStackTrace();
            }
        }
        return "navbarUser";
    }
}
