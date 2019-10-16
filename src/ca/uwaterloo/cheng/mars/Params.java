package ca.uwaterloo.cheng.mars;

import ca.uwaterloo.cheng.utils.ProperUtil;
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.f.TypeFCurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;

public class Params {

    private static Pairing pairing = PairingFactory.getPairing("f.properties");

    public static Element Hash_1(String input) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest messageDigest = new SHA256.Digest();
        byte[] output = messageDigest.digest(input.getBytes());
        return pairing.getG1().newElementFromBytes(output).getImmutable();
    }

    public static Element Hash_2(String input) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest messageDigest = new SHA256.Digest();
        byte[] output = messageDigest.digest(input.getBytes());
        return pairing.getG2().newElementFromBytes(output).getImmutable();
    }

    public static byte[] Hash_3(byte[] input) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest messageDigest = new SHA256.Digest();
        byte[] output = messageDigest.digest(input);
        return output;
    }

    public static Element Hash_4(Element in) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest messageDigest = new SHA256.Digest();
        byte[] output = messageDigest.digest(in.toBytes());
        return pairing.getG1().newElementFromBytes(output).getImmutable();
    }

    public static BigInteger Hash(byte[] input) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest messageDigest = new SHA256.Digest();
        byte[] output = messageDigest.digest(input);
        BigInteger data = new BigInteger(output);
        return data;
    }

    public static BigInteger randomZq(int len, BigInteger q)
    {
        BigInteger r;
        do
        {
            r = new BigInteger(len-1, new SecureRandom());
        }
        while (r.compareTo(q) >= 0);
        return r;
    }

    public static Element read_g_1()
    {
        Element g_1 = null;
        try {
            byte[] g_1_temp = Base64.decode(ProperUtil.getConfigProperties("g_1"));
            if (g_1_temp != null) {
                g_1 = pairing.getG1().newElementFromBytes(g_1_temp).getImmutable();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return g_1;
    }

    public static Element read_g_2()
    {
        Element g_2 = null;
        try {
            byte[] g_2_temp = Base64.decode(ProperUtil.getConfigProperties("g_2"));
            if (g_2_temp != null) {
                g_2 = pairing.getG2().newElementFromBytes(g_2_temp).getImmutable();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return g_2;
    }

    public static PairingParameters GeneratePairing() {
        int rBits = 256;
        PairingParametersGenerator pg = new TypeFCurveGenerator(rBits);
        PairingParameters params = pg.generate();
        try{
            File file = new File("f.properties");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(params.toString().getBytes("UTF-8"));
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Pairing pairing = PairingFactory.getPairing(params);
        Element g_1 = pairing.getG1().newRandomElement().getImmutable();
        Element g_2 = pairing.getG2().newRandomElement().getImmutable();
        ProperUtil.writeDateToLocalFile("g_1", Base64.encodeBytes(g_1.toBytes()));
        ProperUtil.writeDateToLocalFile("g_2", Base64.encodeBytes(g_2.toBytes()));
        return params;
    }

    public static void main(String[] args) {
        GeneratePairing();
//        Element g_1_t = pairing.getG1().newRandomElement().getImmutable();
//        Element g_2_t = pairing.getG2().newRandomElement().getImmutable();
//        double start = System.nanoTime();
//        for(int i=0;i<100;i++) {
//            Element g_t = pairing.pairing(g_1_t, g_2_t);
//        }
//        double end = System.nanoTime();
//        System.out.println((end-start)/(1000000*100));
//        PairingPreProcessing g_pre = pairing.getPairingPreProcessingFromElement(g_1_t);
//        start = System.nanoTime();
//        Element g_t_2 = g_pre.pairing(g_2_t);
//        end = System.nanoTime();
//        System.out.println((end-start)/(1000000));
    }

}
