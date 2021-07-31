package polsl.pblserverapp.controller;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
import polsl.pblserverapp.services.UtilService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Controller
public class QueueManagementController
{
    private final Logger logger = LoggerFactory.getLogger(QueueManagementController.class);
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final UtilService utilService;

    public QueueManagementController(QueueRepository queueRepository,UserRepository userRepository,UtilService utilService)
    {
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
        this.utilService = utilService;
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
            try
            {
                model.addAttribute("version",utilService.getVersion());
            }
            catch (XmlPullParserException | IOException e)
            {
                logger.error(e.getMessage());
            }
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
            if(queueName!=null)
            {
                if(!queueName.equals("") && !queueRepository.existsByName(queueName))
                {
                    Queue queue = new Queue();
                    queue.setName(queueName);
                    queueRepository.save(queue);
                    redirectAttributes.addAttribute("message","Utworzono kolejkę: "+queueName);
                }
                else
                {
                    redirectAttributes.addAttribute("errorCode","Nie udało się utworzyć kolejki. Taka kolejka już istnieje!");
                }
            }
            else
            {
                logger.error("Incorrect value has passed!");
                redirectAttributes.addAttribute("errorCode","Nie udało się utworzyć kolejki. Nazwa jest nullem!");
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
            if(queuename==null || queueRepository.count()==1)
            {
                ra.addAttribute("errorCode","Nie można usunąć tego elementu - jest to ostatnia kolejka");
                return "redirect:/logged/queues";
            }
            else if( queuename.equals("tasks"))
            {
                ra.addAttribute("errorCode","Nie można usunąć domyślnej kolejki!");
                return "redirect:/logged/queues";
            }
            else
            {
                Optional<Queue> tempQueue = queueRepository.getByName(queuename);
                if(tempQueue.isPresent())
                {
                    Queue queue = tempQueue.get();
                    queueRepository.delete(queue);
                    ra.addAttribute("message","Usunięto kolejkę!");
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
