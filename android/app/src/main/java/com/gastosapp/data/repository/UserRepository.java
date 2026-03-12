package com.gastosapp.data.repository;

import com.gastosapp.data.local.UserDao;
import com.gastosapp.data.local.UserEntity;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Singleton
public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executorService;

    @Inject
    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public Future<UserEntity> login(String email, String password) {
        return executorService.submit(() -> userDao.login(email, password));
    }

    public Future<Boolean> register(UserEntity user) {
        return executorService.submit(() -> {
            if (userDao.getUserByEmail(user.getEmail()) != null) {
                return false;
            }
            userDao.insert(user);
            return true;
        });
    }

    public Future<UserEntity> getUserByEmail(String email) {
        return executorService.submit(() -> userDao.getUserByEmail(email));
    }
}
