package com.adaucu.demo.seguridad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class HashDeContrasena {

    private static final int ITERACIONES = 210_000;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final int LONGITUD_SALT_BYTES = 16;

    private final SecureRandom aleatorio = new SecureRandom();

    public String generarSalt() {
        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        aleatorio.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hash(String contrasena, String saltBase64) {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] derivada = derivar(contrasena, salt);
        return Base64.getEncoder().encodeToString(derivada);
    }

    public boolean coincide(String contrasena, String saltBase64, String hashEsperadoBase64) {
        String hashCalculado = hash(contrasena, saltBase64);
        return MessageDigest.isEqual(
                hashCalculado.getBytes(),
                hashEsperadoBase64.getBytes());
    }

    private byte[] derivar(String contrasena, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), salt, ITERACIONES, LONGITUD_CLAVE_BITS);
            SecretKeyFactory factoria = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factoria.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contrasena", e);
        }
    }
}
