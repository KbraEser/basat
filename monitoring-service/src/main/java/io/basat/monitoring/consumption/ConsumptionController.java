package io.basat.monitoring.consumption;

import io.basat.monitoring.common.ApiResponse;
import io.basat.monitoring.common.TokenClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Consumption", description = "Energy over time for the dashboard")
@RestController
@RequestMapping("/api/consumption")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @Operation(summary = "Energy per hour or day",
            description = "Sums the company's meters, or one site's meters, from local midnight at the start of the "
                    + "period until now. TODAY and LAST_7_DAYS return hourly points, LAST_30_DAYS daily points. "
                    + "Consumption and solar production are reported separately.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Points found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Unknown period or malformed site id",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Site does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ApiResponse<ConsumptionResponse> getConsumption(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Leave empty for all sites of the company")
            @RequestParam(required = false) UUID siteId,
            @RequestParam(defaultValue = "TODAY") ConsumptionPeriod period) {
        return ApiResponse.of(consumptionService.getConsumption(TokenClaims.tenantId(jwt), siteId, period));
    }
}