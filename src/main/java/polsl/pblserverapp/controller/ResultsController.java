package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import polsl.pblserverapp.dao.ResultRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.Filter;
import polsl.pblserverapp.model.User;
import polsl.pblserverapp.services.QueueService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Date;

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
                model.addAttribute("users",userRepository.findAll());
                model.addAttribute("filter",new Filter());
            }
            else
            {
                model.addAttribute("results",resultRepository.findByOwnerUsername(user.getUsername()));
                model.addAttribute("filter",new Filter());
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

    @PostMapping("/logged/results/admin")
    public String filterAdmin(Filter filter, Model model, HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();

        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);
            logger.info("Admin filter: "+filter.toString());
            if(user.getRole().equals("ROLE_ADMIN"))
            {
                model.addAttribute("results",resultRepository.findAll());
                model.addAttribute("users",userRepository.findAll());
                model.addAttribute("filter",new Filter());
            }
            else
            {
                model.addAttribute("results",resultRepository.findByOwnerUsername(user.getUsername()));
                model.addAttribute("filter",new Filter());
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

    @PostMapping("/logged/results/user")
    public String filterUser(Filter filter, Model model, HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();

        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);
            filter.setUserId(String.valueOf(user.getUserId()));
            logger.info("User filter: "+filter);
            if(user.getRole().equals("ROLE_ADMIN"))
            {
                model.addAttribute("results",resultRepository.findAll());
                model.addAttribute("users",userRepository.findAll());
                model.addAttribute("filter",new Filter());
            }
            else
            {
                model.addAttribute("results",resultRepository.findByOwnerUsername(user.getUsername()));
                model.addAttribute("filter",new Filter());
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

    boolean isWithinRange(Date searchedDate,Date startDate,Date endDate)
    {
        return !(searchedDate.before(startDate) || searchedDate.after(endDate));
    }
}
