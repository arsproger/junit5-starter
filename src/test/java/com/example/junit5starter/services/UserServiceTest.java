package com.example.junit5starter.services;

import com.example.junit5starter.TestBase;
import com.example.junit5starter.dao.UserDao;
import com.example.junit5starter.extension.*;
import com.example.junit5starter.models.User;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(
        {UserServiceParamResolver.class,
                PostProcessingExtension.class,
                ConditionalExtension.class,
//                ThrowableExtension.class
//                GlobalExtension.class
        }
)
//@TestMethodOrder(MethodOrderer.MethodName.class) // лучше не использовать
public class UserServiceTest extends TestBase {
    private UserService userService;
    private UserDao userDao;

    private static final User BOB = User.of(1, "Bob", "123");
    private static final User STEVEN = User.of(2, "Steven", "111");

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        this.userDao = Mockito.mock(UserDao.class);
        this.userService = new UserService(userDao);
        System.out.println("Before each: " + this);
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(BOB);
        Mockito.doReturn(true).when(userDao).deleteById(BOB.getId()); // является предпочтительным (универсальный)
//        Mockito.doReturn(true).when(userDao).deleteById(Mockito.any()); // dummy
//        Mockito.when(userDao.deleteById(BOB.getId()))
//                .thenReturn(true).thenReturn(false); // последовательное возвращение
        Boolean res = userService.deleteUser(BOB.getId());
        assertThat(res).isTrue();
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
            if (true) throw new RuntimeException(); // Exception handling ( Extension model )

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
        @Disabled("flaky, need to see")
        void logicFailIfPasswordIsNotCorrect() {
            userService.add(BOB);
            Optional<User> maybeUser = userService.login(BOB.getUsername(), "dummy");
            assertTrue(maybeUser.isEmpty());
        }

        //        @Test
        @RepeatedTest(5)
        void logicFailIfUserDoesNotExist() {
            userService.add(BOB);
            Optional<User> maybeUser = userService.login("dummy", BOB.getPassword());
            assertTrue(maybeUser.isEmpty());
        }

        @Test
//        @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
        void checkLoginFunctionalityPerformance() {
            assertTimeout(Duration.ofMillis(200L),
                    () -> userService.login("dummy", BOB.getPassword()));
//            assertTimeoutPreemptively(); // метод выполняется в отдельном потоке
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
