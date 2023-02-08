package com.fastcampus.snsproject.service;

import com.fastcampus.snsproject.exception.ErrorCode;
import com.fastcampus.snsproject.exception.SnsApplicationException;
import com.fastcampus.snsproject.fixture.UserEntityFixture;
import com.fastcampus.snsproject.model.User;
import com.fastcampus.snsproject.model.entity.UserEntity;
import com.fastcampus.snsproject.repository.UserEntityRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserEntityRepository userEntityRepository;

    @MockBean
    private BCryptPasswordEncoder encoder;

    @Test
    void 회원가입이_정상적으로_동작하는경우(){
        String userName = "username";
        String password = "password";

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(UserEntityFixture.get(userName, password, 1));


        Assertions.assertDoesNotThrow(()->userService.join(userName, password));
    }

    @Test
    void 회원가입시_userName으로_회원가입된_유저가_이미_있는경우(){
        String userName = "username";
        String password = "password";

        //가상의 userEntity 생성
        UserEntity fixture = UserEntityFixture.get(userName, password, 1);


        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        when(encoder.encode(password)).thenReturn("encrypt_password");
        when(userEntityRepository.save(any())).thenReturn(Optional.of(fixture));


        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, ()->userService.join(userName, password));
        Assertions.assertEquals(ErrorCode.DUPLICATED_USER_NAME, e.getErrorCode());
    }

    @Test
    void 로그인이_정상적으로_동작하는경우(){
        String userName = "username";
        String password = "password";

        //가상의 userEntity 생성
        UserEntity fixture = UserEntityFixture.get(userName, password, 1);

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));
        when(encoder.matches(password, fixture.getPassword())).thenReturn(true);


        Assertions.assertDoesNotThrow(()->userService.login(userName, password));
    }

    @Test
    void 로그인시_회원가입된_user가_없는_경우(){
        String userName = "userName";
        String password = "password";


        

        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.empty());



        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, ()->userService.login(userName, password));

        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());

    }

    @Test
    void 로그인시_password가_틀린_경우(){
        String userName = "username";
        String password = "password";
        String wrongPassword = "password2";

        //가상의 userEntity 생성
        UserEntity fixture = UserEntityFixture.get(userName, password, 1);



        //mocking
        when(userEntityRepository.findByUserName(userName)).thenReturn(Optional.of(fixture));




        SnsApplicationException e = Assertions.assertThrows(SnsApplicationException.class, ()->userService.login(userName, wrongPassword));
        Assertions.assertEquals(ErrorCode.INVALID_PASSWORD, e.getErrorCode());
    }
}