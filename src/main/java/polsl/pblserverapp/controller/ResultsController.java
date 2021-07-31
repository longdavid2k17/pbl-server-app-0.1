package polsl.pblserverapp.controller;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import polsl.pblserverapp.dao.ResultRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.Filter;
import polsl.pblserverapp.model.Result;
import polsl.pblserverapp.model.User;
import polsl.pblserverapp.services.QueueService;
import polsl.pblserverapp.services.UtilService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ResultsController
{
    private final UserRepository userRepository;
    private final QueueService queueService;
    private final ResultRepository resultRepository;
    private final UtilService utilService;
    private final Logger logger = LoggerFactory.getLogger(ResultsController.class);
    private final List<String> statuses;

    public ResultsController(UserRepository userRepository, QueueService queueService,ResultRepository resultRepository,UtilService utilService)
    {
        this.userRepository = userRepository;
        this.queueService = queueService;
        this.resultRepository = resultRepository;
        this.utilService = utilService;
        this.statuses = new ArrayList<>();
        this.statuses.add("Nowe zadanie");
        this.statuses.add("Oczekiwanie na pobranie");
        this.statuses.add("Pobrano");
        this.statuses.add("Rozpoczęto obliczanie");
        this.statuses.add("Zakończono sukcesem");
        this.statuses.add("Zakończono niepowodzeniem");
    }

    @GetMapping("/logged/results")
    public String results(Model model, HttpServletRequest request,@ModelAttribute("message") String message,@ModelAttribute("errorCode") String errorCode)
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
                model.addAttribute("statuses",statuses);
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
            }
            else
            {
                model.addAttribute("results",resultRepository.findByOwnerUsername(user.getUsername()));
                model.addAttribute("filter",new Filter());
                model.addAttribute("statuses",statuses);
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
            }
            try
            {
                queueService.areAnyMessages();
            }
            catch (Exception e)
            {
                model.addAttribute("errorCode",e.getMessage());
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
                model.addAttribute("users",userRepository.findAll());
            }
            model.addAttribute("filter",new Filter());
            model.addAttribute("statuses",statuses);
            model.addAttribute("results",filterResult(resultRepository.findAll(),filter));
            try
            {
                model.addAttribute("version",utilService.getVersion());
            }
            catch (XmlPullParserException | IOException e)
            {
                logger.error(e.getMessage());
            }

            try
            {
                queueService.areAnyMessages();
            }
            catch (Exception e)
            {
                model.addAttribute("errorCode",e.getMessage());
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
                model.addAttribute("users",userRepository.findAll());
            }
            model.addAttribute("filter",new Filter());
            model.addAttribute("statuses",statuses);
            model.addAttribute("results",filterResult(resultRepository.findAll(),filter));
            try
            {
                model.addAttribute("version",utilService.getVersion());
            }
            catch (XmlPullParserException | IOException e)
            {
                logger.error(e.getMessage());
            }

            try
            {
                queueService.areAnyMessages();
            }
            catch (Exception e)
            {
                model.addAttribute("errorCode",e.getMessage());
            }
            return "resultDir/results";
        }
        else
        {
            return "redirect:/logged";
        }
    }

    @GetMapping("/logged/results/delete")
    public String deleteAllRecords(HttpServletRequest request, RedirectAttributes ra)
    {
        Principal principal = request.getUserPrincipal();

        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            if (user.getRole().equals("ROLE_ADMIN"))
            {
                resultRepository.deleteAll();
                ra.addAttribute("message","Usunięto rekordy pomyślnie!");
                return "redirect:/logged/results";
            }
            else
            {
                ra.addAttribute("message","Błąd podczas usuwania!");
                return "redirect:/logged/results";
            }
        }
        else
        {
            return "redirect:/logged";
        }
    }

    @GetMapping("/logged/results/deletebyid/{id}")
    public String deleteById(HttpServletRequest request, RedirectAttributes ra, @PathVariable Long id)
    {
        Principal principal = request.getUserPrincipal();

        if(principal!=null)
        {
            User user = userRepository.findByUsername(principal.getName());
            if (user.getRole().equals("ROLE_ADMIN"))
            {
                resultRepository.deleteById(id);
                ra.addAttribute("message","Pomyślnie usunięto rekord !");
                return "redirect:/logged/results";
            }
            else
            {
                ra.addAttribute("message","Błąd podczas usuwania!");
                return "redirect:/logged/results";
            }
        }
        else
        {
            return "redirect:/logged";
        }
    }

    public List<Result> filterResult(List<Result> primaryList, Filter filter)
    {
        List<Result> filteredResults;
        logger.info("Starting list length: "+primaryList.size());
        boolean isStartDatePresent = false,isStartHourPresent = false,isEndDatePresent = false,isEndHourPresent = false,isStatusPresent = false;
        boolean lookForAllUsers = false;

            if(!filter.getStartDate().equals(""))
            {
                isStartDatePresent=true;
            }
            if(!filter.getStartHour().equals(""))
            {
                isStartHourPresent=true;
            }
            if(!filter.getEndDate().equals(""))
            {
                isEndDatePresent=true;
            }
            if(!filter.getEndHour().equals(""))
            {
                isEndHourPresent=true;
            }
            if(!filter.getStatus().equals(""))
            {
                isStatusPresent=true;
            }
            if(filter.getUserId().equals(""))
            {
                lookForAllUsers=true;
            }

        if(!lookForAllUsers)
        {
            filteredResults = primaryList
                    .stream()
                    .filter(c -> c.getOwnerId().equals(Long.valueOf(filter.getUserId())))
                    .collect(Collectors.toList());
        }
       else
            filteredResults = primaryList;
        logger.info("List length after first filtering: "+filteredResults.size());

        if(isStartDatePresent)
        {
            filteredResults = filterByStartDate(filteredResults,filter.getStartDate());
            logger.info("List length after next filtering: "+filteredResults.size());
        }
        if(isStartHourPresent)
        {
            filteredResults = filterByStartHour(filteredResults,filter.getStartHour());
            logger.info("List length after next filtering: "+filteredResults.size());
        }
        if(isEndDatePresent)
        {
            filteredResults = filterByEndDate(filteredResults,filter.getEndDate());
        }
        if(isEndHourPresent)
        {
            filteredResults = filterByEndHour(filteredResults,filter.getEndHour());
            logger.info("List length after next filtering: "+filteredResults.size());
        }
        if(isStatusPresent)
        {
            filteredResults = filterByStatus(filteredResults,filter.getStatus());
            logger.info("List length after next filtering: "+filteredResults.size());
        }

        return filteredResults;
    }

    public List<Result> filterByStartHour(List<Result> filteredResults, String startHour)
    {
        List<Result> startHourFilteredResults = new ArrayList<>();

        for(Result result : filteredResults)
        {
            String[] parts = startHour.split(":");
            Calendar cal1 = Calendar.getInstance();
            cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            cal1.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            cal1.set(Calendar.SECOND, Integer.parseInt("00"));

            parts = result.getCreationHour().split(":");
            Calendar cal2 = Calendar.getInstance();
            cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            cal2.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            cal2.set(Calendar.SECOND, Integer.parseInt(parts[2]));

            if (cal1.before(cal2) || cal1.equals(cal2))
            {
                startHourFilteredResults.add(result);
            }
        }
        return startHourFilteredResults;
    }

    public List<Result> filterByEndHour(List<Result> filteredResults, String endHour)
    {
        List<Result> endHourFilteredResults = new ArrayList<>();

        for(Result result : filteredResults)
        {
            if(!result.getEndingHour().equals("-"))
            {
                String[] parts = endHour.split(":");
                Calendar cal1 = Calendar.getInstance();
                cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                cal1.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                cal1.set(Calendar.SECOND, Integer.parseInt("00"));

                parts = result.getEndingHour().split(":");
                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                cal2.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                cal2.set(Calendar.SECOND, Integer.parseInt(parts[2]));

                if (cal1.after(cal2) || cal1.equals(cal2))
                {
                    endHourFilteredResults.add(result);
                }
            }
        }
        return endHourFilteredResults;
    }
    public List<Result> filterByStatus(List<Result> filteredResults, String status)
    {
        List<Result> endHourFilteredResults = new ArrayList<>();

        for(Result result : filteredResults)
        {
            if(!result.getResultStatus().equals("-"))
            {
                if (result.getResultStatus().equals(status))
                {
                    endHourFilteredResults.add(result);
                }
            }
        }
        return endHourFilteredResults;
    }

    public List<Result> filterByStartDate(List<Result> filteredResults, String startDate)
    {
        List<Result> startDayFilteredResults = new ArrayList<>();

        for(Result result : filteredResults)
        {
                String[] monthYear = startDate.split("-");
                Calendar cal1 = Calendar.getInstance();
                cal1.set(Calendar.YEAR, Integer.parseInt(monthYear[0]));
                cal1.set(Calendar.MONTH, Integer.parseInt(monthYear[1]));
                cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(monthYear[2]));

                String[] dayResult = result.getCreationDate().split("-");
                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayResult[0]));
                cal2.set(Calendar.MONTH, Integer.parseInt(dayResult[1]));
                cal2.set(Calendar.YEAR, Integer.parseInt(dayResult[2]));

                if (cal1.before(cal2) || cal1.equals(cal2))
                {
                    startDayFilteredResults.add(result);
                }
        }
        return startDayFilteredResults;
    }
    public List<Result> filterByEndDate(List<Result> filteredResults, String endDate)
    {
        List<Result> startDayFilteredResults = new ArrayList<>();

        for(Result result : filteredResults)
        {
            if(!result.getEndingDate().equals("-"))
            {
                logger.info("START DAY FILTER VAL: "+endDate);
                String[] monthYear = endDate.split("-");
                Calendar cal1 = Calendar.getInstance();
                cal1.set(Calendar.YEAR, Integer.parseInt(monthYear[0]));
                cal1.set(Calendar.MONTH, Integer.parseInt(monthYear[1]));
                cal1.set(Calendar.DAY_OF_MONTH, Integer.parseInt(monthYear[2]));
                logger.info(cal1.get(Calendar.DAY_OF_MONTH) +"-"+ cal1.get(Calendar.MONTH) +"-"+ cal1.get(Calendar.YEAR));

                logger.info("START DAY RECORD VAL: "+result.getEndingDate());
                String[] dayResult = result.getCreationDate().split("-");
                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayResult[0]));
                cal2.set(Calendar.MONTH, Integer.parseInt(dayResult[1]));
                cal2.set(Calendar.YEAR, Integer.parseInt(dayResult[2]));
                logger.info(cal2.get(Calendar.DAY_OF_MONTH) +"-"+ cal2.get(Calendar.MONTH) +"-"+ cal2.get(Calendar.YEAR));

                if (cal1.after(cal2) || cal1.equals(cal2))
                {
                    startDayFilteredResults.add(result);
                }
            }
        }
        return startDayFilteredResults;
    }
}
