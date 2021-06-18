package polsl.pblserverapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.ResultRepository;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.QueueConfiguration;
import polsl.pblserverapp.model.Result;
import polsl.pblserverapp.model.Task;
import polsl.pblserverapp.model.User;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

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
    private Connection connection = null;
    private final CachingConnectionFactory factory;

    public QueueService(QueueConfiguration configuration, ResultRepository resultsRepository,UserRepository userRepository)
    {
        this.configuration = configuration;
        this.resultsRepository = resultsRepository;
        this.userRepository = userRepository;
        factory = new CachingConnectionFactory(configuration.getHostAddress());
        factory.setUsername("springuser");
        factory.setPassword("springpassword");
        this.rabbitTemplate = new RabbitTemplate(factory);
    }

    public void sendTask(Task task) throws Exception
    {
        connection = factory.createConnection();
        if(connection!=null)
        {
            try
            {
                logger.info("Using RabbitConfiguration: "+configuration.toString());
                StringBuilder buildedTask = new StringBuilder(task.getShape().getCommand() + " ");
                for(int i=0;i<task.getShape().getParametersList().size();i++)
                {
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
                result.setQueueName(task.getSelectedQueueName());
                result.setShapeId(task.getShape().getShapeId());
                result.setFullCommand(buildedTask.toString());
                result.setResultsUrl(configuration.getLocalizationUrl());
                Result savedResult = resultsRepository.save(result);
                savedResult.setFullCommand("'"+configuration.getLocalizationUrl()+"' ID"+savedResult.getId()+" "+ buildedTask);
                Result savedFullResult = resultsRepository.save(savedResult);
                RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);
                Properties properties = rabbitAdmin.getQueueProperties(result.getQueueName());
                if(properties==null)
                {
                    Queue queue = new Queue(result.getQueueName(),false,false,false);
                    rabbitAdmin.declareQueue(queue);
                }
                rabbitTemplate.convertAndSend(result.getQueueName(),savedFullResult.getFullCommand() );
            }
            catch (Exception e)
            {
                logger.error("Error while connection! Code: "+e.getMessage());
                throw new Exception("Error while connection!\nCheck your RabbitMQ server\nYours tasks will not be sended!\nCode: "+e.getMessage());
            }
        }
    }

    public void sendTaskList(List<String> tasks, Long ownerId) throws Exception
    {
        if(!tasks.isEmpty())
        {
            connection = factory.createConnection();
            if(connection!=null)
            {
                try
                {
                    User user = userRepository.findByUserId(ownerId);
                    Date date = new Date();
                    for (String task : tasks)
                    {
                        Result result = new Result();
                        result.setResultStatus("Nowe zadanie");
                        result.setCreationDate(dateFormat.format(date));
                        result.setCreationHour(hourFormat.format(date));
                        result.setOwnerId(ownerId);
                        result.setEndingDate("-");
                        result.setEndingHour("-");
                        result.setOwnerUsername(user.getUsername());
                        result.setFullCommand(task);
                        result.setResultsUrl(configuration.getLocalizationUrl());
                        Result savedResult = resultsRepository.save(result);
                        savedResult.setFullCommand("'" + configuration.getLocalizationUrl() + "' ID" + savedResult.getId() + " " + task);
                        Result savedFullResult = resultsRepository.save(savedResult);
                        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);
                        Properties properties = rabbitAdmin.getQueueProperties(result.getQueueName());
                        if(properties==null)
                        {
                            Queue queue = new Queue(result.getQueueName(),false,false,false);
                            rabbitAdmin.declareQueue(queue);
                        }
                        rabbitTemplate.convertAndSend(result.getQueueName(), savedFullResult.getFullCommand());
                    }
                }
                catch (Exception e)
                {
                    logger.error("Error while connection! Code: "+e.getMessage());
                    throw new Exception("Error while connection!\nCheck your RabbitMQ server\nYours tasks will not be sended!\nCode: "+e.getMessage());
                }
            }
        }
    }


    @Scheduled(fixedRate = 1000)
    public void areAnyMessages() throws Exception
    {
        try
        {
            Object message = rabbitTemplate.receiveAndConvert(configuration.getInputQueueName());
            if(message!=null)
            {
                String mess = null;
                mess= new String((byte[]) message, StandardCharsets.UTF_8);
                if(mess!=null)
                {
                    Optional<Result> optionalResult = checkResult(mess);
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
        catch (Exception e)
        {
            logger.error("Error while connection! Code: "+e.getMessage());
            throw new Exception("Error while connection!\nCheck your RabbitMQ server!\n Code: "+e.getMessage());
        }

    }

    public Optional<Result> checkResult(String message)
    {
        List<Result> actualResults = resultsRepository.findAll();
        for(Result result : actualResults)
        {
            String trimmedMessage = message.replaceAll("\\s+","");
            String trimmedCommand = result.getFullCommand().replaceAll("\\s+","");
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
        return Optional.empty();
    }
}
