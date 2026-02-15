package com.shifterizator.shifterizatorbackend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@ConditionalOnExpression("!'${r2.access-key:}'.isBlank()")
public class R2StorageService {

    private final S3Presigner presigner;
    private final String bucket;
    private final String publicBaseUrl;

    public R2StorageService(S3Presigner presigner,
                            @Value("${r2.bucket}") String bucket,
                            @Value("${r2.public-base-url}") String publicBaseUrl) {
        this.presigner = presigner;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
    }

    public String generateUploadUrl(String key, String contentType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putRequest)
                .build();
        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        return presigned.url().toString();
    }

    public String getPublicUrl(String key) {
        return publicBaseUrl + key;
    }
}
