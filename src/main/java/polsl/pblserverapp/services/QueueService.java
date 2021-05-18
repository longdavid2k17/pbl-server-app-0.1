package polsl.pblserverapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.ResultRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.QueueConfiguration;
import polsl.pblserverapp.model.Result;
import polsl.pblserverapp.model.Task;
import polsl.pblserverapp.model.User;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class QueueService
{
    private final Logger logger = LoggerFactory.getLogger(QueueService.class);
    private final QueueConfiguration configuration;
    private final ResultRepository resultsRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");

    public QueueService(QueueConfiguration configuration, ResultRepository resultsRepository,UserRepository userRepository)
    {
        this.configuration = configuration;
        this.resultsRepository = resultsRepository;
        this.userRepository = userRepository;
        CachingConnectionFactory factory = new CachingConnectionFactory(configuration.getHostAddress());
        this.rabbitTemplate = new RabbitTemplate(factory);
    }

    public void sendTask(Task task)
    {
        logger.info("Using RabbitConfiguration: "+configuration.toString());
        StringBuilder buildedTask = new StringBuilder(task.getShape().getCommand() + " ");
        for(int i=0;i<task.getShape().getParametersList().size();i++)
        {
            //String pair = task.getShape().getParametersList().get(i).getSwitchParam()+":"+task.getArgsValues().get(i)+" ";
            String pair = task.getShape().getParametersList().get(i).getSwitchParam()+task.getArgsValues().get(i)+" ";
            buildedTask.append(pair);
        }

        Result result = new Result();
        result.setResultStatus("Nowe zadanie");
        result.setCreationDate(task.getCreationDate());
        result.setCreationHour(task.getCreationHour());
        result.setOwnerId(task.getOwnerId());
        result.setEndingDate("-");
        result.setEndingHour("-");
        result.setOwnerUsername(task.getOwnerUsername());
        result.setShapeId(task.getShape().getShapeId());
        result.setFullCommand(buildedTask.toString());
        result.setResultsUrl(configuration.getLocalizationUrl());
        Result savedResult = resultsRepository.save(result);
        savedResult.setFullCommand("'"+configuration.getLocalizationUrl()+"' ID"+savedResult.getId()+" "+ buildedTask);
        Result savedFullResult = resultsRepository.save(savedResult);
        rabbitTemplate.convertAndSend(configuration.getOutputQueueName(),savedFullResult.getFullCommand() );
    }

    public void sendTaskList(List<String> tasks, Long ownerId)
    {
        if(!tasks.isEmpty())
        {
            User user = userRepository.findByUserId(ownerId);
            Date date = new Date();
            for(int i=0;i< tasks.size();i++)
            {
                Result result = new Result();
                result.setResultStatus("Nowe zadanie");
                result.setCreationDate(dateFormat.format(date));
                result.setCreationHour(hourFormat.format(date));
                result.setOwnerId(ownerId);
                result.setEndingDate("-");
                result.setEndingHour("-");
                result.setOwnerUsername(user.getUsername());
                //result.setShapeId(task.getShape().getShapeId());
                result.setFullCommand(tasks.get(i));
                result.setResultsUrl(configuration.getLocalizationUrl());
                Result savedResult = resultsRepository.save(result);
                savedResult.setFullCommand("'"+configuration.getLocalizationUrl()+"' ID"+savedResult.getId()+" "+ tasks.get(i));
                Result savedFullResult = resultsRepository.save(savedResult);
                rabbitTemplate.convertAndSend(configuration.getOutputQueueName(), savedFullResult.getFullCommand());
            }
        }
    }


    @Scheduled(fixedRate = 1000)
    public void areAnyMessages()
    {
        Object message = rabbitTemplate.receiveAndConvert(configuration.getInputQueueName());
        if(message!=null)
        {
            String mess = null;
            try
            {
                mess= new String((byte[]) message, "UTF-8");
                logger.info("Odebrano"+mess);
            }
            catch (UnsupportedEncodingException e)
            {
                logger.error(e.getMessage());
            }
            if(mess!=null)
            {
                String globalMessage = mess;
                Optional<Result> optionalResult = checkResult(globalMessage);
                if(optionalResult.isPresent())
                {
                    Result result = optionalResult.get();
                    resultsRepository.save(result);
                }
                else
                {
                    logger.error("Metoda areAnyMessages() - Nie ma takiego rekordu!");
                }
            }
        }
    }

    public Optional<Result> checkResult(String message)
    {
        List<Result> actualResults = resultsRepository.findAll();
        logger.info(message);
        logger.info("message length = "+message.length());
        for(Result result : actualResults)
        {
            logger.info("============================");
            logger.info(result.getFullCommand());
            String trimmedMessage = message.replaceAll("\\s+","");
            String trimmedCommand = result.getFullCommand().replaceAll("\\s+","");
            logger.info(trimmedMessage);
            logger.info(trimmedCommand);
            //if(message.trim().startsWith(result.getFullCommand().trim()) && message.length()>=result.getFullCommand().length())
            if(trimmedMessage.startsWith(trimmedCommand) && trimmedMessage.length()>=trimmedCommand.length())
            {
                String status = trimmedMessage.substring(trimmedCommand.length());
                if(status.equals("zakonczono"))
                {
                    Date date = new Date();
                    result.setEndingDate(dateFormat.format(date));
                    result.setEndingHour(hourFormat.format(date));
                    result.setResultStatus("Zakończono sukcesem");
                }
                else if(status.equals("rozpoczeto"))
                {
                    result.setResultStatus("Rozpoczęto obliczanie");
                }
                else if(status.equals("pobrano"))
                {
                    result.setResultStatus("Pobrano");
                }
                else if(status.startsWith("niepowodzenie"))
                {
                    String errorCode = status.substring("niepowodzenie/".length());
                    result.setResultStatus("Zakończono niepowodzeniem");
                    Date date = new Date();
                    result.setEndingDate(dateFormat.format(date));
                    result.setEndingHour(hourFormat.format(date));
                    result.setErrorCode(errorCode);
                }
                return Optional.of(result);
            }
        }
        logger.info("============================");
        logger.error("Metoda checkResult() - Nie ma takiego rekordu!");
        return Optional.empty();
    }
}
