package com.fuse.utils.reporttemplate;

import java.io.InputStream;

import com.fuse.utils.S3DependencyFactory;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3 extends ReportTemplate {
	private final S3Client s3Client;
	private final String bucketName = System.getenv("FACTION_BUCKET_NAME");

	private final String bucketPath = System.getenv("FACTION_MONGO_DATABASE");

	public S3() {
		this.s3Client = S3DependencyFactory.s3Client();
	}

	public void uploadTemplate(String templateName, byte[] templateBytes) {
		String key = this.bucketPath + "/" + templateName;

		// this.setup();
		this.s3Client.putObject(PutObjectRequest.builder().bucket(this.bucketName).key(key).build(),
				RequestBody.fromBytes(templateBytes));

	}

	public String setup() {
		try {
			this.s3Client.createBucket(CreateBucketRequest.builder().bucket(this.bucketName).build());
			this.s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder().bucket(this.bucketName).build());

		} catch (S3Exception e) {
			System.err.println(e.awsErrorDetails().errorMessage());
		}
		return super.setup();
	}

	public InputStream getTemplate(String fileName) {
		String s3FileName = this.bucketPath + "/" + fileName;
		GetObjectRequest s3ObjectRequest = GetObjectRequest.builder().bucket(this.bucketName).key(s3FileName).build();
		ResponseInputStream<GetObjectResponse> response = this.s3Client.getObject(s3ObjectRequest);
		return (InputStream) response;
	}

	public void deleteTemplate(String fileName) {
		String keyName = this.bucketPath + "/" + fileName;
		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(this.bucketName).key(keyName)
					.build();
			this.s3Client.deleteObject(deleteObjectRequest);
		} catch (S3Exception e) {
			System.err.println(e.awsErrorDetails().errorMessage());
		}
	}
}
