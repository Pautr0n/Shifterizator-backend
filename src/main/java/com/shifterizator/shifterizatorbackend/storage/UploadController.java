package com.shifterizator.shifterizatorbackend.storage;

import com.shifterizator.shifterizatorbackend.employee.access.EmployeeAccessPolicy;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.auth.service.CurrentUserService;
import com.shifterizator.shifterizatorbackend.storage.dto.UploadUrlRequestDto;
import com.shifterizator.shifterizatorbackend.storage.dto.UploadUrlResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@ConditionalOnExpression("!'${r2.access-key:}'.isBlank()")
@Tag(name = "Media upload", description = "Obtain presigned URLs for direct upload of profile pictures to R2")
public class UploadController {

    private static final String EXT_JPEG = "jpg";
    private static final String EXT_PNG = "png";
    private static final String EXT_WEBP = "webp";

    private final R2StorageService r2StorageService;
    private final CurrentUserService currentUserService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAccessPolicy employeeAccessPolicy;

    @Operation(
            summary = "Get presigned upload URL",
            description = "Returns a presigned PUT URL and the final public URL. Upload the file with PUT to uploadUrl, then store finalUrl in user or employee profilePictureUrl.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload URL and final URL returned"),
            @ApiResponse(responseCode = "400", description = "Invalid scope, contentType, or missing entityId for employee"),
            @ApiResponse(responseCode = "403", description = "Not allowed to upload for this employee"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping("/upload-url")
    public ResponseEntity<UploadUrlResponseDto> getUploadUrl(@Valid @RequestBody UploadUrlRequestDto dto) {
        String key = buildKey(dto);
        String uploadUrl = r2StorageService.generateUploadUrl(key, dto.contentType());
        String finalUrl = r2StorageService.getPublicUrl(key);
        return ResponseEntity.ok(new UploadUrlResponseDto(uploadUrl, finalUrl));
    }

    private String buildKey(UploadUrlRequestDto dto) {
        String ext = extensionFromContentType(dto.contentType());
        if ("user".equals(dto.scope())) {
            long userId = currentUserService.getCurrentUser().getId();
            return "users/" + userId + "/avatar." + ext;
        }
        if (dto.entityId() == null) {
            throw new IllegalArgumentException("entityId is required when scope is employee");
        }
        Employee employee = employeeRepository.findActiveById(dto.entityId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        employeeAccessPolicy.ensureCanAccessEmployee(employee, currentUserService.getCurrentUser());
        return "employees/" + dto.entityId() + "/avatar." + ext;
    }

    private static String extensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> EXT_JPEG;
            case "image/png" -> EXT_PNG;
            case "image/webp" -> EXT_WEBP;
            default -> EXT_JPEG;
        };
    }
}
