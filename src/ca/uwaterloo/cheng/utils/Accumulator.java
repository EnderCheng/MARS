package ca.uwaterloo.cheng.utils;


import ca.uwaterloo.cheng.mars.Params;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

public class Accumulator {
    private static Pairing pairing = PairingFactory.getPairing("f.properties");
    private final static int rbits = 256;
    private final static BigInteger order = pairing.getZr().getOrder();

    public static HashSet<Integer> U_1,U_2;
    public static BigInteger alpha,beta;
    public static Element Acc_1, Acc_2, g_1, g_2, eta_1, eta_2;
    public static ArrayList<Element> gg_1, gg_2, gg_3, gg_4;
    public static int l_1,l_2;

    private static boolean TAG = false;

    public Accumulator()
    {

    }

    private static void Initialize(int l, ArrayList<Element> _g_1, ArrayList<Element> _g_2, BigInteger exp)
    {
        for (int i = 1; i <= 2 * l; i++) {
            if (i != (l + 1)) {
                Element tmp_1 = g_1.pow(exp.pow(i)).getImmutable();
                Element tmp_2 = g_2.pow(exp.pow(i)).getImmutable();
                _g_1.add(tmp_1);
                _g_2.add(tmp_2);
            } else {
                _g_1.add(null);
                _g_2.add(null);
            }
        }
    }

    public static void Setup(int _l_1, int _l_2) {
        if (TAG == false) {
            g_1 = Params.read_g_1();
            g_2 = Params.read_g_2();
            TAG = true;
        }

        l_1 = _l_1;
        U_1 = new HashSet<>();
        alpha = Params.randomZq(rbits, order);
        Acc_1 = pairing.getG2().newOneElement().getImmutable();
        eta_1 = pairing.pairing(g_1, g_2).pow(alpha.pow(l_1 + 1)).getImmutable();
        gg_1 = new ArrayList<>();
        gg_2 = new ArrayList<>();

        Initialize(l_1, gg_1,gg_2, alpha);

        l_2 = _l_2;
        U_2 = new HashSet<>();
        beta = Params.randomZq(rbits, order);
        Acc_2 = pairing.getG1().newOneElement().getImmutable();
        eta_2 = pairing.pairing(g_1, g_2).pow(beta.pow(l_2 + 1)).getImmutable();
        gg_3 = new ArrayList<>();
        gg_4 = new ArrayList<>();

        Initialize(l_2, gg_3,gg_4, beta);
    }

    public static void AccGen_1(int i)
    {
        U_1.add(i);
        Acc_1 = Acc_1.mul(gg_2.get(l_1-i));
    }

    public static void Revoke_1(int i)
    {
        U_1.remove(i);
        Acc_1 = Acc_1.mul(gg_2.get(l_1-i).invert());
    }
    public static void AccGen_2(int i)
    {
        U_2.add(i);
        Acc_2 = Acc_2.mul(gg_3.get(l_2-i));
    }

    public static void Revoke_2(int i)
    {
        U_2.remove(i);
        Acc_2 = Acc_2.mul(gg_3.get(l_2-i).invert());
    }

    public static Element WitGen_1(int j)
    {
        Element w = pairing.getG1().newOneElement().getImmutable();
        for(Integer i:U_1)
        {
            if(i!=j)
                w = w.mul(gg_1.get(l_1-i+j));
        }
        return w;
    }

    public static Element WitGen_2(int j)
    {
        Element w = pairing.getG2().newOneElement().getImmutable();
        for(Integer i:U_2)
        {
            if(i!=j)
                w = w.mul(gg_4.get(l_1-i+j));
        }
        return w;
    }

    public static boolean AccVerify_1(Element g_i, Element w_1)
    {
        Element left = pairing.pairing(g_i, Acc_1).mul(pairing.pairing(w_1,g_2).invert());
        Element right = eta_1;
        if(!left.isEqual(right))
            return false;
        return true;
    }

    public static boolean AccVerify_2(Element g_i, Element w_2)
    {
        Element left = pairing.pairing(Acc_2,g_i).mul(pairing.pairing(g_1,w_2).invert());
        Element right = eta_2;
        if(!left.isEqual(right))
            return false;
        return true;
    }

    public static void main(String[] args) {
        Accumulator accu = new Accumulator();
        accu.Setup(10,10);

        accu.AccGen_1(2);
        accu.AccGen_2(2);

        Element w_1 = accu.WitGen_1(2);
        Element w_2 = accu.WitGen_2(2);

        Element g_i_1 = gg_1.get(1);
        Element g_i_2 = gg_4.get(1);
        boolean rst1 = accu.AccVerify_1(g_i_1,w_1);
        boolean rst2 = accu.AccVerify_2(g_i_2,w_2);

        System.out.println(rst1);
        System.out.println(rst2);
    }


}
