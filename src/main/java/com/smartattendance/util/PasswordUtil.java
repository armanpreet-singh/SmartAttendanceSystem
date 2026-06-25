package com.smartattendance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil - Password Hashing and Verification Utility
 *
 * Responsibilities:
 *  - Hash plain-text passwords using SHA-256 with a salt.
 *  - Verify a plain-text password against a stored hashed password.
 *  - Generate cryptographically secure random salts.
 *
 * Security Notes:
 *  - Uses SHA-256 with a per-user random salt (Base64-encoded).
 *  - Stored format: BASE64(salt) + ":" + BASE64(hash)
 *  - In real production, prefer BCrypt or Argon2 (via Spring Security).
 *    SHA-256 with salt is used here to keep the project dependency-free
 *    and suitable for a college project context.
 *
 * Pattern: Utility Class (all static methods, no instantiation).
 */
public final class PasswordUtil {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

    // ----------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int    SALT_LENGTH    = 16;    // bytes
    private static final String SEPARATOR      = ":";   // Separates salt and hash

    // ----------------------------------------------------------------
    // Private Constructor (Utility class - no instantiation)
    // ----------------------------------------------------------------
    private PasswordUtil() {
        throw new UnsupportedOperationException(
                "PasswordUtil is a utility class and cannot be instantiated."
        );
    }

    // ----------------------------------------------------------------
    // generateSalt()
    // Generates a cryptographically secure random salt.
    // ----------------------------------------------------------------

    /**
     * Generates a cryptographically secure random salt.
     *
     * @return A Base64-encoded salt string.
     */
    public static String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] saltBytes = new byte[SALT_LENGTH];
        secureRandom.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    // ----------------------------------------------------------------
    // hashPassword()
    // Hashes a plain-text password with a new random salt.
    // ----------------------------------------------------------------

    /**
     * Hashes a plain-text password using SHA-256 with a random salt.
     *
     * <p>The returned string is in the format:
     * {@code BASE64(salt) + ":" + BASE64(SHA-256(salt + password))}</p>
     *
     * @param plainPassword The plain-text password to hash.
     * @return The hashed password string (salt + hash, separated by ":").
     * @throws RuntimeException if the hashing algorithm is unavailable.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        String salt = generateSalt();
        String hash = computeHash(salt, plainPassword);

        logger.debug("Password hashed successfully.");
        return salt + SEPARATOR + hash;
    }

    // ----------------------------------------------------------------
    // verifyPassword()
    // Verifies a plain-text password against a stored hashed password.
    // ----------------------------------------------------------------

    /**
     * Verifies a plain-text password against a stored hashed password.
     *
     * <p>Extracts the salt from the stored hash, re-hashes the provided
     * plain password with the same salt, and compares the results.</p>
     *
     * @param plainPassword  The plain-text password provided by the user.
     * @param storedPassword The stored hashed password (from the database).
     * @return {@code true} if the passwords match, {@code false} otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            logger.warn("Password verification failed: null input detected.");
            return false;
        }

        String[] parts = storedPassword.split(SEPARATOR);

        if (parts.length != 2) {
            // Legacy support: plain-text password stored directly (testing only)
            // This handles the sample_data.sql plain passwords during initial testing.
            logger.warn("Stored password is not in hashed format. Performing plain-text comparison.");
            return plainPassword.equals(storedPassword);
        }

        String salt              = parts[0];
        String storedHash        = parts[1];
        String computedHash      = computeHash(salt, plainPassword);

        boolean matches = computedHash.equals(storedHash);
        logger.debug("Password verification result: {}", matches ? "MATCH" : "NO MATCH");
        return matches;
    }

    // ----------------------------------------------------------------
    // computeHash()
    // Internal helper: Computes SHA-256 hash of salt + password.
    // ----------------------------------------------------------------

    /**
     * Computes the SHA-256 hash of the concatenation of salt and password.
     *
     * @param salt     The Base64-encoded salt string.
     * @param password The plain-text password.
     * @return The Base64-encoded SHA-256 hash.
     */
    private static String computeHash(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            // Concatenate salt + password before hashing
            String saltedPassword = salt + password;

            byte[] hashBytes = digest.digest(
                    saltedPassword.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            logger.error("FATAL: Hashing algorithm '{}' is not available.", HASH_ALGORITHM, e);
            throw new RuntimeException(
                    "Hashing algorithm '" + HASH_ALGORITHM + "' is not available.", e
            );
        }
    }

    // ----------------------------------------------------------------
    // isPasswordStrong()
    // Validates password strength before hashing/storing.
    // ----------------------------------------------------------------

    /**
     * Validates the strength of a plain-text password.
     *
     * <p>Rules:
     * <ul>
     *   <li>Minimum 8 characters.</li>
     *   <li>At least one uppercase letter.</li>
     *   <li>At least one lowercase letter.</li>
     *   <li>At least one digit.</li>
     *   <li>At least one special character (@, #, $, !, %, *, ?, &).</li>
     * </ul>
     * </p>
     *
     * @param password The plain-text password to validate.
     * @return {@code true} if the password meets all strength requirements.
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.matches(".*[@#$!%*?&].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}