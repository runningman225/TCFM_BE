package com.future.tcfm.service.impl;

import com.future.tcfm.model.Group;
import com.future.tcfm.model.User;
import com.future.tcfm.repository.GroupRepository;
import com.future.tcfm.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.mail.MessagingException;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

//@RunWith(MockitoJUnitRunner.Silent.class)
@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private WebApplicationContext wac;

    @InjectMocks
    UserServiceImpl userService;

    private User user;
    private Group group;

    @Test
    public void loadAll() {
        List<User> users = Arrays.asList(user,user,user);

        List<User> found =userRepository.findAll();

        assertThat(found.get(0).getName()).isEqualTo("Admin");

     /*   Mockito.when(userRepository.findAll()).thenReturn(users);

        // Method call
        List<User> userList= userService.loadAll();

        // Verification
        Assert.assertThat(userList, Matchers.hasSize(3));
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Mockito.verifyNoMoreInteractions(userRepository);*/
    }

    @Test
    public void getUserByEmail() {
        User user = new User();
        user.setEmail("nancy@gdn.com");

        doReturn(user).when(userRepository).findByEmail(this.user.getEmail());
        User result = userService.getUser(user.getEmail());

        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(user.getEmail());
        Assert.assertEquals(result, user);
    }

    @Test
    public void manageUser() throws MessagingException {
        user=  new User();
        user.setGroupName("Test");
        user.setActive(true);
        user.setIdUser("ID");

        group = new Group();
        group.setName("Test");
        group.setActive(true);

        when(userRepository.findByIdUser(user.getIdUser())).thenReturn(user);
        when(groupRepository.findByNameAndActive(user.getGroupName(),true)).thenReturn(group);

        userService.manageUser(user.getIdUser(),user,"");
    }

    @Test
    public void getImage() {}

    @Test
    public void saveUploadedFile() {}

    @Test
    public void checkImageFile() {}

    @Test
    public void deleteUser() throws MessagingException {
        user=new User();
        user.setIdUser("ID");

        ResponseEntity e = userService.deleteUser("asd");
    }


}
