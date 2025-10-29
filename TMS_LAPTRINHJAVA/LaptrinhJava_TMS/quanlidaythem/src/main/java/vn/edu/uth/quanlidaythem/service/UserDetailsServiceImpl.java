package vn.edu.uth.quanlidaythem.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.uth.quanlidaythem.domain.User;
import vn.edu.uth.quanlidaythem.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User: " + username));
            
        System.out.println("\n\n!!! [DEBUG-1] Đã tìm thấy user: " + user.getUsername() + ", Role đọc từ DB là: '" + user.getRole() + "'\n\n");
        
        return UserDetailsImpl.build(user); 
    }
}