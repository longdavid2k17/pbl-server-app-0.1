package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import polsl.pblserverapp.dao.ShapeRepository;
import polsl.pblserverapp.dao.SwitchParameterRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.Shape;
import polsl.pblserverapp.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Date;
import java.util.List;

@Controller
public class ShapeController
{
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(ShapeController.class);
    private final SwitchParameterRepository switchParameterRepository;
    private final ShapeRepository shapeRepository;

    public ShapeController(UserRepository userRepository, SwitchParameterRepository switchParameterRepository, ShapeRepository shapeRepository)
    {
        this.userRepository = userRepository;
        this.switchParameterRepository = switchParameterRepository;
        this.shapeRepository = shapeRepository;
    }

    @GetMapping("/logged/shape/new")
    public String newShapeForm(HttpServletRequest request, Model model)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);
            model.addAttribute("shape",new Shape());
            model.addAttribute("switchesList",switchParameterRepository.findAll());
        }
        return "shape/addShape";
    }

    @PostMapping("/logged/shape/new")
    public String processNewShape(@Valid Shape shape, Errors errors,Model model,HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();
        User user = userRepository.findByUsername(principal.getName());
        if(!user.getRole().equals("ROLE_ADMIN"))
        {
            return "redirect:/logged";
        }
        else
        {
            if(errors.hasErrors())
            {
                model.addAttribute("user",user);
                //TODO znaleźć prawidłowy błąd do przekazania w message
                //model.addAttribute("message",errors.getGlobalError());
                model.addAttribute("message","Nie wybrano żadnego parametru!");
                model.addAttribute("switchesList",switchParameterRepository.findAll());
                return "shape/addShape";
            }
            log.info("SHAPE_ATTR: "+shape.getParametersList());
            model.addAttribute("user", user);
            shape.setCreationDate(new Date());
            log.info("New shape: "+shape);
            shapeRepository.save(shape);
            return "redirect:/logged";
        }
    }

    @GetMapping("/logged/shapes")
    public String completeShapes(Model model,HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();
        User user = userRepository.findByUsername(principal.getName());
        if(user.getRole().equals(null))
        {
            return "redirect:/logged";
        }
        else
        {
            List<Shape> shapeList = shapeRepository.findAll();
            model.addAttribute("user",user);
            model.addAttribute("shapeList",shapeList);
            return "/shape/chooseSet";
        }
    }
}
