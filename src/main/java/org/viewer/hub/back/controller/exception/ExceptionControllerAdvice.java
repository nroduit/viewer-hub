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

package org.viewer.hub.back.controller.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

/**
 * Handle exception behaviours
 */
@ControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

	@ExceptionHandler({ NotFoundException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<String> notFound(Throwable ex) {
		LOG.error("Not found:{}", ex.getMessage());
		return ResponseEntity.notFound().build();
	}

	@ExceptionHandler({ ConstraintViolationException.class, ParameterException.class, ConstraintException.class,
			MethodArgumentNotValidException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<String> badRequest(Throwable ex) {
		LOG.error("Bad request:{}", ex.getMessage());
		return ResponseEntity.badRequest().build();
	}

	@ExceptionHandler({ NoContentException.class })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<String> noContent(Throwable ex) {
		LOG.error("No content:{}", ex.getMessage());
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler({ TechnicalException.class, SQLException.class, JsonProcessingException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> technicalIssue(Throwable ex) {
		LOG.error("Technical issue:{}", ex.getMessage());
		return ResponseEntity.internalServerError().build();
	}

	@ExceptionHandler({ WeasisException.class })
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<String> weasisIssue(Throwable ex) {
		String message = ex.getMessage();
		LOG.error("Weasis issue:{}", message);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
	}

}
