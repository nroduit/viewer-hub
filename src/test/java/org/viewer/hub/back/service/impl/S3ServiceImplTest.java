/*
 *  Copyright (c) 2022-2025 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.viewer.hub.back.config.s3.DeleteResource;
import org.viewer.hub.back.config.s3.DownloadResource;
import org.viewer.hub.back.config.s3.UploadResource;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.transfer.s3.model.CompletedCopy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

	// Mocks
	@Mock
	UploadResource uploadResource;

	@Mock
	DownloadResource downloadResource;

	@Mock
	DeleteResource deleteResource;

	@InjectMocks
	private S3ServiceImpl s3Service;

	@Test
    @DisplayName("doesS3KeyExists: case key exists")
    void given_existingKey_when_checkingExistence_then_shouldReturnTrue() {
        // Given
        // Mock downloadResource
        when(this.downloadResource.checkS3KeyExists(anyString())).thenReturn(true);

        // When
        boolean toTest = this.s3Service.doesS3KeyExists("existingKey");

        // Then
        assertThat(toTest).isTrue();
    }

	@Test
    @DisplayName("doesS3KeyExists: case key not existing")
    void given_notExistingKey_when_checkingExistence_then_shouldReturnFalse() {
        // Given
        // Mock downloadResource
        when(this.downloadResource.checkS3KeyExists(anyString())).thenReturn(false);

        // When
        boolean toTest = this.s3Service.doesS3KeyExists("notExistingKey");

        // Then
        assertThat(toTest).isFalse();
    }

	@Test
	@DisplayName("doesS3KeyExists: case key blank")
	void given_blankKey_when_checkingExistence_then_shouldReturnFalse() {
		// When
		boolean toTest = this.s3Service.doesS3KeyExists("");

		// Then
		assertThat(toTest).isFalse();
	}

	@Test
    @DisplayName("retrieveS3KeysFromPrefix: case keys from prefix exist")
    void given_existingKeysFromPrefix_when_retrieveS3KeysFromPrefix_then_shouldReturnKeys() {
        // Given
        // Mock downloadResource
        when(this.downloadResource.retrieveS3KeysFromPrefix(anyString())).thenReturn(Set.of("key"));

        // When
        Set<String> toTest = this.s3Service.retrieveS3KeysFromPrefix("prefix");

        // Then
        assertThat(toTest).isNotEmpty();
    }

	@Test
    @DisplayName("retrieveS3KeysFromPrefix: case keys from prefix not existing")
    void given_notExistingKeysFromPrefix_when_retrieveS3KeysFromPrefix_then_shouldReturnEmptySet() {
        // Given
        // Mock downloadResource
        when(this.downloadResource.retrieveS3KeysFromPrefix(anyString())).thenReturn(Collections.emptySet());

        // When
        Set<String> toTest = this.s3Service.retrieveS3KeysFromPrefix("prefix");

        // Then
        assertThat(toTest).isEmpty();
    }

	@Test
	@DisplayName("retrieveS3KeysFromPrefix: case prefix is blank")
	void given_blankPrefix_when_retrieveS3KeysFromPrefix_then_shouldReturnEmptySet() {
		// When
		Set<String> toTest = this.s3Service.retrieveS3KeysFromPrefix("");

		// Then
		assertThat(toTest).isEmpty();
	}

	@Test
    @DisplayName("retrieveS3Object: case object exist")
    void given_existingObject_when_retrieveS3Object_then_shouldReturnInputStream() throws IOException {
        // Given
        // Mock downloadResource
        when(this.downloadResource.retrieveS3ObjectInputStream(anyString())).thenReturn(InputStream.nullInputStream());

        InputStream toTest = null;
        try{
            // When
            toTest =  this.s3Service.retrieveS3Object("key");
            // Then
            assertThat(toTest).isNotNull();
        }
        finally{
            if (toTest != null){
                toTest.close();
            }
        }
    }

	@Test
    @DisplayName("retrieveS3Object: case object not existing")
    void given_notExistingObject_when_retrieveS3Object_then_shouldReturnNull() throws IOException {
        // Given
        // Mock downloadResource
        when(this.downloadResource.retrieveS3ObjectInputStream(anyString())).thenReturn(null);

        InputStream toTest = null;
        try{
            // When
            toTest =  this.s3Service.retrieveS3Object("key");
            // Then
            assertThat(toTest).isNull();
        }
        finally{
            if (toTest != null){
                toTest.close();
            }
        }
    }

	@Test
	@DisplayName("retrieveS3Object: case key blank")
	void given_keyBlank_when_retrieveS3Object_then_shouldReturnNull() {
		// When
		InputStream toTest = this.s3Service.retrieveS3Object("");
		// Then
		assertThat(toTest).isNull();

	}

	@Test
	@DisplayName("uploadObjectInS3: case inputStream null")
	void given_nullInputStream_when_uploadObjectInS3_then_shouldReturnNull() {
		// When
		CompletableFuture<PutObjectResponse> toTest = this.s3Service.uploadObjectInS3(null, "existingKey");

		// Then
		assertThat(toTest).isNull();
	}

	@Test
	@DisplayName("uploadObjectInS3: case key blank")
	void given_blankKey_when_uploadObjectInS3_then_shouldReturnNull() {
		// Given
		ByteArrayInputStream byteArrayInputStreamMock = mock(ByteArrayInputStream.class);

		// When
		CompletableFuture<PutObjectResponse> toTest = this.s3Service.uploadObjectInS3(byteArrayInputStreamMock, "");

		// Then
		assertThat(toTest).isNull();
	}

	@Test
    @DisplayName("uploadObjectInS3: case valid criteria")
    void given_validCriteria_when_uploadObjectInS3_then_shouldReturnNotNullFuture() {
        // Given
        // Mock uploadResource
        when(this.uploadResource.uploadObject(any(ByteArrayInputStream.class), anyString())).thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));
        ByteArrayInputStream byteArrayInputStreamMock = mock(ByteArrayInputStream.class);

        // When
        CompletableFuture<PutObjectResponse> toTest = this.s3Service.uploadObjectInS3(byteArrayInputStreamMock, "key");

        // Then
        assertThat(toTest).isNotNull();
    }

	@Test
	@DisplayName("copyS3ObjectFromTo: case source key blank")
	void given_blankSourceKey_when_copyS3ObjectFromTo_then_shouldReturnNull() {
		// When
		CompletableFuture<CompletedCopy> toTest = this.s3Service.copyS3ObjectFromTo("", "destinationKey");

		// Then
		assertThat(toTest).isNull();
	}

	@Test
	@DisplayName("copyS3ObjectFromTo: case destination key blank")
	void given_blankDestinationKey_when_copyS3ObjectFromTo_then_shouldReturnNull() {
		// When
		CompletableFuture<CompletedCopy> toTest = this.s3Service.copyS3ObjectFromTo("sourceKey", "");

		// Then
		assertThat(toTest).isNull();
	}

	@Test
    @DisplayName("copyS3ObjectFromTo: case valid criteria")
    void given_validCriteria_when_copyS3ObjectFromTo_then_shouldReturnNotNullFuture() {
        // Given
        // Mock uploadResource
        when(this.uploadResource.copyObjectFromTo(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(CompletedCopy.builder().response(CopyObjectResponse.builder().build()).build()));

        // When
        CompletableFuture<CompletedCopy> toTest = this.s3Service.copyS3ObjectFromTo("sourceKey", "destinationKey");

        // Then
        assertThat(toTest).isNotNull();
    }

	@Test
	@DisplayName("deleteS3Objects: case prefix blank")
	void given_blankPrefixKey_when_deleteS3Objects_then_shouldReturnNullCompletableFuture()
			throws ExecutionException, InterruptedException {
		// When
		CompletableFuture<DeleteObjectsResponse> toTest = this.s3Service.deleteS3Objects("");

		// Then
		assertThat(toTest.get()).isNull();
	}

}
