package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import polsl.pblserverapp.dao.ResultRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.User;
import polsl.pblserverapp.services.QueueService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class ResultsController
{
    private final UserRepository userRepository;
    private final QueueService queueService;
    private final ResultRepository resultRepository;
    private final Logger logger = LoggerFactory.getLogger(ResultsController.class);

    public ResultsController(UserRepository userRepository, QueueService queueService,ResultRepository resultRepository)
    {
        this.userRepository = userRepository;
        this.queueService = queueService;
        this.resultRepository = resultRepository;
    }

    @GetMapping("/logged/results")
    public String results(Model model, HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();

        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);
            if(user.getRole().equals("ROLE_ADMIN"))
            {
                model.addAttribute("results",resultRepository.findAll());
            }
            else
            {
                model.addAttribute("results",resultRepository.findByOwnerUsername(user.getUsername()));
            }

            if(queueService.areAnyMessages())
            {
                logger.info(queueService.getGlobalMessage());
            }
            return "resultDir/results";
        }
        else
        {
            return "redirect:/logged";
        }
    }
}
