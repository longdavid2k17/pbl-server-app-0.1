package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import polsl.pblserverapp.dao.QueueRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.Queue;
import polsl.pblserverapp.model.User;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

@Controller
public class QueueManagementController
{
    private final Logger logger = LoggerFactory.getLogger(QueueManagementController.class);
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;

    public QueueManagementController(QueueRepository queueRepository,UserRepository userRepository)
    {
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/logged/queues")
    public String queueManagement(HttpServletRequest request, Model model, @ModelAttribute("message") String message, @ModelAttribute("errorCode") String errorCode)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("queues",queueRepository.findAll());
            model.addAttribute("user",user);
            model.addAttribute("message",message);
            model.addAttribute("errorCode",errorCode);
            return "queue_management";
        }
        return "redirect:/logged";
    }

    @PostMapping("/logged/queues/newqueue")
    public String postNewQueue(Model model, @RequestParam String queueName, HttpServletRequest request, RedirectAttributes redirectAttributes)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user", user);
            if(queueName!=null && !queueName.equals("") && !queueRepository.existsByName(queueName))
            {
                Queue queue = new Queue();
                queue.setName(queueName);
                queueRepository.save(queue);
                redirectAttributes.addAttribute("message","Utworzono kolejkę: "+queueName);
            }
            else
            {
                logger.error("Incorrect value has passed!");
                redirectAttributes.addAttribute("errorCode","Nie udało się utworzyć kolejki. Nazwa jest nullem lub taka kolejka istnieje!");
            }
            return "redirect:/logged/queues";
        }
        else
        {
            return "redirect:/logged/queues";
        }
    }

    @GetMapping("/logged/queues/delete/{queuename}")
    public String deleteQueue(@PathVariable String queuename, Model model, HttpServletRequest request, RedirectAttributes ra)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user", user);
            if(queuename==null || queuename.equals("tasks") || queueRepository.count()==1)
            {
                ra.addAttribute("message","Nie można usunąć tego elementu / jest to ostatnia kolejka");
                return "redirect:/logged/queues";
            }
            else
            {
                Optional<Queue> tempQueue = queueRepository.getByName(queuename);
                if(tempQueue.isPresent())
                {
                    Queue queue = tempQueue.get();
                    queueRepository.delete(queue);
                    return "redirect:/logged/queues";
                }
                else
                    return "redirect:/logged/queues";
            }
        }
        else
        {
            return "redirect:/";
        }
    }

}
