package polsl.pblserverapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import polsl.pblserverapp.dao.QueueRepository;
import polsl.pblserverapp.dao.ShapeRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.*;
import polsl.pblserverapp.services.FileLoaderService;
import polsl.pblserverapp.services.QueueService;
import polsl.pblserverapp.utils.ApacheXlsxUtil;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class TasksController
{
    private final Logger logger = LoggerFactory.getLogger(TasksController.class);
    private final UserRepository userRepository;
    private final ShapeRepository shapeRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;
    private final FileLoaderService fileLoaderService;
    private Shape selectedShapeGlobal;
    private String ownerUsername;
    private Long ownerId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");


    public TasksController(UserRepository userRepository,ShapeRepository shapeRepository, QueueService queueService, FileLoaderService fileLoaderService,QueueRepository queueRepository)
    {
        this.userRepository = userRepository;
        this.shapeRepository = shapeRepository;
        this.queueService = queueService;
        this.fileLoaderService = fileLoaderService;
        this.queueRepository = queueRepository;
        selectedShapeGlobal = null;
    }

    @GetMapping("/logged/tasks")
    public String tasks(Model model, HttpServletRequest request,@ModelAttribute("message") String message)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("shapeList",shapeRepository.findAll());
            model.addAttribute("user",user);
            model.addAttribute("queues",queueRepository.findAll());
            model.addAttribute("message",message);
            logger.info("Queues: "+queueRepository.findAll());
            ownerUsername = principal.getName();
            ownerId = user.getUserId();
            return "newCalculations";
        }
            return "redirect:/logged";
    }

    @PostMapping("/logged/tasks/newqueue")
    public String postNewQueue(Model model, @RequestParam String queueName,HttpServletRequest request)
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user", user);
            if(queueName!=null && !queueRepository.existsByName(queueName))
            {
                Queue queue = new Queue();
                queue.setName(queueName);
                queueRepository.save(queue);
            }
            else
            {
                logger.error("Incorrect value has passed!");
            }
            return "redirect:/logged/tasks";
        }
        else
        {
            return "redirect:/logged/tasks";
        }
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
            if(shapeId != null)
            {
                ownerUsername = principal.getName();
                ownerId = user.getUserId();
                Shape selectedShape = shapeRepository.findByShapeId(shapeId);
                selectedShapeGlobal=selectedShape;
                model.addAttribute("shapeList",shapeRepository.findAll());
                model.addAttribute("user",user);
                model.addAttribute("task",new Task());
                model.addAttribute("selectedShape", selectedShape);
                model.addAttribute("queues",queueRepository.findAll());
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
                ownerUsername = principal.getName();
                ownerId = user.getUserId();
                Shape selectedShape = shapeRepository.findByShapeId(Long.valueOf(shapeId));
                selectedShapeGlobal = selectedShape;
                model.addAttribute("shapeList",shapeRepository.findAll());
                model.addAttribute("user",user);
                model.addAttribute("task",new Task());
                model.addAttribute("selectedShape", selectedShape);
                model.addAttribute("queues",queueRepository.findAll());
                return "newCalculations";
            }
            else
                logger.error("Given id is wrong: "+ shapeId);
                return "redirect:/logged/tasks";

        }
    }

    @PostMapping("/logged/tasks/send")
    public String sendTaskForm(Task task, RedirectAttributes ra)
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
            else if(task.getArgsValues().isEmpty())
            {
                logger.info("Args: "+task.getArgsValues().toString());
                logger.error("No values was passed to parameters");
            }
            Date date = new Date();
            task.setShape(selectedShapeGlobal);
            task.setOwnerUsername(ownerUsername);
            task.setOwnerId(ownerId);
            task.setCreationDate(dateFormat.format(date));
            task.setCreationHour(hourFormat.format(date));
            try
            {
                logger.info(task.toString());
                queueService.sendTask(task);
            }
            catch (Exception e)
            {
                ra.addAttribute("errorCode",e.getMessage());
            }

            return "redirect:/logged/results";
        }
    }

    @GetMapping("/logged/tasks/deletequeue/{queuename}")
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
                return "redirect:/logged/tasks";
            }
            else
            {
                Optional<Queue> tempQueue = queueRepository.getByName(queuename);
                if(tempQueue.isPresent())
                {
                    Queue queue = tempQueue.get();
                    queueRepository.delete(queue);
                    return "redirect:/logged/tasks";
                }
                else
                    return "redirect:/logged/tasks";
            }
        }
        else
        {
            return "redirect:/";
        }
    }

    @PostMapping("/logged/tasks/upload")
    public String uploadXlsx(@RequestParam("xlsxFile") MultipartFile xlsxFile, Model model, HttpServletRequest request, RedirectAttributes ra )
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);

            //if (Objects.requireNonNull(xlsxFile.getOriginalFilename()).isEmpty())
            if (xlsxFile.getOriginalFilename().isEmpty())
            {
                model.addAttribute("message", "Wystąpił problem z załadowaniem pliku. Spróbuj ponownie!");
                return "redirect:/logged/tasks/choose";
            }
            if(!ApacheXlsxUtil.isExcelFile(xlsxFile))
            {
                logger.error("Plik nie jest plikiem z rozszerzeniem .XLS lub XLSX");
                model.addAttribute("message", "Plik nie jest plikiem z rozszerzeniem .XLS lub XLSX");
                return "redirect:/logged/tasks/choose";
            }
            try
            {
                fileLoaderService.storeExcelFile(xlsxFile.getInputStream(), ownerId);
                logger.info("File "+ xlsxFile.getOriginalFilename()+" loaded successfully!");
                model.addAttribute("message", "Pomyślnie załadowano plik!");
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                ra.addAttribute("errorCode",e.getMessage());
            }
            return "redirect:/logged/results";
        }
        else
        {
            return "redirect:/logged";
        }
    }
}
