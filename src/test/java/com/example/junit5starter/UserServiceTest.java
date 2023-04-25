package com.example.junit5starter;

import com.example.junit5starter.models.User;
import com.example.junit5starter.paramresolver.UserServiceParamResolver;
import com.example.junit5starter.services.UserService;
import net.bytebuddy.implementation.bind.annotation.Empty;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({UserServiceParamResolver.class})
//@TestMethodOrder(MethodOrderer.MethodName.class) // лучше не использовать
class UserServiceTest {
    private UserService userService;

    private static final User BOB = User.of(1, "Bob", "123");
    private static final User STEVEN = User.of(2, "Steven", "111");

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare(UserService userService) {
        this.userService = userService;
        System.out.println("Before each: " + this);
    }

    @Test
    @Order(value = 1) // MethodOrderer.OrderAnnotation.class
    @DisplayName(value = "test display name")
        // MethodOrderer.DisplayName.class
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();

        MatcherAssert.assertThat(users, empty()); // hamcrest
//        assertTrue(users.isEmpty(), "User list should be empty!");
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(BOB);
        userService.add(STEVEN);

        List<User> users = userService.getAll();

        assertThat(users).hasSize(2); // assertJ
//        assertEquals(2, users.size());
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(BOB, STEVEN);

        Map<Integer, User> map = userService.getAllConvertedById();

        MatcherAssert.assertThat(map, hasKey(BOB.getId())); // hamcrest

        assertAll(
                () -> assertThat(map).containsKeys(BOB.getId(), STEVEN.getId()), // assertJ
                () -> assertThat(map).containsValues(BOB, STEVEN) // assertJ
        );
    }

    @AfterEach
    void deleteDateFromDataBase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }

    @Nested
    @DisplayName("test user login functionality")
    @Tag("login")
    class LoginTest {

        @Test
        void loginSuccessIfUserExist() {
            userService.add(BOB);
            Optional<User> maybeUser = userService.login(BOB.getUsername(), BOB.getPassword());

            assertThat(maybeUser).isPresent(); // assertJ
//        assertTrue(maybeUser.isPresent());

            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(BOB)); // assertJ
//        maybeUser.ifPresent(user -> assertEquals(BOB, user));
        }

        //        @org.junit.Test(expected = IllegalArgumentException.class) // До JUnit5
        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
            assertAll(
                    () -> {
                        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                () -> userService.login(null, "dummy"));

                        assertThat(e.getMessage()).isEqualTo("Username or password is null!");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @Test
        void logicFailIfPasswordIsNotCorrect() {
            userService.add(BOB);
            Optional<User> maybeUser = userService.login(BOB.getUsername(), "dummy");
            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void logicFailIfUserDoesNotExist() {
            userService.add(BOB);
            Optional<User> maybeUser = userService.login("dummy", BOB.getPassword());
            assertTrue(maybeUser.isEmpty());
        }

        @ParameterizedTest(name = "{arguments} test")
//        @NullSource
//        @EmptySource
//        @ValueSource(strings = {"Bob", "Steven"})
//        @NullAndEmptySource
//        @MethodSource("getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/loginTestData.csv", delimiter = ',', numLinesToSkip = 1)
        @CsvSource({ // аналог @CsvFileSource, только не нужно создавать отдельный файл
                "Bob, 123",
                "Steven, 111"
        })
        void loginParametrizedTest(String username, String password) {
            userService.add(BOB, STEVEN);
            Optional<User> maybeUser = userService.login(username, password);
        }

        static Stream<Arguments> getArgumentsForLoginTest() {
            return Stream.of(
                    Arguments.of("Bob", "123"),
                    Arguments.of("Steven", "111"),
                    Arguments.of("Bob", "dummy"),
                    Arguments.of("dummy", "123")
            );
        }

    }
}
