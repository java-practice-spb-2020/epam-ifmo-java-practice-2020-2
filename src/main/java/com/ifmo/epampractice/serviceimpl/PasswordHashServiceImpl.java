package com.ifmo.epampractice.serviceimpl;

import com.ifmo.epampractice.exceptions.HashingException;
import com.ifmo.epampractice.services.PasswordHashService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class PasswordHashServiceImpl implements PasswordHashService {

    private final char SALT_SEPARATOR = '#';

    @Override
    public String getHash(String password) throws HashingException {
        byte[] saltBytes = getSalt();
        byte[] passwordBytes = password.getBytes(StandardCharsets.US_ASCII);
        byte[] passwordHashBytes = getHash(passwordBytes, saltBytes);

        return new String(saltBytes, StandardCharsets.US_ASCII) +
                SALT_SEPARATOR +
                new String(passwordHashBytes, StandardCharsets.US_ASCII);
    }

    @Override
    public boolean isMatching(String saltedHash, String password)
            throws HashingException, IllegalArgumentException {

        String[] hashParts = saltedHash.split(Character.toString(SALT_SEPARATOR));
        if (hashParts.length != 2) {
            throw new IllegalArgumentException("'saltedHash' should contain a salt and a hash " +
                    "separated by '" + SALT_SEPARATOR + "' character.");
        }

        byte[] saltBytes = hashParts[0].getBytes(StandardCharsets.US_ASCII);
        byte[] hashBytes = hashParts[1].getBytes(StandardCharsets.US_ASCII);
        byte[] passwordBytes = password.getBytes(StandardCharsets.US_ASCII);
        byte[] passwordHashBytes = getHash(passwordBytes, saltBytes);

        return Arrays.equals(hashBytes, passwordHashBytes);
    }

    private byte[] getSalt() {
        SecureRandom rand = new SecureRandom();
        byte[] saltBytes = new byte[16];
        rand.nextBytes(saltBytes);

        // Ensure salt string doesn't contain separator character
        for (int i = 0; i < saltBytes.length; i++) {
            if (saltBytes[i] == SALT_SEPARATOR) {
                saltBytes[i] = '8'; // could be whatever other character
            }
        }
        return saltBytes;
    }

    private byte[] getHash(byte[] input, byte[] salt) throws HashingException {
        final String HASH_ALGORITHM = "SHA-512";

        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            return md.digest(input);
        }
        catch (NoSuchAlgorithmException e) {
            throw new HashingException("Hashing algorithm not found: " + HASH_ALGORITHM, e);
        }
    }

}
