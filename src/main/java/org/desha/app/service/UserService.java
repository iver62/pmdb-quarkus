package org.desha.app.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.desha.app.domain.dto.UserDTO;
import org.desha.app.repository.UserRepository;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class UserService {

    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @WithTransaction
    public Uni<Long> countUsers() {
        return userRepository.count();
    }

    @WithTransaction
    public Uni<List<UserDTO>> getUsers(String sort, Sort.Direction direction, String term) {
        return
                userRepository.findUsers(sort, direction, term)
                        .map(users ->
                                users
                                        .stream()
                                        .map(UserDTO::fromEntity)
                                        .sorted(Comparator.comparing(UserDTO::getUsername))
                                        .toList()
                        )
                ;
    }
}
