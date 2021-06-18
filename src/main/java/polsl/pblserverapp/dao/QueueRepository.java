package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import polsl.pblserverapp.model.Queue;

import java.util.Optional;

@Repository
public interface QueueRepository extends CrudRepository<Queue,Long>
{
    boolean existsByName(String name);
    Optional<Queue> getByName(String name);
}
