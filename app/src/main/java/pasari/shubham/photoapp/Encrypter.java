package pasari.shubham.photoapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

class Encrypter {
    private final static int READ_WRITE_BLOCK_BUFFER_SIZE = 8;
    private final static String ALGORITHM_FOR_IMAGE_ENCRYPTION = "AES/CBC/PKCS7Padding";

    //Function to encrypt the file
    @SuppressWarnings("resource")
    static void encrypt(SecretKey secretKey, AlgorithmParameterSpec paramSpec, InputStream in, OutputStream out) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_FOR_IMAGE_ENCRYPTION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
            out = new CipherOutputStream(out, cipher);
            int buffer_index = 0;
            byte[] buffer_array = new byte[READ_WRITE_BLOCK_BUFFER_SIZE];
            while ((buffer_index = in.read(buffer_array)) >= 0) {
                out.write(buffer_array, 0, buffer_index);
            }
        } finally {
            out.close();
        }
    }

    //Function to decrypt the file
    @SuppressWarnings("resource")
    static void decrypt(SecretKey secretKey, AlgorithmParameterSpec paramSpec, InputStream in, OutputStream out) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_FOR_IMAGE_ENCRYPTION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
            out = new CipherOutputStream(out, cipher);
            int buffer_index = 0;
            byte[] buffer_array = new byte[READ_WRITE_BLOCK_BUFFER_SIZE];
            while ((buffer_index = in.read(buffer_array)) >= 0) {
                out.write(buffer_array, 0, buffer_index);
            }
        } finally {
            out.close();
        }
    }



}
