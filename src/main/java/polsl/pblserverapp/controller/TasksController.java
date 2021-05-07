package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import polsl.pblserverapp.dao.ShapeRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.Shape;
import polsl.pblserverapp.model.Task;
import polsl.pblserverapp.model.User;
import polsl.pblserverapp.services.QueueService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Date;

@Controller
public class TasksController
{
    private final Logger logger = LoggerFactory.getLogger(TasksController.class);
    private final UserRepository userRepository;
    private final ShapeRepository shapeRepository;
    private final QueueService queueService;
    private Shape selectedShapeGlobal;

    public TasksController(UserRepository userRepository,ShapeRepository shapeRepository, QueueService queueService)
    {
        this.userRepository = userRepository;
        this.shapeRepository = shapeRepository;
        this.queueService = queueService;
        selectedShapeGlobal = null;
    }

    @GetMapping("/logged/tasks")
    public String tasks(Model model, HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("shapeList",shapeRepository.findAll());
            model.addAttribute("user",user);
            return "newCalculations";
        }
            return "redirect:/logged";
    }

    @GetMapping("/logged/tasks/choosed/{id}")
    public String selectShapeFromList(@PathVariable(value = "id") Long shapeId, Model model,HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();
        if(principal==null)
        {
            return "redirect:/logged";
        }
        else
        {
            User user = userRepository.findByUsername(principal.getName());
            if(shapeId!=null && !shapeId.equals(""))
            {
                Shape selectedShape = shapeRepository.findByShapeId(Long.valueOf(shapeId));
                selectedShapeGlobal=selectedShape;
                logger.info("Given ID = "+ shapeId);
                model.addAttribute("shapeList",shapeRepository.findAll());
                model.addAttribute("user",user);
                model.addAttribute("task",new Task());
                model.addAttribute("selectedShape", selectedShape);
                return "newCalculations";
            }
            else
                logger.error("Given id is wrong: "+ shapeId);
            return "redirect:/logged/tasks";
        }
    }

    @PostMapping("/logged/tasks/choose")
    public String chooseShape(@ModelAttribute("shape") String shapeId, Model model, HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();
        if(principal==null)
        {
            return "redirect:/logged";
        }
        else
        {
            User user = userRepository.findByUsername(principal.getName());
            if(shapeId!=null && !shapeId.equals(""))
            {
                Shape selectedShape = shapeRepository.findByShapeId(Long.valueOf(shapeId));
                selectedShapeGlobal = selectedShape;
                logger.info("Given ID = "+ shapeId);
                model.addAttribute("shapeList",shapeRepository.findAll());
                model.addAttribute("user",user);
                model.addAttribute("task",new Task());
                model.addAttribute("selectedShape", selectedShape);
                return "newCalculations";
            }
            else
                logger.error("Given id is wrong: "+ shapeId);
                return "redirect:/logged/tasks";

        }
    }

    @PostMapping("/logged/tasks/send")
    public String sendTaskForm(Task task)
    {
        logger.info(task.toString());
        if(selectedShapeGlobal==null)
        {
            logger.error("Wrong value of global shape!");
            return "redirect:/logged/tasks";
        }
        else
        {
            if(task.getArgsValues().size()!= selectedShapeGlobal.getParametersList().size())
            {
                logger.error("List of values has different size than primary shape!");
                return "redirect:/";
            }
            task.setShape(selectedShapeGlobal);
            task.setCreationDate(String.valueOf(new Date()));
            queueService.sendTask(task);
            return "redirect:/logged/results";
        }
    }
}
