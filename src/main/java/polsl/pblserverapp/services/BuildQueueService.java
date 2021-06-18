package polsl.pblserverapp.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.QueueRepository;
import polsl.pblserverapp.model.Queue;

@Service
public class BuildQueueService
{
    private final QueueRepository queueRepository;

    public BuildQueueService(QueueRepository queueRepository)
    {
        this.queueRepository = queueRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void insertDefaultQueues()
    {
        Queue defaultQueue = new Queue();
        defaultQueue.setName("tasks");
        if(!queueRepository.existsByName(defaultQueue.getName()))
        {
            queueRepository.save(defaultQueue);
        }

        Queue ram20GroupQueue = new Queue();
        ram20GroupQueue.setName("ram20Group");
        if(!queueRepository.existsByName(ram20GroupQueue.getName()))
        {
            queueRepository.save(ram20GroupQueue);
        }

        Queue ram256GroupQueue = new Queue();
        ram256GroupQueue.setName("ram256Group");
        if(!queueRepository.existsByName(ram256GroupQueue.getName()))
        {
            queueRepository.save(ram256GroupQueue);
        }
    }
}
