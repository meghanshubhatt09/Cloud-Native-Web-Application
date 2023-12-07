package com.webservices.cloudwebapp.webapp;

import com.webservices.cloudwebapp.webapp.restController.UserController;
import com.webservices.cloudwebapp.webapp.model.User;
import com.webservices.cloudwebapp.webapp.repository.UserRepository;
import com.webservices.cloudwebapp.webapp.service.UserService;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;





import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class UserControllerTests {
    
    @MockBean
    UserService userServicesTest;

    @MockBean
    UserRepository userRepositoryTest;

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private static User USER;

    String requestBody = """
                   {
                           "username": "Xyzgmail.com",
                           "password": "web",
                           "first_name": "cloud",
                           "last_name": "web"
                   }
""";

    @Test
    public void createUserTest() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Mockito.when(userServicesTest.registerUser(Mockito.any(User.class)))
                .thenReturn(USER);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/account")
                .accept(MediaType.APPLICATION_JSON).content(requestBody)
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse response = result.getResponse();
        // response.getStatus()
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }
}
