package com.jcuadrado.erplitebackend.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordEncoderAdapterTest {

    private BCryptPasswordEncoderAdapter encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoderAdapter();
    }

    @Test
    @DisplayName("encode should return a non-blank BCrypt hash different from the raw password")
    void encode_shouldReturnHashDifferentFromRawPassword() {
        String rawPassword = "Secure@1";

        String encoded = encoder.encode(rawPassword);

        assertThat(encoded).isNotBlank();
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(encoded).startsWith("$2a$");
    }

    @Test
    @DisplayName("encode should produce different hashes for the same password on each call")
    void encode_shouldProduceDifferentHashes_forSamePassword() {
        String rawPassword = "Secure@1";

        String hash1 = encoder.encode(rawPassword);
        String hash2 = encoder.encode(rawPassword);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("matches should return true when raw password matches the encoded hash")
    void matches_shouldReturnTrue_whenPasswordMatchesHash() {
        String rawPassword = "Secure@1";
        String encoded = encoder.encode(rawPassword);

        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("matches should return false when raw password does not match the encoded hash")
    void matches_shouldReturnFalse_whenPasswordDoesNotMatch() {
        String rawPassword = "Secure@1";
        String encoded = encoder.encode(rawPassword);

        assertThat(encoder.matches("WrongPassword!", encoded)).isFalse();
    }
}
