package polsl.pblserverapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.ResultRepository;
import polsl.pblserverapp.model.QueueConfiguration;
import polsl.pblserverapp.model.Result;
import polsl.pblserverapp.model.Task;

@Service
public class QueueService
{
    private final Logger logger = LoggerFactory.getLogger(QueueService.class);
    private final QueueConfiguration configuration;
    private final ResultRepository resultsRepository;
    private final RabbitTemplate rabbitTemplate;
    private String globalMessage;

    public QueueService(QueueConfiguration configuration, ResultRepository resultsRepository)
    {
        this.configuration = configuration;
        this.resultsRepository = resultsRepository;
        CachingConnectionFactory factory = new CachingConnectionFactory(configuration.getHostAddress());
        this.rabbitTemplate = new RabbitTemplate(factory);
    }

    public void sendTask(Task task)
    {
        logger.info("Using RabbitConfiguration: "+configuration.toString());
        StringBuilder buildedTask = new StringBuilder(task.getShape().getCommand() + " ");
        for(int i=0;i<task.getShape().getParametersList().size();i++)
        {
            String pair = task.getShape().getParametersList().get(i).getSwitchParam()+":"+task.getArgsValues().get(i)+" ";
            buildedTask.append(pair);
        }
        logger.info("Final command: "+buildedTask);
        rabbitTemplate.convertAndSend(configuration.getOutputQueueName(), buildedTask.toString());
        Result result = new Result();
        result.setResultStatus("Rozpoczęto");
        result.setCreationDate(task.getCreationDate());
        result.setEndingDate("-");
        result.setShapeId(task.getShape().getShapeId());
        result.setFullCommand(buildedTask.toString());
        result.setResultsUrl("Domyślna lokalizacja");
        resultsRepository.save(result);
    }


    @Scheduled(fixedRate = 1000)
    public boolean areAnyMessages()
    {
        Object message = rabbitTemplate.receiveAndConvert(configuration.getOutputQueueName());
        if(message==null)
        {
            return false;
        }
        else
            globalMessage = message.toString();
        logger.info("Recived: "+globalMessage);
        return true;
    }

    public String getGlobalMessage()
    {
        return globalMessage;
    }
}
