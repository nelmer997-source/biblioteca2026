package com.curso.biblioteca2026.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePublicarContratoOpenApiComRotasEssenciaisESegurancaJwt() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("Biblioteca 2026 API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/api/categorias/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/categorias/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/livros/{id}'].get.summary")
                        .value("Buscar livro por ID"))
                .andExpect(jsonPath("$.paths['/api/livros/{id}'].get.responses['404'].content")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/livros/{id}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/livros/{id}/estoque/entradas'].post").exists())
                .andExpect(jsonPath("$.paths['/api/livros/{id}/estoque/saidas'].post").exists())
                .andExpect(jsonPath("$.paths['/api/pessoas'].post").exists())
                .andExpect(jsonPath("$.paths['/api/leitores/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/bibliotecarios'].post").exists())
                .andExpect(jsonPath("$.paths['/api/emprestimos/{id}/itens'].post").exists())
                .andExpect(jsonPath("$.paths['/api/emprestimos/{id}/finalizacao'].post").exists())
                .andExpect(jsonPath("$.paths['/api/contas-acesso'].post").exists())
                .andExpect(jsonPath("$.paths['/api/contas-acesso/me'].get").exists())
                .andExpect(jsonPath("$.paths['/api/contas-acesso/me/senha'].put").exists())
                .andExpect(jsonPath("$.paths['/api/autenticacao/login'].post").exists())
                .andExpect(jsonPath("$.components.schemas.LivroResponse").exists())
                .andExpect(jsonPath("$.components.schemas.EmprestimoResponse").exists())
                .andExpect(jsonPath("$.components.schemas.ContaAcessoResponse").exists())
                .andExpect(jsonPath("$.components.schemas.TokenResponse").exists())
                .andExpect(jsonPath("$.components.schemas.ApiError").exists());
    }
}