package edu.hneu.studentsportal.service;

import edu.hneu.studentsportal.domain.User;
import edu.hneu.studentsportal.enums.UserRole;
import edu.hneu.studentsportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;

@Log4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
        log.info(format("User[%s] was saved", user));
    }

    public User getUserForId(String id) {
        return userRepository.findOne(id);
    }

    public List<User> getAdmins() {
        return userRepository.findAllByRole(UserRole.ADMIN);
    }

    public User create(String id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        save(user);
        return user;
    }

    public void delete(String email) {
        userRepository.delete(email);
    }
}
