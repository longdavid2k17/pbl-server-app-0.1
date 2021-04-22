package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import polsl.pblserverapp.model.SwitchParameter;

@Repository
public interface SwitchParameterRepository extends CrudRepository<SwitchParameter,Long>
{

}
