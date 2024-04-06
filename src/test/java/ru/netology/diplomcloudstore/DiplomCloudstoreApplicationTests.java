package ru.netology.diplomcloudstore;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.netology.diplomcloudstore.domain.FileEntityResponse;
import ru.netology.diplomcloudstore.domain.LoginResponse;
import ru.netology.diplomcloudstore.dto.FileDataDto;
import ru.netology.diplomcloudstore.dto.LoginUserDto;
import ru.netology.diplomcloudstore.entities.FileEntity;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.repositories.FileEntityRepository;
import ru.netology.diplomcloudstore.repositories.UserRepository;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class DiplomCloudstoreApplicationTests {

    static final Logger LOGGER = LoggerFactory.getLogger(DiplomCloudstoreApplicationTests.class);

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withExposedPorts(5432)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .withDatabaseName("javamicro")
            .withUsername("postgres")
            .withPassword("mysecretpassword")
            .withInitScript("db/create_scheme.sql")
            .withReuse(true)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(5433), new ExposedPort(5432)))
            ));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5433/javamicro?loggerLevel=ON");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "mysecretpassword");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FileEntityRepository fileEntityRepository;

    @BeforeEach
    public void before(){
        fileEntityRepository.deleteAll();
    }
    @Test
    void controlerMethod_CheckLogin_ReturnsSuccessWithJwtOrBadResponce() throws Exception {
        //prerequisites
        String payloadTrue = "{\"login\":\"testing@mail.ru\", \"password\":\"123\"}";
        String payloadFalse = "{\"login\":\"testing2@mail.ru\", \"password\":\"123\"}";

        ObjectMapper objectMapper = new ObjectMapper();
        LoginUserDto loginUserDtoTrue = objectMapper.readValue(payloadTrue, LoginUserDto.class);
        LoginUserDto loginUserDtoFalse = objectMapper.readValue(payloadFalse, LoginUserDto.class);


        //Check user in database
        Optional<UserEntity> userEntityTrue = userRepository.findByUsername(loginUserDtoTrue.getUsername());
        Assertions.assertTrue(userEntityTrue.isPresent());

        Optional<UserEntity> userEntityFalse = userRepository.findByUsername(loginUserDtoFalse.getUsername());
        Assertions.assertFalse(userEntityFalse.isPresent());

        //Check the success status of Login method

        MockHttpServletResponse responsePostSuccess = mockMvc.perform(
                        post("http://localhost:8081/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadTrue)
                )
                .andReturn()
                .getResponse();

        LoginResponse loginResponse = objectMapper.readValue(responsePostSuccess.getContentAsString(), LoginResponse.class);
        //Check Jwt-token
        Assertions.assertNotNull(loginResponse.getToken());
        Assertions.assertTrue(() -> {
            return loginResponse.getToken().length() > 0;
        });

        //Check Success status
        Assertions.assertEquals(responsePostSuccess.getStatus(), 200);

        //Check the bad status of Login method. User not found
        MockHttpServletResponse responsePostDeny = mockMvc.perform(
                        post("http://localhost:8081/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadFalse)
                )
                .andReturn()
                .getResponse();

        Assertions.assertEquals(responsePostDeny.getStatus(), 401);
    }

    @Test
    @Transactional
    void controlerMethod_CheckGetListFilesByLimit_ReturnTrueIfEquals() throws Exception {
        //prerequisites
        String payloadTrue = "{\"login\":\"testing@mail.ru\", \"password\":\"123\"}";
        String prefixAuthHeader = "Bearer";
        int expected = 1;

        ObjectMapper objectMapper = new ObjectMapper();
        LoginUserDto loginUserDtoTrue = objectMapper.readValue(payloadTrue, LoginUserDto.class);

        //prepare 1 file. Check save row to database
        Optional<UserEntity> userEntity = userRepository.findByUsername(loginUserDtoTrue.getUsername());
        Assertions.assertTrue(userEntity.isPresent());

        //test
        MockHttpServletResponse responsePostLoginSuccess = mockMvc.perform(
                        post("http://localhost:8081/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadTrue)
                )
                .andReturn()
                .getResponse();

        System.out.println("responsePostLoginSuccess.getContentAsString() =" + responsePostLoginSuccess.getContentAsString());
        LoginResponse loginResponse = objectMapper.readValue(responsePostLoginSuccess.getContentAsString(), LoginResponse.class);
        String jwt = loginResponse.getToken();

        FileEntity fileEntity = FileEntity.builder()
                .user(userEntity.get())
                .fullPath("C:\\test\\test.txt")
                .fileName("test.txt")
                .size(10L)
                .deleted(false)
                .createdAt(new Date())
                .broken(false)
                .extention("txt")
                .build();
        fileEntityRepository.save(fileEntity);

        List<FileEntity> listFiles = fileEntityRepository.findAllByUserAndNotDeletedWithLimit(userEntity.get().getUsername(), false, 3);
        Assertions.assertEquals(expected, listFiles.size());
        System.out.println("fileEntityRepository.findAllByUserAndNotDeletedWithLimit() =" + listFiles.size());

        MockHttpServletResponse responseGetListSuccess = mockMvc.perform(
                        get("http://localhost:8081/cloud/list?limit=3")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Auth-Token", prefixAuthHeader + " " + jwt)
                )
                .andReturn()
                .getResponse();

        System.out.println("responseGetListSuccess.getContentAsString() =" + responseGetListSuccess.getContentAsString());
        List<FileEntityResponse> list = objectMapper.readValue(
                responseGetListSuccess.getContentAsString(),
                new TypeReference<List<FileEntityResponse>>() {
                });

        Assertions.assertEquals(expected, list.size());
    }

    @Test
    void controlerMethod_CheckAddFile_ReturnTrueIfEquals() throws Exception {
        //prerequisites
        byte[] fileToUpload = "file for container testing".getBytes();
        String payloadTrue = "{\"login\":\"testing@mail.ru\", \"password\":\"123\"}";
        String prefixAuthHeader = "Bearer";

        ObjectMapper objectMapper = new ObjectMapper();
        LoginUserDto loginUserDtoTrue = objectMapper.readValue(payloadTrue, LoginUserDto.class);

        //test
        MockHttpServletResponse responsePostLoginSuccess = mockMvc.perform(
                        post("http://localhost:8081/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadTrue)
                )
                .andReturn()
                .getResponse();

        System.out.println("responsePostSuccess.getContentAsString() =" + responsePostLoginSuccess.getContentAsString());
        LoginResponse loginResponse = objectMapper.readValue(responsePostLoginSuccess.getContentAsString(), LoginResponse.class);
        String jwt = loginResponse.getToken();

        MockMultipartFile fileMultiPart = new MockMultipartFile("file", "testFile.txt", "multipart/form-data; boundary=----", fileToUpload);
        MockHttpServletResponse responsePostFileSuccess = mockMvc.perform(multipart("http://localhost:8081/cloud/file")
                        .file(fileMultiPart)
                        .header("Auth-Token", prefixAuthHeader + " " + jwt)
                        .param("filename", "testFile.txt")
                )
                .andReturn()
                .getResponse();

        System.out.println("responsePostFileSuccess.getContentAsString() =" + responsePostFileSuccess.getContentAsString());
        Assertions.assertEquals(HttpStatus.OK.value(), responsePostFileSuccess.getStatus());

        Optional<FileEntity> fileEntity = fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(loginUserDtoTrue.getUsername(), "testFile.txt", false);
        Assertions.assertTrue(fileEntity.isPresent());
    }

    @Test
    @Transactional
    void controlerMethod_CheckGetFile_ReturnTrueIfEquals() throws Exception {

        //prerequisites
        byte[] fileToUpload = "file for container testing".getBytes();
        String payloadTrue = "{\"login\":\"testing@mail.ru\", \"password\":\"123\"}";
        String prefixAuthHeader = "Bearer";

        ObjectMapper objectMapper = new ObjectMapper();
        LoginUserDto loginUserDtoTrue = objectMapper.readValue(payloadTrue, LoginUserDto.class);

        //prepare 1 file. Check save row to database
        Optional<UserEntity> userEntity = userRepository.findByUsername(loginUserDtoTrue.getUsername());
        Assertions.assertTrue(userEntity.isPresent());

        //test
        //do Login to service
        MockHttpServletResponse responsePostLoginSuccess = mockMvc.perform(
                        post("http://localhost:8081/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadTrue)
                )
                .andReturn()
                .getResponse();

        System.out.println("responsePostLoginSuccess.getContentAsString() =" + responsePostLoginSuccess.getContentAsString());
        LoginResponse loginResponse = objectMapper.readValue(responsePostLoginSuccess.getContentAsString(), LoginResponse.class);
        String jwt = loginResponse.getToken();

        //do save new file
        MockMultipartFile fileMultiPart = new MockMultipartFile("file", "testFile.txt", "multipart/form-data; boundary=----", fileToUpload);
        MockHttpServletResponse responsePostFileSuccess = mockMvc.perform(multipart("http://localhost:8081/cloud/file")
                        .file(fileMultiPart)
                        .header("Auth-Token", prefixAuthHeader + " " + jwt)
                        .param("filename", "testFile.txt")
                )
                .andReturn()
                .getResponse();

        Assertions.assertEquals(HttpStatus.OK.value(), responsePostFileSuccess.getStatus());

        Optional<FileEntity> fileEntity = fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(loginUserDtoTrue.getUsername(), "testFile.txt", false);
        Assertions.assertTrue(fileEntity.isPresent());

        //do load to client last saved file
        MockHttpServletResponse responseGetFileSuccess = mockMvc.perform(
                        get("http://localhost:8081/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Auth-Token", prefixAuthHeader + " " + jwt)
                                .param("filename", "testFile.txt")
                )
                .andReturn()
                .getResponse();

        Assertions.assertTrue(responseGetFileSuccess.getContentAsByteArray().length > 0);
        Assertions.assertEquals(200, responseGetFileSuccess.getStatus());
    }

    @Test
    @Transactional
    void controlerMethod_CheckRenameFile_ReturnTrueIfEquals() throws Exception {
        //prerequisites
        byte[] fileToUpload = "file for container testing".getBytes();
        String payloadTrue = "{\"login\":\"testing@mail.ru\", \"password\":\"123\"}";
        String payloadPut = "{\"filename\":\"koala2.jpg\"}";

        String prefixAuthHeader = "Bearer";
        String expected = "File name to upload";

        ObjectMapper objectMapper = new ObjectMapper();
        LoginUserDto loginUserDtoTrue = null;
        try {
            loginUserDtoTrue = objectMapper.readValue(payloadTrue, LoginUserDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //prepare 1 file. Check save row to database
        Optional<UserEntity> userEntity = userRepository.findByUsername(loginUserDtoTrue.getUsername());
        Assertions.assertTrue(userEntity.isPresent());

        //test
        //do Login to service
        MockHttpServletResponse responsePostLoginSuccess = mockMvc.perform(
                        post("http://localhost:8081/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadTrue)
                )
                .andReturn()
                .getResponse();

        System.out.println("responsePostLoginSuccess.getContentAsString() =" + responsePostLoginSuccess.getContentAsString());
        LoginResponse loginResponse = objectMapper.readValue(responsePostLoginSuccess.getContentAsString(), LoginResponse.class);
        String jwt = loginResponse.getToken();

        //do save new file
        MockMultipartFile fileMultiPart = new MockMultipartFile("file", "testFile.txt", "multipart/form-data; boundary=----", fileToUpload);
        MockHttpServletResponse responsePostFileSuccess = mockMvc.perform(multipart("http://localhost:8081/cloud/file")
                        .file(fileMultiPart)
                        .header("Auth-Token", prefixAuthHeader + " " + jwt)
                        .param("filename", "testFile.txt")
                )
                .andReturn()
                .getResponse();

        Assertions.assertEquals(HttpStatus.OK.value(), responsePostFileSuccess.getStatus());

        Optional<FileEntity> fileEntity = fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(loginUserDtoTrue.getUsername(), "testFile.txt", false);
        Assertions.assertTrue(fileEntity.isPresent());


        //do rename to client last saved file
        MockHttpServletResponse responsePutFileSuccess = mockMvc.perform(
                        put("http://localhost:8081/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Auth-Token", prefixAuthHeader + " " + jwt)
                                .param("filename", "testFile.txt")
                                .content(payloadPut)
                )
                .andReturn()
                .getResponse();

        Assertions.assertEquals(200,responsePutFileSuccess.getStatus());
        Assertions.assertEquals(expected,responsePutFileSuccess.getContentAsString());

        FileDataDto fileDataDto = objectMapper.readValue(payloadPut, FileDataDto.class);
        Optional<FileEntity> newFile = fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(userEntity.get().getUsername(),fileDataDto.getNewName(),false);
        Assertions.assertTrue(newFile.isPresent());

        Optional<FileEntity> oldFile = fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(userEntity.get().getUsername(),fileDataDto.getOldName(),false);
        Assertions.assertFalse(oldFile.isPresent());
    }

    @Test
    @Transactional
    void controlerMethod_CheckDeleteFile_ReturnTrueIfEquals() throws Exception {
        //prerequisites
        byte[] fileToUpload = "file for container testing".getBytes();
        String payloadTrue = "{\"login\":\"testing@mail.ru\", \"password\":\"123\"}";
        String payloadPut = "{\"filename\":\"koala2.jpg\"}";

        String prefixAuthHeader = "Bearer";
        String expected = "Success deleted";

        ObjectMapper objectMapper = new ObjectMapper();
        LoginUserDto loginUserDtoTrue = null;
        try {
            loginUserDtoTrue = objectMapper.readValue(payloadTrue, LoginUserDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //prepare 1 file. Check save row to database
        Optional<UserEntity> userEntity = userRepository.findByUsername(loginUserDtoTrue.getUsername());
        Assertions.assertTrue(userEntity.isPresent());

        //test
        //do Login to service
        MockHttpServletResponse responsePostLoginSuccess = mockMvc.perform(
                        post("http://localhost:8081/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payloadTrue)
                )
                .andReturn()
                .getResponse();

        System.out.println("responsePostLoginSuccess.getContentAsString() =" + responsePostLoginSuccess.getContentAsString());
        LoginResponse loginResponse = objectMapper.readValue(responsePostLoginSuccess.getContentAsString(), LoginResponse.class);
        String jwt = loginResponse.getToken();

        //do save new file
        MockMultipartFile fileMultiPart = new MockMultipartFile("file", "testFile.txt", "multipart/form-data; boundary=----", fileToUpload);
        MockHttpServletResponse responsePostFileSuccess = mockMvc.perform(multipart("http://localhost:8081/cloud/file")
                        .file(fileMultiPart)
                        .header("Auth-Token", prefixAuthHeader + " " + jwt)
                        .param("filename", "testFile.txt")
                )
                .andReturn()
                .getResponse();

        Assertions.assertEquals(HttpStatus.OK.value(), responsePostFileSuccess.getStatus());

        Optional<FileEntity> fileEntity = fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(loginUserDtoTrue.getUsername(), "testFile.txt", false);
        Assertions.assertTrue(fileEntity.isPresent());

        //do delete file

        MockHttpServletResponse responseDeleteFileSuccess = mockMvc.perform(
                        delete("http://localhost:8081/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Auth-Token", prefixAuthHeader + " " + jwt)
                                .param("filename", "testFile.txt")
                                .content(payloadPut)
                )
                .andReturn()
                .getResponse();


        Assertions.assertEquals(200,responseDeleteFileSuccess.getStatus());
        Assertions.assertEquals(expected,responseDeleteFileSuccess.getContentAsString());

        Optional<FileEntity> newFile = fileEntityRepository.findFirstByFileNameAndUserAndDeletedNot(userEntity.get().getUsername(),"testFile.txt",false);
        Assertions.assertFalse(newFile.isPresent());
    }
}
