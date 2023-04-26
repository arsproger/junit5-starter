package com.example.junit5starter.dao;

import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

public class UserDaoMock extends UserDao {
    private Map<Integer, Boolean> answers = new HashMap<>();
    private Answer1<Integer, Boolean> answer1;

    @Override
    public Boolean deleteById(Integer id) {
        return answers.getOrDefault(id, false);
    }

}
