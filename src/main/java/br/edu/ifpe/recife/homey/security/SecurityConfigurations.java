package br.edu.ifpe.recife.homey.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {
    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return  httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Autenticação - público
                        .requestMatchers(HttpMethod.POST, "/autenticacao/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/autenticacao/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/autenticacao/me").authenticated()
                        
                        // Criação de usuários - público
                        .requestMatchers(HttpMethod.POST, "/usuario/cliente").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario/prestador").permitAll()
                        
                        // Categorias - todas as operações públicas (facilitando uso inicial)
                        .requestMatchers("/categorias/**").permitAll()
                        
                        // Serviços - GET público, POST/PUT/DELETE apenas autenticados
                        .requestMatchers(HttpMethod.GET, "/servicos", "/servicos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/servicos").hasRole("PRESTADOR")
                        .requestMatchers(HttpMethod.PUT, "/servicos/**").hasRole("PRESTADOR")
                        .requestMatchers(HttpMethod.PATCH, "/servicos/**").hasRole("PRESTADOR")
                        .requestMatchers(HttpMethod.DELETE, "/servicos/**").hasRole("PRESTADOR")
                        
                        // Contratos - apenas autenticados
                        .requestMatchers(HttpMethod.POST, "/contratos").hasRole("CLIENTE")
                        .requestMatchers("/contratos/**").authenticated()
                        
                        // Console H2 - público (apenas dev)
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Swagger/OpenAPI - público
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Qualquer outra rota requer autenticação
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Token ausente ou inválido\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                            boolean isAnonymous = authentication == null || authentication instanceof AnonymousAuthenticationToken;

                            if (isAnonymous) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Faça login e informe o Bearer token\"}");
                                return;
                            }

                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Sem permissão para acessar este recurso\"}");
                        })
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
