package io.basat.monitoring.kpi;

import io.basat.monitoring.common.ApiResponse;
import io.basat.monitoring.common.TokenClaims;
import io.basat.monitoring.consumption.ConsumptionPeriod;
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

@Tag(name = "KPIs", description = "Summary figures for the dashboard cards")
@RestController
@RequestMapping("/api/kpis")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    @Operation(summary = "Consumption, cost, CO2 and solar production of a period",
            description = "Cost uses the configured tariff and excludes taxes; it is also split into the day, "
                    + "peak and night bands. CO2 uses the grid emission factor. Solar production is not included "
                    + "in consumption or cost.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Figures calculated"),
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
    public ApiResponse<KpiResponse> getKpis(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Leave empty for all sites of the company")
            @RequestParam(required = false) UUID siteId,
            @RequestParam(defaultValue = "TODAY") ConsumptionPeriod period) {
        return ApiResponse.of(kpiService.getKpis(TokenClaims.tenantId(jwt), siteId, period));
    }
}