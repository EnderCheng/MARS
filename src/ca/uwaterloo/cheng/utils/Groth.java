package ca.uwaterloo.cheng.utils;

import ca.uwaterloo.cheng.data.GrothKey;
import ca.uwaterloo.cheng.data.GrothSig;
import ca.uwaterloo.cheng.mars.Params;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.ArrayList;

public class Groth {

    private static Pairing pairing = PairingFactory.getPairing("f.properties");
    private final static int rbits = 256;
    private final static BigInteger order = pairing.getZr().getOrder();
    private static Element g_1, g_2;
    public static ArrayList<Element> y_1, y_2;
    private static boolean TAG = false;

    public Groth()
    {

    }

    public static void Setup(int n_1, int n_2)
    {
        if(TAG == false) {
            g_1 = Params.read_g_1();
            g_2 = Params.read_g_2();
            y_1 = new ArrayList<>();
            for (int i = 1; i <= n_1; i++) {
                y_1.add(pairing.getG1().newRandomElement().getImmutable());
            }
            y_2 = new ArrayList<>();
            for (int i = 1; i <= n_2; i++) {
                y_2.add(pairing.getG2().newRandomElement().getImmutable());
            }
            TAG = true;
        }
    }

    public static GrothKey Gen_1(){
        BigInteger v = Params.randomZq(rbits, order);
        BigInteger sk_1 = v;
        Element vk_1 = g_2.pow(sk_1).getImmutable();
        return new GrothKey(sk_1,vk_1);
    }

    public static GrothKey Gen_2(){
        BigInteger v = Params.randomZq(rbits, order);
        BigInteger sk_2 = v;
        Element vk_2 = g_1.pow(sk_2).getImmutable();
        return new GrothKey(sk_2,vk_2);
    }

    public static GrothSig Sign_1(ArrayList<Element> M_1, int n, BigInteger sk){
        BigInteger r = Params.randomZq(rbits, order);
        Element R = g_2.pow(r).getImmutable();
        Element y_1_1 = y_1.get(0);
        BigInteger r_inv = r.modInverse(order);
        Element S = (y_1_1.mul(g_1.pow(sk))).pow(r_inv).getImmutable();
        ArrayList<Element> T = new ArrayList<>();
        for(int i=0;i<n;i++)
        {
            Element m_i = M_1.get(i);
            Element y_i = y_1.get(i);
            Element T_i = (y_i.pow(sk).mul(m_i)).pow(r_inv).getImmutable();
            T.add(T_i);
        }
        return new GrothSig(R,S,T);
    }

    public static GrothSig Sign_2(ArrayList<Element> M_2, int n, BigInteger sk){
        BigInteger r = Params.randomZq(rbits, order);
        Element R = g_1.pow(r).getImmutable();
        Element y_2_1 = y_2.get(0);
        BigInteger r_inv = r.modInverse(order);
        Element S = (y_2_1.mul(g_2.pow(sk))).pow(r_inv).getImmutable();
        ArrayList<Element> T = new ArrayList<>();
        for(int i=0;i<n;i++)
        {
            Element m_i = M_2.get(i);
            Element y_i = y_2.get(i);
            Element T_i = (y_i.pow(sk).mul(m_i)).pow(r_inv).getImmutable();
            T.add(T_i);
        }
        return new GrothSig(R,S,T);
    }

    public static boolean Verify_1(GrothSig sig, ArrayList<Element> M_1, int n, Element vk)
    {
        Element left = pairing.pairing(sig.S, sig.R);
        Element right = pairing.pairing(y_1.get(0),g_2).mul(pairing.pairing(g_1,vk));
        if(!left.isEqual(right))
            return false;
        for(int i=0;i<n;i++)
        {
            Element m_i = M_1.get(i);
            Element y_i = y_1.get(i);
            Element T_i = sig.T.get(i);
            left = pairing.pairing(T_i, sig.R);
            right = pairing.pairing(y_i,vk).mul(pairing.pairing(m_i,g_2));
            if(!left.isEqual(right))
                return false;
        }
        return true;
    }

    public static boolean Verify_2(GrothSig sig, ArrayList<Element> M_2, int n, Element vk)
    {
        Element left = pairing.pairing(sig.R, sig.S);
        Element right = pairing.pairing(g_1,y_2.get(0)).mul(pairing.pairing(vk, g_2));
        if(!left.isEqual(right))
            return false;
        for(int i=0;i<n;i++)
        {
            Element m_i = M_2.get(i);
            Element y_i = y_2.get(i);
            Element T_i = sig.T.get(i);
            left = pairing.pairing(sig.R, T_i);
            right = pairing.pairing(vk,y_i).mul(pairing.pairing(g_1,m_i));
            if(!left.isEqual(right))
                return false;
        }
        return true;
    }

    public static GrothSig Randomize(GrothSig sig)
    {
        BigInteger r = Params.randomZq(rbits, order);
        BigInteger r_inv = r.modInverse(order);
        Element R = sig.R.pow(r).getImmutable();
        Element S = sig.S.pow(r_inv).getImmutable();
        int n = sig.T.size();
        ArrayList<Element> T = new ArrayList<>();
        for(int i=0;i<n;i++)
        {
            Element T_i = sig.T.get(i).pow(r_inv).getImmutable();
            T.add(T_i);
        }
        return new GrothSig(R,S,T);
    }

    public static void main(String[] args) {
        int n_1 = 3;
        int n_2 = 3;
        ArrayList<Element> M_1 = new ArrayList<>();
        ArrayList<Element> M_2 = new ArrayList<>();

        for(int i=0;i<n_1;i++)
        {
            M_1.add(pairing.getG1().newRandomElement().getImmutable());
        }
        for(int i=0;i<n_2;i++)
        {
            M_2.add(pairing.getG2().newRandomElement().getImmutable());
        }

        Groth groth = new Groth();
        groth.Setup(n_1,n_2);

        GrothKey key_1 = groth.Gen_1();
        GrothKey key_2 = groth.Gen_2();

        GrothSig sig_1 = groth.Sign_1(M_1,n_1,key_1.sk);
        GrothSig sig_2 = groth.Sign_2(M_2,n_2,key_2.sk);

        boolean rst_1 = groth.Verify_1(sig_1,M_1,n_1,key_1.vk);
        boolean rst_2 = groth.Verify_2(sig_2,M_2,n_2,key_2.vk);

        System.out.println(rst_1);
        System.out.println(rst_2);

        GrothSig sig_1_1 = groth.Randomize(sig_1);
        GrothSig sig_2_1 = groth.Randomize(sig_2);

        boolean rst_3 = groth.Verify_1(sig_1_1,M_1,n_1,key_1.vk);
        boolean rst_4 = groth.Verify_2(sig_2_1,M_2,n_2,key_2.vk);

        System.out.println(rst_3);
        System.out.println(rst_4);

        ArrayList<String> str_sig_1 = sig_1.toStringList();
        ArrayList<String> str_sig_2 = sig_2.toStringList();

        byte[] byte_sig_1 = BufferUtils.ObjectToByte(str_sig_1);
        byte[] byte_sig_2 = BufferUtils.ObjectToByte(str_sig_2);

        byte[] key = Params.Hash_3("password".getBytes());
        try {
            byte[] ciphertext_sig_1 = AES.encrypt(byte_sig_1, key);
            byte[] ciphertext_sig_2 = AES.encrypt(byte_sig_2, key);

            byte[] text_sig_1 = AES.decrypt(ciphertext_sig_1, key);
            byte[] text_sig_2= AES.decrypt(ciphertext_sig_2, key);

            str_sig_1 = (ArrayList<String>) BufferUtils.ByteToObject(text_sig_1);
            str_sig_2 = (ArrayList<String>) BufferUtils.ByteToObject(text_sig_2);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        GrothSig sig_1_2 = GrothSig.toGrothSig(str_sig_1,1);
        GrothSig sig_2_2 = GrothSig.toGrothSig(str_sig_2,2);

        System.out.println(sig_1_2.R.isEqual(sig_1.R));
        System.out.println(sig_2_2.R.isEqual(sig_2.R));

        ArrayList<GrothSig> sig_list = new ArrayList<>();
        sig_list.add(sig_2_1);
        sig_list.add(sig_2_2);

        OT ot = new OT();
        ot.Setup();
        Element A = ot.VD_Step_1();
        Element B = ot.User_Step_1(A, 1);
        ArrayList<byte[]> enc_data = ot.VD_Step_2(A, B, sig_list);
        GrothSig sig_test = ot.User_Step_2(enc_data, A, 1, 2);
        System.out.println(sig_test.R.isEqual(sig_2_1.R));
    }

}
