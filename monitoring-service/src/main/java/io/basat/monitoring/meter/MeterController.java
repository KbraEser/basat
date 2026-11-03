package io.basat.monitoring.meter;

import io.basat.monitoring.common.ApiResponse;
import io.basat.monitoring.common.TokenClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Meters", description = "Energy meters installed at the company's sites")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;

    @Operation(summary = "List the meters of a site", description = "Returns the meters ordered by name.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meters listed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Site does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/sites/{siteId}/meters")
    public ApiResponse<List<MeterResponse>> getMeters(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID siteId) {
        return ApiResponse.of(meterService.getMeters(TokenClaims.tenantId(jwt), siteId));
    }

    @Operation(summary = "Get a meter")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meter does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/meters/{meterId}")
    public ApiResponse<MeterResponse> getMeter(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID meterId) {
        return ApiResponse.of(meterService.getMeter(TokenClaims.tenantId(jwt), meterId));
    }

    @Operation(summary = "Add a meter to a site")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Meter created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed; the `errors` field lists invalid fields",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Viewers cannot add meters",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Site does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "A meter with this serial number already exists",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/sites/{siteId}/meters")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MANAGER')")
    public ApiResponse<MeterResponse> createMeter(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID siteId,
                                                  @Valid @RequestBody MeterRequest request) {
        return ApiResponse.of(meterService.createMeter(TokenClaims.tenantId(jwt), siteId, request),"Meter created successfully");
    }

    @Operation(summary = "Update a meter", description = "Replaces the details of the meter; it stays at its site.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Viewers cannot change meters",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meter does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Another meter already has this serial number",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/meters/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MANAGER')")
    public ApiResponse<MeterResponse> updateMeter(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID meterId,
                                                  @Valid @RequestBody MeterRequest request) {
        return ApiResponse.of(meterService.updateMeter(TokenClaims.tenantId(jwt), meterId, request), "Meter updated");
    }

    @Operation(summary = "Delete a meter")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Meter deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Viewers cannot delete meters",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Meter does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/meters/{meterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENERGY_MANAGER')")
    public void deleteMeter(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID meterId) {
        meterService.deleteMeter(TokenClaims.tenantId(jwt), meterId);
    }
}
