package car.sharing.app.util;

import car.sharing.app.dto.payment.CreatePaymentRequestDto;
import car.sharing.app.dto.payment.PaymentDto;
import car.sharing.app.model.PaymentStatus;
import car.sharing.app.model.PaymentType;
import java.math.BigDecimal;
import java.util.List;

public class PaymentUtil {

    public static PaymentDto createPaymentDto1(Long id) {
        return new PaymentDto()
                .setId(id)
                .setStatus(PaymentStatus.PAID)
                .setPaymentType(PaymentType.PAYMENT)
                .setRentalId(1L)
                .setAmountToPay(BigDecimal.valueOf(400.00))
                .setSessionUrl("http://example.com/success1")
                .setSessionId("sess_1");
    }

    public static PaymentDto createPaymentDto2(Long id) {
        return new PaymentDto()
                .setId(id)
                .setStatus(PaymentStatus.PENDING)
                .setPaymentType(PaymentType.PAYMENT)
                .setRentalId(2L)
                .setAmountToPay(BigDecimal.valueOf(240.00))
                .setSessionUrl("http://example.com/success1")
                .setSessionId("sess_1");
    }

    public static CreatePaymentRequestDto createPaymentRequestDto() {
        return new CreatePaymentRequestDto()
                .setRentalId(2L)
                .setPaymentType(PaymentType.PAYMENT);
    }

    public static List<PaymentDto> createListOfPayments() {
        return List.of(
                new PaymentDto()
                        .setId(1L)
                        .setStatus(PaymentStatus.PAID)
                        .setPaymentType(PaymentType.PAYMENT)
                        .setRentalId(1L)
                        .setAmountToPay(BigDecimal.valueOf(400.00))
                        .setSessionUrl("http://example.com/success1")
                        .setSessionId("sess_1"),
                new PaymentDto()
                        .setId(2L)
                        .setStatus(PaymentStatus.PENDING)
                        .setPaymentType(PaymentType.PAYMENT)
                        .setRentalId(2L)
                        .setAmountToPay(BigDecimal.valueOf(200.00))
                        .setSessionUrl("http://example.com/success2")
                        .setSessionId("sess_2"),
                new PaymentDto()
                        .setId(3L)
                        .setStatus(PaymentStatus.PAID)
                        .setPaymentType(PaymentType.FINE)
                        .setRentalId(2L)
                        .setAmountToPay(BigDecimal.valueOf(50.00))
                        .setSessionUrl("http://example.com/success3")
                        .setSessionId("sess_3")
        );
    }
}
