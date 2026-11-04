package io.basat.ingestion.reading;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Readings", description = "Energy readings sent by meters")
@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingPublisher readingPublisher;

    @Operation(summary = "Send a reading",
            description = "Validates the reading and hands it over for processing; storage happens asynchronously.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Reading accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Validation failed; the `errors` field lists invalid fields",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendReading(@Valid @RequestBody ReadingRequest request){
        readingPublisher.publish(request.toReading());
    }

}
