/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.handlers;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author jejkal
 */
public class RestResponseEntityExceptionHandlerTest{

  @Test
  public void testHandleEntityNotFoundException(){
    ResponseEntity<Object> response = new RestResponseEntityExceptionHandler().handleEntityNotFound(new EntityNotFoundException("Not found"), null);
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void testHandleConflict(){
    ResponseEntity<Object> response = new RestResponseEntityExceptionHandler().handleBadRequest(new RuntimeException("Conflict"), null);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void testHandleConstraintViolationException(){
    ResponseEntity<Object> response = new RestResponseEntityExceptionHandler().handleConstraintViolationException(new ConstraintViolationException("Conflict", null, "constraint name"), null);
    Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
  }

  @Test
  public void testHandleHibernateException(){
    ResponseEntity<Object> response = new RestResponseEntityExceptionHandler().handleHibernateException(new HibernateException("Hibernate Exception"), null);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
