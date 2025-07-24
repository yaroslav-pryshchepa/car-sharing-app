package car.sharing.app.controller;

import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import car.sharing.app.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rentals Controller", description = "Managing users' car rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @PostMapping
    @Operation(summary = "Create new rental",
            description = "Creates a rental and decreases car inventory by 1")
    public RentalDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.create(requestDto);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @GetMapping
    @Operation(summary = "Get rentals by user and status",
            description = "Returns rentals filtered by userId and isActive")
    public List<RentalDto> getRentals(
            @RequestParam("user_id") Long userId,
            @RequestParam("is_active") Boolean isActive
    ) {
        return rentalService.findAllByUserIdAndIsActive(userId, isActive);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @GetMapping("/{id}")
    @Operation(summary = "Get rental by ID", description = "Returns rental by its ID")
    public RentalDto getRentalById(@PathVariable Long id) {
        return rentalService.findById(id);
    }

    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    @PostMapping("/{id}/return")
    @Operation(summary = "Return a rented car",
            description = "Sets actual return date and increases car inventory by 1")
    public RentalDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }
}
