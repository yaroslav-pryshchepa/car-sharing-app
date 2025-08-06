package car.sharing.app.controller;

import static car.sharing.app.util.RentalUtil.createBadRentalRequestDto;
import static car.sharing.app.util.RentalUtil.createRentalDto;
import static car.sharing.app.util.RentalUtil.createRentalRequestDto;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.app.config.WithMockCustomUser;
import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Sql(scripts = {
        "classpath:database/delete-rentals.sql",
        "classpath:database/delete-cars.sql",
        "classpath:database/delete-users-and-roles.sql",
        "classpath:database/insert-users-and-roles.sql",
        "classpath:database/insert-cars.sql",
        "classpath:database/insert-rentals.sql",
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/delete-rentals.sql",
        "classpath:database/delete-cars.sql",
        "classpath:database/delete-users-and-roles.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RentalControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext context,
            @Autowired DataSource dataSource) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/delete-rentals.sql"));
        }
    }

    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Create rental with valid request")
    void createRental_ValidRequest_ReturnsCreatedRental() throws Exception {
        CreateRentalRequestDto requestDto = createRentalRequestDto();
        RentalDto expected = createRentalDto(1L);

        MvcResult result = mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        RentalDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalDto.class);

        assertTrue(reflectionEquals(expected, actual, "id", "rentalDate", "actualReturnDate"));
    }

    @WithMockCustomUser(id = 1L, username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Get rental by valid ID")
    void getRentalById_ValidId_ReturnsRentalDto() throws Exception {
        RentalDto expected = createRentalDto(1L);

        MvcResult result = mockMvc.perform(get("/rentals/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalDto.class);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCarModel(), actual.getCarModel());
    }

    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Return rental by ID")
    void returnRental_ValidId_ReturnsRentalDto() throws Exception {
        RentalDto expected = createRentalDto(1L);
        MvcResult result = mockMvc.perform(post("/rentals/{id}/return", 1L))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                RentalDto.class);
        assertTrue(reflectionEquals(expected, actual, "id", "actualReturnDate"));
    }

    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Create rental with invalid data")
    void createRental_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateRentalRequestDto badRequest = createBadRentalRequestDto();

        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @WithMockCustomUser(id = 1L, username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Get rental with invalid ID returns NotFound")
    void getRentalById_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/rentals/{id}", 100L))
                .andExpect(status().isNotFound());
    }
}
