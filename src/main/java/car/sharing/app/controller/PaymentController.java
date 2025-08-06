package car.sharing.app.controller;

import car.sharing.app.dto.payment.CreatePaymentRequestDto;
import car.sharing.app.dto.payment.PaymentDto;
import car.sharing.app.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Payments Controller", description = "Managing payment sessions for rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @PostMapping
    @Operation(summary = "Create payment session",
            description = "Creates a payment session for the given rental and returns session info")
    public PaymentDto createPayment(
            @RequestBody @Valid CreatePaymentRequestDto requestDto,
            Authentication authentication,
            UriComponentsBuilder uriBuilder
    ) {
        return paymentService.createPayment(requestDto, authentication, uriBuilder);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @GetMapping
    @Operation(summary = "Get payments by user ID",
            description = "Returns payments filtered by userId")
    public List<PaymentDto> getPayments(
            @RequestParam(required = false, name = "user_id") Long userId,
            Authentication authentication
    ) {
        return paymentService.getPayments(userId, authentication);
    }

    @PermitAll
    @GetMapping("/success")
    @Operation(summary = "Handle successful Stripe payment",
            description = "Handles redirect from Stripe when payment succeeds")
    public ResponseEntity<String> handleStripeSuccess(
            @RequestParam(value = "session_id", required = false) String sessionId
    ) {
        paymentService.markPaymentCompleted(sessionId);
        return ResponseEntity.ok("Payment was successful"
                + (sessionId != null ? " (Session ID: " + sessionId + ")" : "") + ".");
    }

    @PermitAll
    @GetMapping("/cancel")
    @Operation(summary = "Handle canceled Stripe payment",
            description = "Informs the user that the payment was canceled")
    public ResponseEntity<String> handleStripeCancel() {
        String message = "Payment was canceled. "
                + "You can complete the payment later using the link, which is valid for 24 hours "
                + "from the moment the payment session was created.";
        return ResponseEntity.ok(message);
    }
}
