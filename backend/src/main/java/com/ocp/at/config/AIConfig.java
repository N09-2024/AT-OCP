package com.ocp.at.config;

import com.ocp.at.ai.IAProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Sélectionne le provider IA actif via la propriété {@code ocp.ai.provider}.
 * <p>
 * Valeurs acceptées :
 * <ul>
 *   <li>{@code MOCK} (défaut) - Mock déterministe, pas de réseau requis</li>
 *   <li>{@code LANG_CHAIN} - LangChain direct (1 appel Gemini via FastAPI)</li>
 *   <li>{@code CREW_AI}    - CrewAI multi-agents (Agent Risques → HSE → Contrôle)</li>
 * </ul>
 * Si la propriété est absente ou inconnue, le Mock est utilisé pour que le
 * service reste opérationnel même sans microservice Python configuré.
 */
@Configuration
public class AIConfig {

    @Bean
    @Primary
    public IAProvider activeIAProvider(
            @Value("${ocp.ai.provider:MOCK}") String providerName,
            @Qualifier("mockAIProvider") IAProvider mock,
            @Qualifier("langChainProvider") IAProvider langChain,
            @Qualifier("crewAIProvider") IAProvider crewAI) {

        if (providerName != null) {
            String name = providerName.toUpperCase();
            if ("CREW_AI".equals(name) || "CREWAI".equals(name)) {
                return crewAI;
            }
            if ("LANG_CHAIN".equals(name) || "LANGCHAIN".equals(name)) {
                return langChain;
            }
        }
        return mock;
    }
}
