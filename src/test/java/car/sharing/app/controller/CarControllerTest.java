package car.sharing.app.controller;

import static car.sharing.app.util.CarUtil.createCarBadRequestDto;
import static car.sharing.app.util.CarUtil.createCarDto;
import static car.sharing.app.util.CarUtil.createCarRequestDto;
import static car.sharing.app.util.CarUtil.createListOfCars;
import static car.sharing.app.util.CarUtil.createUpdatedCarDto;
import static car.sharing.app.util.CarUtil.createUpdatedCarRequestDto;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.app.dto.car.CarDto;
import car.sharing.app.dto.car.CreateCarRequestDto;
import car.sharing.app.model.Car;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext,
            @Autowired DataSource dataSource
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @BeforeEach
    void beforeEach(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/insert-cars.sql")
            );
        }
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/delete-cars.sql")
            );
        }
    }

    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Get all cars with pagination and compare content")
    void findAll_WithValidPagination_ReturnsPagedCars() throws Exception {
        List<Car> cars = createListOfCars();
        List<CarDto> expected = cars.stream()
                .map(car -> new CarDto()
                        .setId(car.getId())
                        .setBrand(car.getBrand())
                        .setModel(car.getModel())
                        .setTypeName(car.getTypeName())
                        .setInventory(car.getInventory())
                        .setDailyFee(car.getDailyFee()))
                .toList();

        MvcResult result = mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        JsonNode contentNode = root.get("content");
        List<CarDto> actual = objectMapper.readValue(
                contentNode.toString(),
                new TypeReference<>() {
                }
        );

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Get car by id and validate content")
    void findById_WithValidId_ReturnsCarDto() throws Exception {
        CarDto expected = createCarDto(1L);
        MvcResult result = mockMvc.perform(get("/cars/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDto.class);
        assertTrue(reflectionEquals(expected, actual, "dailyFee"));
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Create a new car")
    void save_WithValidRequestDto_ReturnsCreatedCar() throws Exception {
        CreateCarRequestDto requestDto = createCarRequestDto();
        CarDto expected = createCarDto(1L);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDto.class);

        assertTrue(reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Update car by id")
    void update_WithValidIdAndDto_ReturnsUpdatedCar() throws Exception {
        CreateCarRequestDto carRequestDto = createUpdatedCarRequestDto();
        CarDto expected = createUpdatedCarDto(1L);
        String jsonRequest = objectMapper.writeValueAsString(carRequestDto);

        MvcResult result = mockMvc.perform(put("/cars/{id}", 1)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CarDto.class);
        assertTrue(reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Delete car by id")
    void delete_WithValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/cars/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Get car by non-existing id")
    void findById_WithInvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/cars/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Update car by non-existing id")
    void update_WithInvalidId_ReturnsNotFound() throws Exception {
        CreateCarRequestDto requestDto = createCarRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(put("/cars/{id}", 999)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Delete car with invalid id")
    void delete_WithInvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/cars/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Create car with invalid data")
    void save_WithInvalidRequestDto_ReturnsBadRequest() throws Exception {
        CreateCarRequestDto invalidRequest = createCarBadRequestDto(1L);

        String jsonRequest = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
