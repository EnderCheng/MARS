package ca.uwaterloo.cheng.utils;

import ca.uwaterloo.cheng.data.GrothSig;
import ca.uwaterloo.cheng.mars.Params;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.ArrayList;

public class OT {

    private static Pairing pairing = PairingFactory.getPairing("f.properties");
    private final static int rbits = 256;
    private final static BigInteger order = pairing.getZr().getOrder();
    private static Element g_1;
    private BigInteger a, b;
    private static boolean TAG = false;

    public OT()
    {

    }

    public void Setup()
    {
        if(TAG == false) {
            g_1 = Params.read_g_1();
            TAG = true;
        }
    }

    public Element VD_Step_1()
    {
        a = Params.randomZq(rbits, order);
        Element A = g_1.pow(a).getImmutable();
        return A;
    }

    public Element User_Step_1(Element A, int i)
    {
        b = Params.randomZq(rbits, order);
        Element B = Params.Hash_4(A).pow(BigInteger.valueOf(i)).mul(g_1.pow(b)).getImmutable();
        return B;
    }

    public ArrayList<byte[]> VD_Step_2(Element A, Element B, ArrayList<GrothSig> sig)
    {
        int n = sig.size();
        ArrayList<byte[]> enc_data = new ArrayList<>();
        try {
            for (int k = 1; k <= n; k++) {
                Element temp = B.pow(a).mul(Params.Hash_4(A).pow(BigInteger.valueOf(k).multiply(a)).invert());
                byte[] key = Params.Hash_3(temp.toBytes());
                byte[] data = BufferUtils.ObjectToByte(sig.get(k - 1).toStringList());
                byte[] c_data = AES.encrypt(data, key);
                enc_data.add(c_data);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return enc_data;
    }

    public GrothSig User_Step_2(ArrayList<byte[]> enc_data, Element A, int i, int tag)
    {
        byte[] key = Params.Hash_3(A.pow(b).toBytes());
        try {
            byte[] dec_data = AES.decrypt(enc_data.get(i - 1), key);
            ArrayList<String> str_sig = (ArrayList<String>) BufferUtils.ByteToObject(dec_data);
            GrothSig sig = GrothSig.toGrothSig(str_sig, tag);
            return sig;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
