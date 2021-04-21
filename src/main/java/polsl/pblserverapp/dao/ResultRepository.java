package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import polsl.pblserverapp.model.Result;

import java.util.List;

public interface ResultRepository extends CrudRepository<Result, Long>
{
    List<Result> findAll();
    List<Result> findByResultStatus(String s);
}
