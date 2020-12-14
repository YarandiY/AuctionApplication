package com.rahnemacollege.test;

import com.rahnemacollege.domain.AddUserDomain;
import com.rahnemacollege.domain.AuthenticationResponse;
import com.rahnemacollege.domain.SimpleUserDomain;
import com.rahnemacollege.domain.UserDomain;
import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.UserRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class PasswordFeaturesTest extends InitTest {
    private final String SIGNUP = "/users/signup";
    private final String FORGOT = "/users/forgot";
    private final String RESET = "/users/reset";
    private final String CHANGE_PASSWORD = "/users/edit/password";
    private final String changedPassword = "WeAreFiveGears";
    private final String initPassword = "fiveGears";
    private String myAuth = null;
    private SimpleUserDomain userDomain;
    @Autowired
    private UserRepository userRepository;

    @Before
    public void signUpNewUser() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders.post(SIGNUP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAddUserDomain())
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        userDomain = gson.fromJson(response, SimpleUserDomain.class);
    }

    @Test
    public void forgettingTest() {
        try {
            //forget
            SimpleUserDomain simpleUserDomain = forgotPassword();
            AssertionsForClassTypes.assertThat(simpleUserDomain.getName())
                    .isEqualTo(userDomain.getName());
            AssertionsForClassTypes.assertThat(simpleUserDomain.getEmail())
                    .isEqualTo(userDomain.getEmail());
            //read Email
            Thread.sleep(5000);
            String token = readEmail();
            assertThat(token).isNotNull();
            System.err.println(token);
            simpleUserDomain = resetPass(token);
            AssertionsForClassTypes.assertThat(simpleUserDomain.getName())
                    .isEqualTo(userDomain.getName());
            AssertionsForClassTypes.assertThat(simpleUserDomain.getEmail())
                    .isEqualTo(userDomain.getEmail());
            myAuth = myLogin(false);
            UserDomain userDomain1 = getMyUserInfo();
            AssertionsForClassTypes.assertThat(userDomain1.getName())
                    .isEqualTo(userDomain.getName());
            AssertionsForClassTypes.assertThat(userDomain1.getEmail())
                    .isEqualTo(userDomain.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void changePassword() throws Exception {
        MultiValueMap<String, String> params = new HttpHeaders();
        if (myAuth != null) {
            params.add("oPassword", changedPassword);
            params.add("nPassword", initPassword);
        }
        else {
            myAuth = myLogin(true);
            params.add("oPassword", initPassword);
            params.add("nPassword", changedPassword);
        }
        String response = mvc.perform(MockMvcRequestBuilders.post(CHANGE_PASSWORD)
                .header("auth", myAuth)
                .params(params))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        SimpleUserDomain simpleUserDomain = gson.fromJson(response, SimpleUserDomain.class);
        AssertionsForClassTypes.assertThat(simpleUserDomain.getName())
                .isEqualTo(userDomain.getName());
        AssertionsForClassTypes.assertThat(simpleUserDomain.getEmail())
                .isEqualTo(userDomain.getEmail());
    }

    private String myLogin(boolean onInitPassword) throws Exception {
        user = new User();
        user.setEmail(userDomain.getEmail());
        if (!onInitPassword)
            user.setPassword(changedPassword);
        else
            user.setPassword(initPassword);
        String response = mvc.perform(MockMvcRequestBuilders.post(LOGIN)
                .contentType(MediaType.APPLICATION_JSON).content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return "Bearer " + gson.fromJson(response, AuthenticationResponse.class).getToken();
    }


    private SimpleUserDomain forgotPassword() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders.post(FORGOT)
                .param("email", userDomain.getEmail())
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return gson.fromJson(response, SimpleUserDomain.class);
    }

    private String readEmail() {
        String protocol = "imap";
        String host = "imap.gmail.com";
        String port = "993";


        String userName = "fivegears.rahnema@gmail.com";
        String password = "yynqarjhpaatinjx";

        EmailReceiver receiver = new EmailReceiver();
        return receiver.getLastToken(protocol, host, port, userName, password);
    }

    private SimpleUserDomain resetPass(String token) throws Exception {
        MultiValueMap<String, String> params = new HttpHeaders();
        params.add("token", token);
        params.add("validPassword", changedPassword);
        String response = mvc.perform(MockMvcRequestBuilders.post(RESET)
                .params(params)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return gson.fromJson(response, SimpleUserDomain.class);
    }


    private String createAddUserDomain() {
        AddUserDomain userDomain = new AddUserDomain();
        userDomain.setName("تستر رهنماکالج");
        userDomain.setEmail("fivegears.rahnema@gmail.com");
        userDomain.setPassword(initPassword);
        return gson.toJson(userDomain);
    }

    private UserDomain getMyUserInfo() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders.get(ME).header("auth", myAuth))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return gson.fromJson(response, UserDomain.class);
    }

    @After
    public void deleteUser(){
        if (userDomain!= null){
            userRepository.deleteByEmail(userDomain.getEmail());
        }
    }
}
