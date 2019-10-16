package ca.uwaterloo.cheng.data;

import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Proof {
    public Element P_1, P_2, P_3, e_1, e_2, e_3, e_4, e_p_1,
            e_p_2, e_a_1, e_a_2, t_g_3i, t_g_4i,t_Q_1, t_Q_2, A_1, A_2, t_pvk, u_1, u_2, U_R, t_S, t_T, e_wit_u, t_wit_u;
    public BigInteger s_f_1, s_f_2, s_x;
    public ArrayList<HashMap<String, Element>> ReptProof;

    public Proof(Element _P_1, Element _P_2, Element _P_3, Element _e_1, Element _e_2, Element _e_3, Element _e_4,
                 Element _e_p_1, Element _e_p_2, Element _e_a_1, Element _e_a_2,
                 BigInteger _s_f_1, BigInteger _s_f_2, Element _t_g_3i, Element _t_g_4i,
                 Element _t_Q_1, Element _t_Q_2, Element _A_1, Element _A_2, Element _t_pvk,
                 ArrayList<HashMap<String, Element>> _ReptProof, Element _u_1, Element _u_2, Element _U_R,
                 Element _t_S, Element _t_T, Element _e_wit_u, Element _t_wit_u, BigInteger _s_x)
    {
        P_1 = _P_1;
        P_2 = _P_2;
        P_3 = _P_3;
        e_1 = _e_1;
        e_2 = _e_2;
        e_3 = _e_3;
        e_4 = _e_4;
        e_p_1 = _e_p_1;
        e_p_2 = _e_p_2;
        e_a_1 = _e_a_1;
        e_a_2 = _e_a_2;
        s_f_1 = _s_f_1;
        s_f_2 = _s_f_2;
        t_g_3i = _t_g_3i;
        t_g_4i = _t_g_4i;
        t_Q_1 = _t_Q_1;
        t_Q_2 = _t_Q_2;
        A_1 = _A_1;
        A_2 = _A_2;
        t_pvk = _t_pvk;
        ReptProof = _ReptProof;
        u_1 = _u_1;
        u_2 = _u_2;
        U_R = _U_R;
        t_S = _t_S;
        t_T = _t_T;
        e_wit_u = _e_wit_u;
        t_wit_u = _t_wit_u;
        s_x = _s_x;
    }

}
