package polsl.pblserverapp.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import polsl.pblserverapp.dao.SwitchParameterRepository;
import polsl.pblserverapp.model.SwitchParameter;

@Service
public class BuildParametersService
{
    private SwitchParameterRepository parameterRepository;

    public BuildParametersService(SwitchParameterRepository parameterRepository)
    {
        this.parameterRepository = parameterRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void insertParameters()
    {
        parameterRepository.save(new SwitchParameter("/a"));
        parameterRepository.save(new SwitchParameter("/b"));
        parameterRepository.save(new SwitchParameter("/c"));
        parameterRepository.save(new SwitchParameter("/d"));
    }
}
