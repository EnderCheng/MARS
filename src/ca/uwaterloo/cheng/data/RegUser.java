package ca.uwaterloo.cheng.data;

import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

public class RegUser {
    public Element g_i, ID_i, vk;
    public BigInteger sk;

    public RegUser(Element _g_i, Element _ID_i, Element _vk, BigInteger _sk)
    {
        g_i = _g_i;
        ID_i = _ID_i;
        vk = _vk;
        sk = _sk;
    }
}
