package ca.uwaterloo.cheng.mars;

import ca.uwaterloo.cheng.data.*;
import ca.uwaterloo.cheng.utils.Accumulator;
import ca.uwaterloo.cheng.utils.Groth;
import ca.uwaterloo.cheng.utils.OT;
import ca.uwaterloo.cheng.utils.ZeroKnowledge;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.ArrayList;

public class MARS_Main {

    public static void main(String[] args) {

        /******************Setting******************/
        double startTime;
        double endTime;
        int num_vendors = 10;
        int num_users = 1;
        int N = 10;
        boolean Modified =true;

        /******************Setup******************/

        // Trusted Authority
        startTime=System.nanoTime();
        //Params.GeneratePairing();
        Pairing pairing = PairingFactory.getPairing("f.properties");
        final int rbits = 256;
        final BigInteger order = pairing.getZr().getOrder();
        Element g_1 = Params.read_g_1().getImmutable();
        Element g_2 = Params.read_g_2().getImmutable();
        Groth.Setup(3,3);
        BigInteger gamma = Params.randomZq(rbits, order);
        Element Gamma = g_1.pow(gamma).getImmutable();
        BigInteger x_1 = Params.randomZq(rbits, order);
        BigInteger x_2 = Params.randomZq(rbits, order);
        Element h = pairing.getG2().newRandomElement().getImmutable();
        Element rho = h.pow(x_1.modInverse(order)).getImmutable();
        Element theta = h.pow(x_2.modInverse(order)).getImmutable();
        endTime=System.nanoTime();
        System.out.println("TA Setup Duration:"+(endTime-startTime)/(1000000));

        //Rating Platform
        startTime=System.nanoTime();
        BigInteger msk = Params.randomZq(rbits, order);
        Element mpk = g_2.pow(msk).getImmutable();
        Accumulator.Setup(num_vendors,num_users);
        endTime=System.nanoTime();
        System.out.println("Rating Platform Setup Duration:"+(endTime-startTime)/(1000000));

        /******************Registration******************/

        //Rating Platform -> Vendor
        startTime=System.nanoTime();
        ArrayList<RegVendor> reg_v_list = new ArrayList<>();
        for(int i=0;i<num_vendors;i++) {
            GrothKey vendor_keys = Groth.Gen_2();
            Element vendor_attr_1 = Params.Hash_1("Restaurant");
            Element vendor_attr_2 = Accumulator.gg_1.get(i);
            Accumulator.AccGen_1(i+1);
            ArrayList<Element> M = new ArrayList<>();
            M.add(vendor_keys.vk);
            M.add(vendor_attr_1);
            M.add(vendor_attr_2);
            GrothSig vcred = Groth.Sign_1(M,3, msk);
            reg_v_list.add(new RegVendor(vcred,vendor_keys.vk,vendor_attr_1,vendor_attr_2,vendor_keys.sk));
        }
        endTime=System.nanoTime();
        System.out.println("Vendor Registration Duration:"+(endTime-startTime)/(1000000*num_vendors));


        //Rating Platform -> User
        startTime=System.nanoTime();
        ArrayList<RegUser> reg_u_list = new ArrayList<>();
        for(int i=0;i<num_users;i++) {
            Element g_i = Accumulator.gg_4.get(i);
            Accumulator.AccGen_2(i+1);
            Element ID_i = g_2.pow(gamma.add(Accumulator.beta.pow(i+1)).modInverse(order)).getImmutable();
            GrothKey u_keys = Groth.Gen_1();
            reg_u_list.add(new RegUser(g_i,ID_i,u_keys.vk,u_keys.sk));
        }
        endTime=System.nanoTime();
        System.out.println("User Registration Duration:"+(endTime-startTime)/(1000000*num_users));


        /******************Online Transaction******************/


        double OT_time_user = 0;
        double OT_time_vendor = 0;

        startTime = System.nanoTime();
        ArrayList<GrothSig> tcred_list = new ArrayList<>();
        for(int j=0;j<N;j++)
        {
            RegVendor reg_v = reg_v_list.get(0);
            Element vk = reg_u_list.get(0).vk;
            Element g_i = reg_u_list.get(0).g_i;
            Element info_vendor = Params.Hash_2("Burger");
            ArrayList<Element> M = new ArrayList<>();
            M.add(vk);
            M.add(info_vendor);
            M.add(g_i);
            GrothSig tcred = Groth.Sign_2(M, 3, reg_v.sk);
            tcred_list.add(tcred);
        }
        endTime = System.nanoTime();
        OT_time_vendor = OT_time_vendor + endTime - startTime;

        if(Modified) {
            OT ot = new OT();
            ot.Setup();
            startTime = System.nanoTime();
            Element A = ot.VD_Step_1();
            endTime = System.nanoTime();
            OT_time_vendor = OT_time_vendor + endTime - startTime;
            startTime = System.nanoTime();
            Element B = ot.User_Step_1(A, 1);
            endTime = System.nanoTime();
            OT_time_user = OT_time_user + endTime - startTime;
            startTime = System.nanoTime();
            ArrayList<byte[]> enc_data = ot.VD_Step_2(A, B, tcred_list);
            endTime = System.nanoTime();
            OT_time_vendor = OT_time_vendor + endTime - startTime;
            startTime = System.nanoTime();
            GrothSig sig_test = ot.User_Step_2(enc_data, A, 1, 2);
            endTime = System.nanoTime();
            OT_time_user = OT_time_user + endTime - startTime;
        }

        System.out.println("OT Vendor Duration:"+ OT_time_vendor/1000000+"; N:"+N);
        System.out.println("OT User Duration:"+ OT_time_user/1000000+"; N:"+N);


        // Vendor
        startTime=System.nanoTime();
        ArrayList<Receipt> receipts = new ArrayList<>();
        for(int j=0;j<num_users;j++) {
            Element vk = reg_u_list.get(j).vk;
            Element g_i = reg_u_list.get(j).g_i;
            for (int i = 0; i < num_vendors; i++) {
                RegVendor reg_v = reg_v_list.get(i);
                Element info_vendor = Params.Hash_2("Burger");
                ArrayList<Element> M = new ArrayList<>();
                M.add(vk);
                M.add(info_vendor);
                M.add(g_i);
                GrothSig tcred = Groth.Sign_2(M, 3, reg_v.sk);
                receipts.add(new Receipt(reg_v.vcred, tcred, reg_v.vk, reg_v.attr_1, reg_v.attr_2, info_vendor));
            }
        }

        endTime=System.nanoTime();
        System.out.println("Transaction Duration (Vendor):"+(endTime-startTime)/(1000000*num_vendors*num_users));


        // User
        startTime=System.nanoTime();
        int num_repts = receipts.size();
        for(int j=0;j<num_users;j++) {
            Element vk = reg_u_list.get(j).vk;
            Element g_i = reg_u_list.get(j).g_i;
            byte[] lambda = Params.Hash_3("Password".getBytes());

            for (int i = 0; i < num_repts; i++) {
                GrothSig vcred = receipts.get(i).vcred;
                GrothSig tcred = receipts.get(i).tcred;

                ArrayList<Element> M_1 = new ArrayList<>();
                M_1.add(receipts.get(i).vpk);
                M_1.add(receipts.get(i).attr_1);
                M_1.add(receipts.get(i).attr_2);
                boolean rst_1 = Groth.Verify_1(vcred, M_1, 3, mpk);

                if (!rst_1)
                    System.out.println("Vendor Auth Error");

                ArrayList<Element> M_2 = new ArrayList<>();
                M_2.add(vk);
                M_2.add(receipts.get(i).info);
                M_2.add(g_i);
                boolean rst_2 = Groth.Verify_2(tcred, M_2, 3, receipts.get(i).vpk);
                if (!rst_2)
                    System.out.println("Transaction Auth Error");
            }
        }

        endTime=System.nanoTime();
        System.out.println("Transaction Duration (User):"+(endTime-startTime)/(1000000*num_repts*num_users));

        /******************Anonymous Rating******************/

        //User
        startTime=System.nanoTime();
        ArrayList<Proof> proof_list = new ArrayList<>();
        for(int j=0;j<num_users;j++) {
            Element ID_i = reg_u_list.get(j).ID_i;
            Element vk = reg_u_list.get(j).vk;
            Element g_i = reg_u_list.get(j).g_i;
            BigInteger sk = reg_u_list.get(j).sk;
            ArrayList<Element> witlist = new ArrayList<>();
            Element msg = Params.Hash_1("Rating and Review Message");
            ArrayList<Element> M_rev = new ArrayList<>();
            M_rev.add(msg);
            GrothSig u_rev = Groth.Sign_1(M_rev, 1, sk);
            for (int i = 0; i < num_repts; i++) {
                Element w_vendor = Accumulator.WitGen_1(i + 1);
                witlist.add(w_vendor);
            }
            Element w_u = Accumulator.WitGen_2(j+1);

            BigInteger f_1 = Params.randomZq(rbits, order);
            BigInteger f_2 = Params.randomZq(rbits, order);

            Element g_3i = Accumulator.gg_3.get(j);

            Element P_1 = rho.pow(f_1).getImmutable();
            Element P_2 = theta.pow(f_2).getImmutable();
            Element P_3 = ID_i.mul(h.pow(f_1.add(f_2))).getImmutable();
            Element Q_1 = g_3i.pow(f_1).getImmutable();
            Element Q_2 = g_3i.pow(f_2).getImmutable();

            ZeroKnowledge.Setup();
            //double start_time = System.nanoTime();
            Proof proof = ZeroKnowledge.Prove(rho, theta, h, Gamma, f_1, f_2, g_3i, g_i, Q_1, Q_2, P_1,
                    P_2, P_3, receipts, vk, u_rev, witlist, w_u);
            //double end_time = System.nanoTime();
            //System.out.println("prove time:"+(end_time-start_time)/1000000);
            proof_list.add(proof);
        }
        endTime=System.nanoTime();
        System.out.println("Rating Duration (User):"+(endTime-startTime)/(1000000*num_users)+"; Num of Receipts:"+num_repts);

        //Rating Platform
        startTime=System.nanoTime();
        for(int j=0;j<num_users;j++) {
            Proof proof = proof_list.get(j);
            Element msg = Params.Hash_1("Rating and Review Message");
            byte[] lambda = Params.Hash_3("Password".getBytes());
            boolean rst = ZeroKnowledge.Verify(mpk, rho, theta, h, Gamma, proof.P_1, proof.P_2, proof.P_3, proof, msg);
            if (!rst)
                System.out.println("Proof Error");
        }
        endTime=System.nanoTime();
        System.out.println("Rating Duration (Rating Platform):"+(endTime-startTime)/(1000000*num_users)+"; Num of Receipts:"+num_repts);

        /******************Revocation******************/
        startTime=System.nanoTime();
        for(int j=0;j<num_vendors;j++) {
            Accumulator.Revoke_1(j+1);
        }
        endTime=System.nanoTime();
        System.out.println("Vendor Revocation Duration:"+(endTime-startTime)/(1000000*num_vendors));

        startTime=System.nanoTime();
        for(int j=0;j<num_users;j++) {
            Accumulator.Revoke_2(j+1);
        }
        endTime=System.nanoTime();
        System.out.println("User Revocation Duration:"+(endTime-startTime)/(1000000*num_users));

        /******************Updating******************/


        /******************Tracing******************/
        startTime = System.nanoTime();
        for(int j=0;j<num_users;j++) {
            Element ID_i = reg_u_list.get(j).ID_i;
            Proof proof = proof_list.get(j);
            Element ID = proof.P_3.mul(proof.P_1.pow(x_1).mul(proof.P_2.pow(x_2)).invert());
            if (!ID.isEqual(ID_i))
                System.out.println("Identity Recover Error");
        }
        endTime=System.nanoTime();
        System.out.println("Tracing Duration (TA):"+(endTime-startTime)/(1000000*num_users));

        System.out.println("Test Finish");
    }
}
