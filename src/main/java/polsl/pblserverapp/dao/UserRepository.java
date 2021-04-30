package polsl.pblserverapp.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import polsl.pblserverapp.model.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User,Long>
{
    User findByUsername(String s);
    User findByUserId(Long id);
    List<User> findAllByRole(String s);
    boolean existsUserByUsername(String s);
    int deleteByUserId(long id);
}
