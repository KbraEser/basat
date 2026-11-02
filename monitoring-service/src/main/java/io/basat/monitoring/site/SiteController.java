package io.basat.monitoring.site;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Sites", description = "Buildings and facilities of the signed-in company")
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @Operation(summary = "List sites", description = "Returns all sites of the signed-in company, ordered by name.")
    @GetMapping
    public ApiResponse<List<SiteResponse>> getSites(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.of(siteService.getSites(TokenClaims.tenantId(jwt)));
    }

    @Operation(summary = "Get a site")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Site does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{siteId}")
    public ApiResponse<SiteResponse> getSite(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID siteId) {
        return ApiResponse.of(siteService.getSite(TokenClaims.tenantId(jwt), siteId));
    }

    @Operation(summary = "Create a site")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Site created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed; the `errors` field lists invalid fields",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "The company already has a site with this name",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SiteResponse> createSite(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody SiteRequest request) {
        return ApiResponse.of(siteService.createSite(TokenClaims.tenantId(jwt), request), "Site created");
    }

    @Operation(summary = "Update a site", description = "Replaces all details of the site.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Site does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Another site of the company already has this name",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{siteId}")
    public ApiResponse<SiteResponse> updateSite(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID siteId,
                                                @Valid @RequestBody SiteRequest request) {
        return ApiResponse.of(siteService.updateSite(TokenClaims.tenantId(jwt), siteId, request), "Site updated");
    }

    @Operation(summary = "Delete a site")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Site deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Site does not exist or belongs to another company",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{siteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSite(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID siteId) {
        siteService.deleteSite(TokenClaims.tenantId(jwt), siteId);
    }
}

