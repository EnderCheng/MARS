package ca.uwaterloo.cheng.utils;

import java.security.Security;

import ca.uwaterloo.cheng.mars.Params;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AES {

    private static byte[] iv = {
            (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
            (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99,
            (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
            (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };

    public static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        byte[] outputBuffer = new byte[cipher.getOutputSize(data.length)];

        int length1 = cipher.processBytes(data,  0, data.length, outputBuffer, 0);
        int length2 = cipher.doFinal(outputBuffer, length1);

        byte[] result = new byte[length1+length2];

        System.arraycopy(outputBuffer, 0, result, 0, result.length);

        return result;
    }

    public static byte[] encrypt(byte[] plain, byte[] key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(
                        new AESEngine()
                )
        );

        aes.init(true, ivAndKey);

        return cipherData(aes, plain);

    }

    public static byte[] decrypt(byte[] cipher, byte[] key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(
                        new AESEngine()
                )
        );
        aes.init(false,  ivAndKey);

        return cipherData(aes, cipher);
    }

    public static void main(String[] args) throws Exception {
        byte[] key = Params.Hash_3("password".getBytes());
        byte[] plainText = "Plain text".getBytes("UTF-8");
        byte[] encryptedMessage = encrypt(plainText, key);
        System.out.println(encryptedMessage);
        byte[] decryptedMessage = decrypt(encryptedMessage, key);
        System.out.println(new String(decryptedMessage));
    }
}

