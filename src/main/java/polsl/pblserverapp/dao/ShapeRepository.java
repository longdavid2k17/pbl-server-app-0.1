package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import polsl.pblserverapp.model.Shape;

@Repository
public interface ShapeRepository extends CrudRepository<Shape, Long>
{

}
