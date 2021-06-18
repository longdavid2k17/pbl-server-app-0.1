package polsl.pblserverapp.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.SwitchParameterRepository;
import polsl.pblserverapp.model.SwitchParameter;

@Service
public class BuildParametersService
{
    private final SwitchParameterRepository parameterRepository;

    public BuildParametersService(SwitchParameterRepository parameterRepository)
    {
        this.parameterRepository = parameterRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void insertParameters()
    {
        if(!parameterRepository.existsBySwitchParam("/a"))
        {
            parameterRepository.save(new SwitchParameter("/a"));
        }
        if(!parameterRepository.existsBySwitchParam("/b"))
        {
            parameterRepository.save(new SwitchParameter("/b"));
        }
    }
}
