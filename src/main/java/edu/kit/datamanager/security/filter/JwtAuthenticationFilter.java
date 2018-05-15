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
package edu.kit.datamanager.security.filter;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author jejkal
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter{//extends UsernamePasswordAuthenticationFilter{

  private final String AUTHORIZATION_HEADER = "Authorization";
  private final String BEARER_TOKEN_IDENTIFIER = "Bearer ";
  private final AuthenticationManager authenticationManager;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager){
    this.authenticationManager = authenticationManager;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException{
    String authToken = request.getHeader(AUTHORIZATION_HEADER);
    if(authToken == null || !authToken.startsWith(BEARER_TOKEN_IDENTIFIER)){
      chain.doFilter(request, response);
    } else{
      Authentication authentication = authenticationManager.authenticate(new JwtAuthenticationToken(authToken.substring(BEARER_TOKEN_IDENTIFIER.length())));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      chain.doFilter(request, response);
    }
  }
}