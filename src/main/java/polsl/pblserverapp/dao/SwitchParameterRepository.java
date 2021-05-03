package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import polsl.pblserverapp.model.SwitchParameter;

import java.util.List;

@Repository
public interface SwitchParameterRepository extends CrudRepository<SwitchParameter,Long>
{
    List<SwitchParameter> findAll();
    SwitchParameter getById(Long id);
    SwitchParameter getBySwitchParam(String s);
}
