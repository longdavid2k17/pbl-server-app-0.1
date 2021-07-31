package polsl.pblserverapp.controller;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import polsl.pblserverapp.dao.QueueRepository;
import polsl.pblserverapp.dao.ShapeRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.Queue;
import polsl.pblserverapp.model.TaskRequest;
import polsl.pblserverapp.model.User;
import polsl.pblserverapp.services.FileLoaderService;
import polsl.pblserverapp.services.QueueService;
import polsl.pblserverapp.services.UtilService;
import polsl.pblserverapp.utils.ApacheXlsxUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ImportTasksController
{
    private final Logger logger = LoggerFactory.getLogger(QueueManagementController.class);
    private final UserRepository userRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;
    private final UtilService utilService;
    private final FileLoaderService fileLoaderService;
    private List<String> importedTasks;
    private Queue selectedQueue;

    public ImportTasksController(UserRepository userRepository, QueueRepository queueRepository, QueueService queueService, UtilService utilService, FileLoaderService fileLoaderService)
    {
        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
        this.queueService = queueService;
        this.utilService = utilService;
        this.fileLoaderService = fileLoaderService;
        importedTasks = new ArrayList<>();
    }

    @GetMapping("/logged/importtasks")
    public String importLayout(Model model, HttpServletRequest request, @ModelAttribute("message") String message, @ModelAttribute("errorCode") String errorCode)
    {
        Principal principal = request.getUserPrincipal();

        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            model.addAttribute("user",user);
            model.addAttribute("message",message);
            model.addAttribute("errorCode",errorCode);
            model.addAttribute("queues",queueRepository.findAll());
            model.addAttribute("fileName","");
            model.addAttribute("targetQueue",new Queue());
            importedTasks = new ArrayList<>();
            model.addAttribute("importedTasks",importedTasks);
            try
            {
                model.addAttribute("version",utilService.getVersion());
            }
            catch (XmlPullParserException | IOException e)
            {
                logger.error(e.getMessage());
            }
            model.addAttribute("tasks",new TaskRequest());
            return "import_tasks";
        }
        return "redirect:/logged";
    }

    @PostMapping("/logged/importtasks/upload")
    public String uploadXlsx(@RequestParam("xlsxFile") MultipartFile xlsxFile, @RequestParam("queueId") Long queueId, Model model, HttpServletRequest request, RedirectAttributes ra )
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            try
            {
                model.addAttribute("version",utilService.getVersion());
            }
            catch (XmlPullParserException | IOException e)
            {
                logger.error(e.getMessage());
            }
            model.addAttribute("user",user);

            if (xlsxFile.getOriginalFilename().isEmpty())
            {
                ra.addAttribute("errorCode", "Wystąpił problem z załadowaniem pliku. Spróbuj ponownie!");
                return "redirect:/logged/importtasks";
            }
            if(!ApacheXlsxUtil.isExcelFile(xlsxFile))
            {
                logger.error("Plik nie jest plikiem z rozszerzeniem .XLS lub XLSX");
                ra.addAttribute("errorCode", "Plik nie jest plikiem z rozszerzeniem .XLS lub XLSX");
                return "redirect:/logged/importtasks";
            }
            try
            {
                model.addAttribute("fileName",xlsxFile.getOriginalFilename());
                if(queueId!=null)
                {
                    importedTasks = fileLoaderService.loadAndReturnExcelTasks(xlsxFile.getInputStream(), user.getUserId(),queueId);
                    logger.info("File "+ xlsxFile.getOriginalFilename()+" loaded successfully!");
                    model.addAttribute("message", "Pomyślnie załadowano plik!");
                    model.addAttribute("importedTasks",importedTasks);
                    Queue queue = queueRepository.findById(queueId).get();
                    model.addAttribute("targetQueue",queue);
                    selectedQueue = queue;
                }
                else
                {
                    Optional<Queue> defaultQueue = queueRepository.getByName("tasks");
                    if(defaultQueue.isPresent())
                    {
                        logger.info("File "+ xlsxFile.getOriginalFilename()+" loaded successfully, but inserted default queue ID!");
                        model.addAttribute("message", "Pomyślnie załadowano plik ale oznaczono kolejkę domyślną!");
                        Queue queue = defaultQueue.get();
                        model.addAttribute("targetQueue",queue);
                        selectedQueue = queue;
                    }
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage());
                ra.addAttribute("errorCode",e.getMessage());
                return "redirect:/importtasks";
            }
            model.addAttribute("tasks",new TaskRequest());
            return "import_tasks";
        }
        else
        {
            return "redirect:/logged";
        }
    }

    @PostMapping("/logged/importtasks/send")
    public String sendSelectedTasks(@Valid TaskRequest taskRequest, Model model, HttpServletRequest request, RedirectAttributes ra )
    {
        Principal principal = request.getUserPrincipal();
        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());

            if(taskRequest!=null && taskRequest.getSelectedTasks().size()>0)
            {
                try
                {
                    queueService.sendTaskList(taskRequest.getSelectedTasks(),user.getUserId(),selectedQueue.getId());
                    return "redirect:/logged/results";
                }
                catch (Exception e)
                {
                    ra.addAttribute("errorCode",e.getMessage());
                    return "redirect:/logged/importtasks";
                }
            }
            else
            {
                ra.addAttribute("errorCode","Nie wybrano żadnych zadań!");
                return "redirect:/logged/importtasks";
            }
        }
        else
        {
            return "redirect:/logged";
        }
    }
}
