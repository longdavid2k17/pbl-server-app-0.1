package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import polsl.pblserverapp.model.Shape;

import java.util.List;

@Repository
public interface ShapeRepository extends CrudRepository<Shape, Long>
{
    List<Shape> findAll();
    List<Shape> findShapesByParametersListContains(Long parameterId);
}
