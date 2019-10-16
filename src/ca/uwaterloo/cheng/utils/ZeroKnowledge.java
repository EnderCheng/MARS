package ca.uwaterloo.cheng.utils;

import ca.uwaterloo.cheng.data.GrothKey;
import ca.uwaterloo.cheng.data.GrothSig;
import ca.uwaterloo.cheng.data.Proof;
import ca.uwaterloo.cheng.data.Receipt;
import ca.uwaterloo.cheng.mars.Params;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class ZeroKnowledge {

    private static Pairing pairing = PairingFactory.getPairing("f.properties");
    private final static int rbits = 256;
    private final static BigInteger order = pairing.getZr().getOrder();
    private static Element g_1, g_2;
    private static boolean TAG = false;

    public ZeroKnowledge(){

    }

    public static void Setup(){
        if(TAG == false)
        {
            g_1 = Params.read_g_1();
            g_2 = Params.read_g_2();
            TAG = true;
        }
    }

    public static Proof Prove(Element rho, Element theta, Element h, Element Gamma, BigInteger f_1, BigInteger f_2, Element g_3i,
                              Element g_4i, Element Q_1, Element Q_2, Element P_1, Element P_2,
                              Element P_3, ArrayList<Receipt> receipts, Element pvk, GrothSig sig,
                              ArrayList<Element> witlist, Element wit_u)
    {
        BigInteger x = Params.randomZq(rbits, order);
        BigInteger x_tmp = Params.randomZq(rbits,order);

        Element Ginv = Gamma.invert().getImmutable();
        Element A_1 = Ginv.pow(f_1).getImmutable();
        Element A_2 = Ginv.pow(f_2).getImmutable();

        BigInteger f_1_tmp = Params.randomZq(rbits, order);
        BigInteger f_2_tmp = Params.randomZq(rbits, order);
        Element g_3i_tmp = pairing.getG1().newRandomElement().getImmutable();
        Element g_4i_tmp = pairing.getG2().newRandomElement().getImmutable();
        Element Q_1_tmp = pairing.getG1().newRandomElement().getImmutable();
        Element Q_2_tmp = pairing.getG1().newRandomElement().getImmutable();
        Element pvk_tmp = pairing.getG2().newRandomElement().getImmutable();

        Element e_p_1 = rho.pow(f_1_tmp).getImmutable();
        Element e_p_2 = theta.pow(f_2_tmp).getImmutable();
        Element e_a_1 = Ginv.pow(f_1_tmp).getImmutable();
        Element e_a_2 = Ginv.pow(f_2_tmp).getImmutable();

        Element e_1 = pairing.pairing(g_3i_tmp, P_1).mul(pairing.pairing(Q_1_tmp,rho).invert()).getImmutable();
        Element e_2 = pairing.pairing(g_3i_tmp, P_2).mul(pairing.pairing(Q_2_tmp,theta).invert()).getImmutable();
        Element e_3 = pairing.pairing(g_3i_tmp, P_3)
                .mul(pairing.pairing(Q_1_tmp.mul(Q_2_tmp),h).invert()).getImmutable();
        Element e_4 = pairing.pairing(g_3i_tmp, g_2)
                .mul(pairing.pairing(g_1,g_4i_tmp).invert()).getImmutable();

        byte[] input = BufferUtils.byteMergerAll(e_p_1.toBytes(),e_p_2.toBytes(),e_a_1.toBytes(),
                e_a_2.toBytes(),e_1.toBytes(),e_2.toBytes(),e_3.toBytes(),e_4.toBytes());

        BigInteger ch = Params.Hash(input).mod(order);
        BigInteger s_f_1 = f_1_tmp.add(ch.multiply(f_1));
        BigInteger s_f_2 = f_2_tmp.add(ch.multiply(f_2));
        BigInteger s_x = x_tmp.add(ch.multiply(x));

        Element t_g_3i = g_3i_tmp.mul(g_3i.pow(ch)).getImmutable();
        Element t_g_4i = g_4i_tmp.mul(g_4i.pow(ch)).getImmutable();
        Element t_pvk = pvk_tmp.mul(pvk.pow(ch)).getImmutable();
        Element t_Q_1 = Q_1_tmp.mul(Q_1.pow(ch)).getImmutable();
        Element t_Q_2 = Q_2_tmp.mul(Q_2.pow(ch)).getImmutable();

        int num  = receipts.size();
        ArrayList<HashMap<String, Element>> ReptProof = new ArrayList<>();
        for (int k = 0; k <num; k++)
        {
            GrothSig vcred = Groth.Randomize(receipts.get(k).vcred);
            GrothSig tcred = Groth.Randomize(receipts.get(k).tcred);

            Element vpk = receipts.get(k).vpk;
            Element attr_1 = receipts.get(k).attr_1;
            Element attr_2 = receipts.get(k).attr_2;
            Element info = receipts.get(k).info;

            Element vcred_R = vcred.R.getImmutable();
            Element vcred_S = vcred.S.getImmutable();

            Element tcred_R = tcred.R.getImmutable();
            Element tcred_S = tcred.S.getImmutable();

            Element tcred_R_x = tcred_R.pow(x);
            Element tcred_R_x_tmp = tcred_R.pow(x_tmp);

            Element vcred_T_1 = vcred.T.get(0);
            Element vcred_T_2 = vcred.T.get(1);
            Element vcred_T_3 = vcred.T.get(2);

            Element tcred_T_1 = tcred.T.get(0);
            Element tcred_T_2 = tcred.T.get(1);
            Element tcred_T_3 = tcred.T.get(2);

            Element vcred_S_tmp = pairing.getG1().newRandomElement().getImmutable();
            Element vcred_T_1_tmp = pairing.getG1().newRandomElement().getImmutable();
            Element vcred_T_2_tmp = pairing.getG1().newRandomElement().getImmutable();
            Element vcred_T_3_tmp = pairing.getG1().newRandomElement().getImmutable();

            Element tcred_S_tmp = pairing.getG2().newRandomElement().getImmutable();
            Element tcred_T_1_tmp = pairing.getG2().newRandomElement().getImmutable();
            Element tcred_T_2_tmp = pairing.getG2().newRandomElement().getImmutable();
            Element tcred_T_3_tmp = pairing.getG2().newRandomElement().getImmutable();

            Element vpk_tmp = pairing.getG1().newRandomElement().getImmutable();
            Element attr_1_tmp = pairing.getG1().newRandomElement().getImmutable();
            Element attr_2_tmp = pairing.getG1().newRandomElement().getImmutable();
            Element info_tmp = pairing.getG2().newRandomElement().getImmutable();

            Element c_1 = pairing.pairing(vcred_S_tmp, vcred_R).getImmutable();
            Element c_2 = pairing.pairing(vcred_T_1_tmp, vcred_R).mul(pairing.pairing(vpk_tmp,g_2).invert()).getImmutable();
            Element c_3 = pairing.pairing(vcred_T_2_tmp, vcred_R).mul(pairing.pairing(attr_1_tmp,g_2).invert()).getImmutable();
            Element c_4 = pairing.pairing(vcred_T_3_tmp, vcred_R).mul(pairing.pairing(attr_2_tmp,g_2).invert()).getImmutable();

            Element y_2_1 = Groth.y_2.get(0);
            Element y_2_2 = Groth.y_2.get(1);
            Element y_2_3 = Groth.y_2.get(2);


            Element c_10= pairing.pairing(tcred_R_x, tcred_S).getImmutable();
            Element c_11 = pairing.pairing(tcred_R_x, tcred_S_tmp).getImmutable();

            Element c_5 = pairing.pairing(tcred_R, tcred_S_tmp).mul((pairing.pairing(vpk_tmp, g_2).invert())).getImmutable();
            Element c_6 = pairing.pairing(tcred_R, tcred_T_1_tmp).mul(pairing.pairing(vpk_tmp, y_2_1).invert()).mul(pairing.pairing(g_1,pvk_tmp).invert()).getImmutable();
            Element c_7 = pairing.pairing(tcred_R, tcred_T_2_tmp).mul(pairing.pairing(vpk_tmp, y_2_2).invert()).mul(pairing.pairing(g_1,info_tmp).invert()).getImmutable();
            Element c_8 = pairing.pairing(tcred_R, tcred_T_3_tmp).mul(pairing.pairing(vpk_tmp, y_2_3).invert()).mul(pairing.pairing(g_1, g_4i_tmp).invert()).getImmutable();

            Element t_1 = vcred_S_tmp.mul(vcred_S.pow(ch)).getImmutable();
            Element t_2 = vcred_T_1_tmp.mul(vcred_T_1.pow(ch)).getImmutable();
            Element t_3 = vcred_T_2_tmp.mul(vcred_T_2.pow(ch)).getImmutable();
            Element t_4 = vcred_T_3_tmp.mul(vcred_T_3.pow(ch)).getImmutable();

            Element t_5 = tcred_S_tmp.mul(tcred_S.pow(ch)).getImmutable();
            Element t_6 = tcred_T_1_tmp.mul(tcred_T_1.pow(ch)).getImmutable();
            Element t_7 = tcred_T_2_tmp.mul(tcred_T_2.pow(ch)).getImmutable();
            Element t_8 = tcred_T_3_tmp.mul(tcred_T_3.pow(ch)).getImmutable();

            Element t_9 = vpk_tmp.mul(vpk.pow(ch)).getImmutable();
            Element t_10 = attr_1_tmp.mul(attr_1.pow(ch)).getImmutable();
            Element t_11 = attr_2_tmp.mul(attr_2.pow(ch)).getImmutable();
            Element t_12 = info_tmp.mul(info.pow(ch)).getImmutable();

            HashMap<String, Element> map = new HashMap();

            map.put("c_1", c_1);
            map.put("c_2", c_2);
            map.put("c_3", c_3);
            map.put("c_4", c_4);
            map.put("c_5", c_5);
            map.put("c_6", c_6);
            map.put("c_7", c_7);
            map.put("c_8", c_8);
            map.put("c_10",c_10);
            map.put("c_11",c_11);

            map.put("t_1", t_1);
            map.put("t_2", t_2);
            map.put("t_3", t_3);
            map.put("t_4", t_4);
            map.put("t_5", t_5);
            map.put("t_6", t_6);
            map.put("t_7", t_7);
            map.put("t_8", t_8);
            map.put("t_9", t_9);
            map.put("t_10", t_10);
            map.put("t_11", t_11);
            map.put("t_12", t_12);

            map.put("r_v", vcred_R);
            map.put("r_t", tcred_R);
            map.put("r_t_x", tcred_R_x);
            map.put("r_t_x_tmp", tcred_R_x_tmp);

            Element Acc_1 = Accumulator.Acc_1;
            Element wit = witlist.get(k);
            Element wit_tmp = pairing.getG1().newRandomElement().getImmutable();
            Element e_wit = pairing.pairing(attr_2_tmp,Acc_1).mul(pairing.pairing(wit_tmp,g_2).invert()).getImmutable();
            Element t_wit = wit_tmp.mul(wit.pow(ch)).getImmutable();

            map.put("e_wit", e_wit);
            map.put("t_wit", t_wit);

            ReptProof.add(map);
        }

        GrothSig u_sig = Groth.Randomize(sig);
        Element u_S_tmp = pairing.getG1().newRandomElement().getImmutable();
        Element u_T_tmp = pairing.getG1().newRandomElement().getImmutable();

        Element y_1_1 = Groth.y_1.get(0);

        Element t_S = u_S_tmp.mul(u_sig.S.pow(ch)).getImmutable();
        Element t_T = u_T_tmp.mul(u_sig.T.get(0).pow(ch)).getImmutable();

        Element u_1 = pairing.pairing(u_S_tmp, u_sig.R).mul(pairing.pairing(g_1,pvk_tmp).invert()).getImmutable();
        Element u_2 = pairing.pairing(u_T_tmp, u_sig.R).mul(pairing.pairing(y_1_1,pvk_tmp).invert()).getImmutable();

        Element Acc_2 = Accumulator.Acc_2;
        Element wit_tmp = pairing.getG2().newRandomElement().getImmutable();
        Element e_wit = pairing.pairing(Acc_2,g_4i_tmp).mul(pairing.pairing(g_1,wit_tmp).invert()).getImmutable();
        Element t_wit = wit_tmp.mul(wit_u.pow(ch)).getImmutable();


        Proof proof = new Proof(P_1, P_2, P_3, e_1, e_2, e_3, e_4, e_p_1, e_p_2, e_a_1, e_a_2,
                s_f_1,s_f_2,t_g_3i,t_g_4i,t_Q_1,t_Q_2, A_1, A_2,t_pvk, ReptProof,u_1,u_2,u_sig.R,t_S,t_T,e_wit,t_wit,s_x);
        return proof;

    }

    public static boolean Verify(Element mpk, Element rho, Element theta, Element h, Element Gamma,
                                 Element P_1, Element P_2, Element P_3, Proof proof, Element msg)
    {
        BigInteger s_f_1 = proof.s_f_1;
        BigInteger s_f_2 = proof.s_f_2;
        BigInteger s_x = proof.s_x;

        Element t_g_3i = proof.t_g_3i;
        Element t_g_4i = proof.t_g_4i;
        Element t_pvk = proof.t_pvk;
        Element t_Q_1 = proof.t_Q_1;
        Element t_Q_2 = proof.t_Q_2;

        Element e_p_1 = proof.e_p_1;
        Element e_p_2 = proof.e_p_2;
        Element e_a_1 = proof.e_a_1;
        Element e_a_2 = proof.e_a_2;
        Element e_1 = proof.e_1;
        Element e_2 = proof.e_2;
        Element e_3 = proof.e_3;
        Element e_4 = proof.e_4;

        Element A_1 = proof.A_1;
        Element A_2 = proof.A_2;

        ArrayList<HashMap<String, Element>> ReptProof = proof.ReptProof;

        byte[] input = BufferUtils.byteMergerAll(e_p_1.toBytes(),e_p_2.toBytes(),e_a_1.toBytes(),
                e_a_2.toBytes(),e_1.toBytes(),e_2.toBytes(),e_3.toBytes(),e_4.toBytes());

        BigInteger ch = Params.Hash(input).mod(order);

        //Statement 1

        Element left = rho.pow(s_f_1);
        Element right = e_p_1.mul(P_1.pow(ch));

        if(!left.isEqual(right)) {
            System.out.println("1");
            return false;
        }

        left = theta.pow(s_f_2);
        right = e_p_2.mul(P_2.pow(ch));

        if(!left.isEqual(right)){
            System.out.println("2");
            return false;
        }

        Element OneGt = pairing.getGT().newOneElement().getImmutable();

        left = pairing.pairing(t_g_3i, P_1).mul(pairing.pairing(t_Q_1,rho).invert());
        right = e_1;

        if(!left.isEqual(right)) {
            System.out.println("3");
            return false;
        }

        left = pairing.pairing(t_g_3i, P_2).mul(pairing.pairing(t_Q_2,theta).invert());
        right = e_2;


        if(!left.isEqual(right)) {
            System.out.println("4");
            return false;
        }

        Element Ginv = Gamma.invert().getImmutable();

        left = Ginv.pow(s_f_1);
        right = e_a_1.mul(A_1.pow(ch));

        if(!left.isEqual(right)){
            System.out.println("5");
            return false;
        }

        left = Ginv.pow(s_f_2);
        right = e_a_2.mul(A_2.pow(ch));

        if(!left.isEqual(right)){
            System.out.println("6");
            return false;
        }

        Element tmp = pairing.pairing(g_1, g_2).mul(pairing.pairing(Gamma,P_3).invert())
                .mul(pairing.pairing(A_1,h).invert()).mul(pairing.pairing(A_2,h).invert()).getImmutable();
        left = pairing.pairing(t_g_3i,P_3).mul(pairing.pairing(t_Q_1,h).invert())
                .mul(pairing.pairing(t_Q_2,h).invert()).mul(tmp.invert().pow(ch));
        right = e_3;

        if(!left.isEqual(right)){
            System.out.println("7");
            return false;
        }

        //Statement 4

        left = pairing.pairing(t_g_3i, g_2).mul(pairing.pairing(g_1,t_g_4i).invert());
        right = e_4;

        if(!left.isEqual(right)) {
            System.out.println("8");
            return false;
        }

        //Statement 3 and 5

        Element y_1_1 = Groth.y_1.get(0);
        Element y_1_2 = Groth.y_1.get(1);
        Element y_1_3 = Groth.y_1.get(2);

        Element y_2_1 = Groth.y_2.get(0);
        Element y_2_2 = Groth.y_2.get(1);
        Element y_2_3 = Groth.y_2.get(2);

        Element tmp_2 = pairing.pairing(y_1_1,mpk).getImmutable();
        Element tmp_3 = pairing.pairing(y_1_2,mpk).getImmutable();
        Element tmp_4 = pairing.pairing(y_1_3,mpk).getImmutable();
        Element tmp_5= pairing.pairing(g_1, y_2_1).getImmutable();
        Element tmp_6 = pairing.pairing(y_1_1, g_2).getImmutable();
        Element tmp_1 = tmp_6.mul(pairing.pairing(g_1,mpk)).getImmutable();

        int num = ReptProof.size();
        for(int k=0; k<num;k++)
        {
            HashMap<String, Element> map = ReptProof.get(k);
            Element c_1 = map.get("c_1");
            Element c_2 = map.get("c_2");
            Element c_3 = map.get("c_3");
            Element c_4 = map.get("c_4");
            Element c_5 = map.get("c_5");
            Element c_6 = map.get("c_6");
            Element c_7 = map.get("c_7");
            Element c_8 = map.get("c_8");
            Element c_10 = map.get("c_10");
            Element c_11 = map.get("c_11");

            Element t_1 = map.get("t_1");
            Element t_2 = map.get("t_2");
            Element t_3 = map.get("t_3");
            Element t_4 = map.get("t_4");
            Element t_5 = map.get("t_5");
            Element t_6 = map.get("t_6");
            Element t_7 = map.get("t_7");
            Element t_8 = map.get("t_8");
            Element t_9 = map.get("t_9");
            Element t_10 = map.get("t_10");
            Element t_11 = map.get("t_11");
            Element t_12 = map.get("t_12");

            Element r_v = map.get("r_v");
            Element r_t = map.get("r_t");

            Element e_wit_v = map.get("e_wit");
            Element t_wit_v = map.get("t_wit");

            Element t_R_x = map.get("r_t_x");
            Element t_R_x_tmp= map.get("r_t_x_tmp");


            left = r_t.pow(s_x);
            right = t_R_x_tmp.mul(t_R_x.pow(ch));


            if(!left.isEqual(right)){
                System.out.println("error R");
                return false;
            }

            left = pairing.pairing(t_R_x, t_5).mul(c_10.invert().pow(ch));
            right = c_11;
            if(!left.isEqual(right)) {
                System.out.println("error e(R,S)");
                return false;
            }

            left = pairing.pairing(t_11,Accumulator.Acc_1).mul(pairing.pairing(t_wit_v,g_2).invert())
                    .mul(Accumulator.eta_1.invert().pow(ch));
            right= e_wit_v;

            if(!left.isEqual(right)) {
                System.out.println("wit error");
                return false;
            }

            left = pairing.pairing(t_1,r_v).mul(tmp_1.invert().pow(ch));
            right = c_1;

            if(!left.isEqual(right)) {
                System.out.println("9");
                return false;
            }

            left = pairing.pairing(t_2,r_v).mul(pairing.pairing(t_9,g_2).invert()).mul(tmp_2.invert().pow(ch));
            right = c_2;

            if(!left.isEqual(right)) {
                System.out.println("10");
                return false;
            }

            left = pairing.pairing(t_3,r_v).mul(pairing.pairing(t_10,g_2).invert()).mul(tmp_3.invert().pow(ch));
            right = c_3;

            if(!left.isEqual(right)) {
                System.out.println("11");
                return false;
            }

            left = pairing.pairing(t_4,r_v).mul(pairing.pairing(t_11,g_2).invert()).mul(tmp_4.invert().pow(ch));
            right = c_4;

            if(!left.isEqual(right)) {
                System.out.println("12");
                return false;
            }

            left = pairing.pairing(r_t, t_5).mul(pairing.pairing(t_9,g_2).invert()).mul(tmp_5.invert().pow(ch));
            right = c_5;

            if(!left.isEqual(right)) {
                System.out.println("13");
                return false;
            }

            left = pairing.pairing(r_t, t_6).mul(pairing.pairing(t_9,y_2_1).invert()).mul(pairing.pairing(g_1,t_pvk).invert());
            right = c_6;

            if(!left.isEqual(right)) {
                System.out.println("14");
                return false;
            }

            left = pairing.pairing(r_t, t_7).mul(pairing.pairing(t_9,y_2_2).invert()).mul(pairing.pairing(g_1,t_12).invert());
            right = c_7;

            if(!left.isEqual(right)) {
                System.out.println("15");
                return false;
            }


            left = pairing.pairing(r_t, t_8).mul(pairing.pairing(t_9,y_2_3).invert()).mul(pairing.pairing(g_1,t_g_4i).invert());
            right = c_8;

            if(!left.isEqual(right)) {
                System.out.println("16");
                return false;
            }

        }

        //Statement 7

        Element u_1 = proof.u_1;
        Element u_2 = proof.u_2;
        Element U_R = proof.U_R;

        Element t_S = proof.t_S;
        Element t_T = proof.t_T;

        left = pairing.pairing(t_S,U_R).mul(pairing.pairing(g_1,t_pvk).invert()).mul(tmp_6.invert().pow(ch));
        right = u_1;

        if(!left.isEqual(right)) {
            System.out.println("17");
            return false;
        }

        tmp = pairing.pairing(msg, g_2);
        left = pairing.pairing(t_T,U_R).mul(pairing.pairing(y_1_1,t_pvk).invert())
                .mul(tmp.invert().pow(ch));
        right = u_2;

        if(!left.isEqual(right)) {
            System.out.println("18");
            return false;
        }


        //Statement 6

        for(int k_1=0; k_1<num;k_1++)
        {
            for(int k_2=k_1+1; k_2<num;k_2++)
            {
                if(k_1 != k_2)
                {
                    HashMap<String, Element> map_1 = ReptProof.get(k_1);
                    HashMap<String, Element> map_2 = ReptProof.get(k_2);
                    Element c_k_1 = map_1.get("c_10");
                    Element c_k_2 = map_2.get("c_10");

                    if(c_k_1.isEqual(c_k_2)) {
                        System.out.println("19");
                        return false;
                    }
                }
            }
        }

        //Statement 2
        Element e_wit_u = proof.e_wit_u;
        Element t_wit_u = proof.t_wit_u;

        left = pairing.pairing(Accumulator.Acc_2,t_g_4i).mul(pairing.pairing(g_1,t_wit_u).invert())
                .mul(Accumulator.eta_2.invert().pow(ch));
        right= e_wit_u;

        if(!left.isEqual(right)) {
            System.out.println("wit error 2");
            return false;
        }

        return true;

    }

    public static void main(String[] args) {
        ZeroKnowledge.Setup();
        Groth.Setup(3,3);
        Accumulator.Setup(10,10);
        BigInteger gamma = Params.randomZq(rbits, order);
        Element Gamma = g_1.pow(gamma).getImmutable();
        BigInteger msk = Params.randomZq(rbits, order);
        Element mpk = g_2.pow(msk).getImmutable();
        BigInteger x_1 = Params.randomZq(rbits, order);
        BigInteger x_2 = Params.randomZq(rbits, order);
        Element h = pairing.getG2().newRandomElement().getImmutable();
        Element rho = h.pow(x_1.modInverse(order)).getImmutable();
        Element theta = h.pow(x_2.modInverse(order)).getImmutable();
        BigInteger f_1 =  Params.randomZq(rbits, order);
        BigInteger f_2 =  Params.randomZq(rbits, order);
        BigInteger beta = Accumulator.beta;
        Element g_3i = g_1.pow(beta.pow(5)).getImmutable();
        Element g_4i = g_2.pow(beta.pow(5)).getImmutable();
        Accumulator.AccGen_2(5);
        Element ID_i = g_2.pow(gamma.add(beta.pow(5)).modInverse(order)).getImmutable();

        Element P_1  = rho.pow(f_1).getImmutable();
        Element P_2  = theta.pow(f_2).getImmutable();
        Element P_3 = ID_i.mul(h.pow(f_1.add(f_2))).getImmutable();
        Element Q_1 = g_3i.pow(f_1).getImmutable();
        Element Q_2 = g_3i.pow(f_2).getImmutable();

        //vendor_1
        GrothKey v_1_keys = Groth.Gen_2();
        Element v_1_attr_1 = Params.Hash_1("Restaurant");
        Element v_1_attr_2 = Accumulator.gg_1.get(0);
        Accumulator.AccGen_1(1);
        ArrayList<Element> M_1 = new ArrayList<>();
        M_1.add(v_1_keys.vk);
        M_1.add(v_1_attr_1);
        M_1.add(v_1_attr_2);
        GrothSig vcred_1 = Groth.Sign_1(M_1,3, msk);

        //vendor_2
        GrothKey v_2_keys = Groth.Gen_2();
        Element v_2_attr_1 = Params.Hash_1("Restaurant");
        Element v_2_attr_2 = Accumulator.gg_1.get(1);
        Accumulator.AccGen_1(2);
        ArrayList<Element> M_2 = new ArrayList<>();
        M_2.add(v_2_keys.vk);
        M_2.add(v_2_attr_1);
        M_2.add(v_2_attr_2);
        GrothSig vcred_2 = Groth.Sign_1(M_2,3, msk);

        //user
        GrothKey u_keys = Groth.Gen_1();

        //vendor_1_user
        Element info_v_1 = Params.Hash_2("Burger");
        ArrayList<Element> M_u_1 = new ArrayList<>();
        M_u_1.add(u_keys.vk);
        M_u_1.add(info_v_1);
        M_u_1.add(g_4i);
        GrothSig tcred_1 = Groth.Sign_2(M_u_1,3, v_1_keys.sk);

        //vendor_2_user
        Element info_v_2 = Params.Hash_2("Pizza");
        ArrayList<Element> M_u_2 = new ArrayList<>();
        M_u_2.add(u_keys.vk);
        M_u_2.add(info_v_2);
        M_u_2.add(g_4i);
        GrothSig tcred_2 = Groth.Sign_2(M_u_2,3, v_2_keys.sk);

        Receipt receipt_1 = new Receipt(vcred_1,tcred_1,v_1_keys.vk,v_1_attr_1,v_1_attr_2,info_v_1);
        Receipt receipt_2 = new Receipt(vcred_2,tcred_2,v_2_keys.vk,v_2_attr_1,v_2_attr_2,info_v_2);

        ArrayList<Receipt>  receipts = new ArrayList<>();
        receipts.add(receipt_1);
        receipts.add(receipt_2);

        Element msg = Params.Hash_1("test");
        ArrayList<Element> M_rev = new ArrayList<>();
        M_rev.add(msg);
        GrothSig u_rev = Groth.Sign_1(M_rev,1,u_keys.sk);

        Element w_v_1 = Accumulator.WitGen_1(1);
        Element w_v_2 = Accumulator.WitGen_1(2);

        ArrayList<Element> witlist = new ArrayList<>();
        witlist.add(w_v_1);
        witlist.add(w_v_2);

        Element w_u = Accumulator.WitGen_2(5);


//        Element left = pairing.pairing(Gamma.mul(g_3i), ID_i);
//        Element right = pairing.pairing(g_1,g_2);
//        System.out.println(left.isEqual(right));
//
//        //test
//        left = pairing.pairing(g_3i,P_1).mul(pairing.pairing(Q_1,rho).invert());
//        right = pairing.getGT().newOneElement();
//        System.out.println(left.isEqual(right));

        //test proof
        Proof proof = ZeroKnowledge.Prove(rho,theta,h,Gamma,f_1,f_2,g_3i, g_4i, Q_1, Q_2, P_1,
                P_2, P_3,receipts,u_keys.vk,u_rev,witlist,w_u);
        boolean rst = ZeroKnowledge.Verify(mpk,rho,theta,h,Gamma, P_1, P_2, P_3, proof, msg);
        System.out.println("verification:"+rst);

    }
}
