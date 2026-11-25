package com.curso.biblioteca2026.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoFiscalTest {

    @Test
    void deveValidarCpfCorretamente() {
        assertTrue(DocumentoFiscal.cpfValido("52998224725"));
        assertTrue(DocumentoFiscal.cpfValido("11144477735"));
        assertFalse(DocumentoFiscal.cpfValido("52998224724"));
        assertFalse(DocumentoFiscal.cpfValido("11111111111"));
        assertFalse(DocumentoFiscal.cpfValido("529.982.247-25"));
        assertFalse(DocumentoFiscal.cpfValido(null));
    }

    @Test
    void deveValidarCnpjCorretamente() {
        assertTrue(DocumentoFiscal.cnpjValido("11222333000181"));
        assertFalse(DocumentoFiscal.cnpjValido("11222333000182"));
        assertFalse(DocumentoFiscal.cnpjValido("11111111111111"));
        assertFalse(DocumentoFiscal.cnpjValido(null));
    }
}