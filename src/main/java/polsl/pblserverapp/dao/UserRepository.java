package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import polsl.pblserverapp.model.User;

@Repository
public interface UserRepository extends CrudRepository<User,Long>
{
    User findByUsername(String s);
}
