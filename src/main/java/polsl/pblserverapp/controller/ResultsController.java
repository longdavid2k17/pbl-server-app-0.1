package polsl.pblserverapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class ResultsController
{
    private final UserRepository userRepository;

    public ResultsController(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @GetMapping("/logged/results")
    public String results(Model model, HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();

        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);
            return "resultDir/results";
        }
        else
        {
            return "redirect:/logged";
        }
    }
}
