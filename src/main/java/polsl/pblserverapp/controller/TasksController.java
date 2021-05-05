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

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class TasksController
{
    private final Logger logger = LoggerFactory.getLogger(TasksController.class);
    private final UserRepository userRepository;
    private final ShapeRepository shapeRepository;

    public TasksController(UserRepository userRepository,ShapeRepository shapeRepository)
    {
        this.userRepository = userRepository;
        this.shapeRepository = shapeRepository;
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
            //model.addAttribute("shape",new Shape());
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
}
