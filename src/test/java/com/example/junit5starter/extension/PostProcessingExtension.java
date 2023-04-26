package com.example.junit5starter.extension;

import com.example.junit5starter.services.UserService;
import lombok.Getter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;

public class PostProcessingExtension implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(Object o, ExtensionContext extensionContext) throws Exception {
        System.out.println("Post processing extension");
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if(field.isAnnotationPresent(Getter.class)) {
                field.set(o, new UserService(null));
            }
        }
    }

}
