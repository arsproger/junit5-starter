package com.example.junit5starter.services;

import com.example.junit5starter.dao.UserDao;
import com.example.junit5starter.models.User;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class UserService {
    private final UserDao userDao;
    private final List<User> users = new ArrayList<>();

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Boolean deleteUser(Integer id) {
        return userDao.deleteById(id);
    }

    public List<User> getAll() {
        return users;
    }

    public void add(User... users) {
        this.users.addAll(Arrays.asList(users));
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null)
            throw new IllegalArgumentException("Username or password is null!");

        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .filter(user -> user.getPassword().equals(password))
                .findFirst();
    }

    public Map<Integer, User> getAllConvertedById() {
        return users.stream().collect(Collectors.toMap(User::getId, identity()));
    }

}
