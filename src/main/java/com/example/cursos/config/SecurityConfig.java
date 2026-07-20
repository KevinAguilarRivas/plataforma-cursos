package com.example.cursos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    // Origenes permitidos para el frontend (demo). En producción, restringir al dominio real.
    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    // Claim personalizado configurado en el User Flow de Azure AD B2C
    private static final String CLAIM_ROL = "extension_rolCurso";
    private static final String ROL_ESTUDIANTE = "estudiante";
    private static final String ROL_INSTRUCTOR = "instructor";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ---- Gestión de cursos y materiales: solo INSTRUCTOR ----
                        .requestMatchers(HttpMethod.POST, "/cursos/crear")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR))
                        .requestMatchers(HttpMethod.POST, "/cursos/subir")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR))
                        .requestMatchers(HttpMethod.PUT, "/cursos/actualizar")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR))
                        .requestMatchers(HttpMethod.DELETE, "/cursos/eliminar")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR))
                        .requestMatchers(HttpMethod.POST, "/calificaciones/registrar")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR))
                        .requestMatchers(HttpMethod.POST, "/inscripciones/procesar-cola")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR))

                        // ---- Consulta de materiales y descarga: ambos roles ----
                        .requestMatchers(HttpMethod.GET, "/cursos/consultar")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR, ROL_ESTUDIANTE))
                        .requestMatchers(HttpMethod.GET, "/cursos/descargar")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR, ROL_ESTUDIANTE))

                        // ---- Inscripción a cursos: solo ESTUDIANTE ----
                        .requestMatchers(HttpMethod.POST, "/inscripciones/inscribir")
                        .access((authentication, context) -> soloRol(authentication, ROL_ESTUDIANTE))

                        // ---- Consulta de calificaciones: ambos roles ----
                        .requestMatchers(HttpMethod.GET, "/calificaciones/consultar")
                        .access((authentication, context) -> soloRol(authentication, ROL_INSTRUCTOR, ROL_ESTUDIANTE))

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    private AuthorizationDecision soloRol(java.util.function.Supplier<org.springframework.security.core.Authentication> authentication,
                                           String... rolesPermitidos) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication.get();
        Jwt jwt = (Jwt) jwtAuth.getToken();
        String rol = jwt.getClaimAsString(CLAIM_ROL);
        for (String permitido : rolesPermitidos) {
            if (permitido.equals(rol)) {
                return new AuthorizationDecision(true);
            }
        }
        return new AuthorizationDecision(false);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        if ("*".equals(allowedOrigins)) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }
}
