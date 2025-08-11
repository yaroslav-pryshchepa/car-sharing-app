package car.sharing.app.mapper;

import car.sharing.app.config.MapperConfig;
import car.sharing.app.dto.payment.CreatePaymentRequestDto;
import car.sharing.app.dto.payment.PaymentDto;
import car.sharing.app.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(source = "rental.id", target = "rentalId")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rental", ignore = true)
    Payment toEntity(CreatePaymentRequestDto requestDto);
}
