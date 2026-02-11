package com.shifterizator.shifterizatorbackend.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
        name = "Health",
        description = "Health check endpoint for monitoring and load balancer integration"
)
public class HealthController {

    @Operation(
            summary = "Health check",
            description = """
                    Simple health check endpoint that returns "OK" if the service is running.
                    This endpoint is publicly accessible and does not require authentication.
                    
                    **Use Case:** 
                    - Load balancer health checks
                    - Monitoring system integration
                    - Service availability verification
                    """,
            tags = {"Health"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Service is healthy and responding"
            )
    })
    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
