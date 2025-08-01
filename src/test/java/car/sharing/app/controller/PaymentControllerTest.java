package car.sharing.app.controller;

import static car.sharing.app.util.PaymentUtil.createPaymentDto1;
import static car.sharing.app.util.PaymentUtil.createPaymentDto2;
import static car.sharing.app.util.PaymentUtil.createPaymentRequestDto;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import car.sharing.app.config.WithMockCustomUser;
import car.sharing.app.dto.payment.CreatePaymentRequestDto;
import car.sharing.app.dto.payment.PaymentDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.util.List;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {
        "classpath:database/delete-payments.sql",
        "classpath:database/delete-rentals.sql",
        "classpath:database/delete-cars.sql",
        "classpath:database/delete-users-and-roles.sql",
        "classpath:database/insert-users-and-roles.sql",
        "classpath:database/insert-cars.sql",
        "classpath:database/insert-rentals.sql",
        "classpath:database/insert-payments.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {
        "classpath:database/delete-payments.sql",
        "classpath:database/delete-rentals.sql",
        "classpath:database/delete-cars.sql",
        "classpath:database/delete-users-and-roles.sql"
},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PaymentControllerTest {

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
                    new ClassPathResource("database/delete-payments.sql"));
        }
    }

    @WithMockCustomUser(id = 1L, username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Create payment with valid data")
    void createPayment_ValidRequest_ReturnsPaymentDto() throws Exception {
        CreatePaymentRequestDto request = createPaymentRequestDto();
        PaymentDto expected = createPaymentDto2(4L);

        MvcResult result = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        PaymentDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                PaymentDto.class);
        System.out.println("expected: " + expected);
        System.out.println("actual: " + actual);
        assertTrue(reflectionEquals(expected, actual, "id", "sessionUrl", "sessionId",
                "amountToPay"));
    }

    @WithMockCustomUser(id = 1L, username = "customer", roles = {"CUSTOMER"})
    @Test
    @DisplayName("Get all payments for current user")
    void getPayments_ValidRequest_ReturnsUserPayments() throws Exception {
        List<PaymentDto> expected = List.of(createPaymentDto1(1L));

        MvcResult result = mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<PaymentDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                new TypeReference<>() {
                }
        );
        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .ignoringFields("id", "amountToPay")
                .isEqualTo(expected.get(0));
    }

    @Test
    @DisplayName("Handle Stripe cancel")
    void handleStripeCancel_ReturnsMessage() throws Exception {
        MvcResult result = mockMvc.perform(get("/payments/cancel"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("Оплата була скасована"));
    }
}
