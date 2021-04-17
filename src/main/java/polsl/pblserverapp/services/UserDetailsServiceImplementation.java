package polsl.pblserverapp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import polsl.pblserverapp.dao.UserRepository;
import polsl.pblserverapp.model.MyUserPrincipal;
import polsl.pblserverapp.model.User;

public class UserDetailsServiceImplementation implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(s);
        if(user==null)
        {
            throw new UsernameNotFoundException(s);
        }
        return new MyUserPrincipal(user);
    }
}
