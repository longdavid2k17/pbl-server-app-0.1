package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import polsl.pblserverapp.dao.ShapeRepository;
import polsl.pblserverapp.dao.SwitchParameterRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.Shape;
import polsl.pblserverapp.model.SwitchParameter;
import polsl.pblserverapp.model.User;
import polsl.pblserverapp.services.FileLoaderService;
import polsl.pblserverapp.utils.ApacheCommonsCsvUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
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
    private final FileLoaderService fileLoaderService;

    public ShapeController(UserRepository userRepository, SwitchParameterRepository switchParameterRepository, ShapeRepository shapeRepository, FileLoaderService fileLoaderService)
    {
        this.userRepository = userRepository;
        this.switchParameterRepository = switchParameterRepository;
        this.shapeRepository = shapeRepository;
        this.fileLoaderService = fileLoaderService;
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
    public String completeShapes(Model model, HttpServletRequest request, HttpServletResponse response)
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

    @GetMapping("/logged/shapes/getAll")
    public void getAllShapes(HttpServletResponse response)
    {
        log.warn("Preparing export!");
        try
        {
            List<Shape> shapeList = shapeRepository.findAll();
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=shapes.csv");
            //OpenCSVUtil.shapesToCsv(response.getWriter(),shapeList);
            ApacheCommonsCsvUtil.shapesToCsv(response.getWriter(),shapeList);
            log.info("File downloaded!");
        }
        catch (IOException e)
        {
            log.error(e.getMessage());
        }
    }

    @PostMapping("/logged/shapes/newswitch")
    public String postNewSwitch(Model model, @RequestParam String switchValue,HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user", user);
            if(switchValue!=null)
            {
                switchParameterRepository.save(new SwitchParameter(switchValue));
            }
            else
            {
                log.error("Incorrect value has passed!");
            }
            return "redirect:/logged/shape/new";
        }
        else
        {
            return "redirect:/logged";
        }
    }

    @PostMapping("/logged/shapes/upload")
    public String uploadCsv(@RequestParam("csvfile")MultipartFile csvfile, Model model,HttpServletRequest request )
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);

            if (csvfile.getOriginalFilename().isEmpty())
            {
                model.addAttribute("message", "Wystąpił problem z załadowaniem pliku. Spróbuj ponownie!");
                return "/shape/chooseSet";
            }
            if(!ApacheCommonsCsvUtil.isCSVFile(csvfile))
            {
                model.addAttribute("message", "Plik nie jest plikiem z rozszerzeniem .CSV");
                return "/shape/chooseSet";
            }
            try
            {
                fileLoaderService.store(csvfile.getInputStream());
                log.info("File "+csvfile.getName()+" loaded successfully!");
                model.addAttribute("message", "Pomyślnie załadowano plik!");
            }
            catch (Exception e)
            {
                log.error(e.getMessage());
            }
            List<Shape> shapeList = shapeRepository.findAll();
            model.addAttribute("shapeList",shapeList);
            return "/shape/chooseSet";
        }
        else
        {
            return "redirect:/logged";
        }
    }
}
