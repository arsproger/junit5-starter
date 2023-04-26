package com.example.junit5starter.extension;

import com.example.junit5starter.services.UserService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static org.junit.jupiter.api.extension.ExtensionContext.*;

public class UserServiceParamResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == UserService.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Store store = extensionContext.getStore(Namespace.create(UserService.class));

//        Store store = extensionContext.getStore(
//        Namespace.create(extensionContext.getTestMethod())); // для каждого метода новый объект
        return store.getOrComputeIfAbsent(UserService.class, it -> new UserService(null));
    }

}
