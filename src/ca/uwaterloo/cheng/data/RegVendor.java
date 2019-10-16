package ca.uwaterloo.cheng.data;

import ca.uwaterloo.cheng.utils.Groth;
import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

public class RegVendor {
    public GrothSig vcred;
    public Element vk, attr_1, attr_2;
    public BigInteger sk;

    public RegVendor(GrothSig _vcred, Element _vk, Element _attr_1, Element _attr_2, BigInteger _sk)
    {
        vcred = _vcred;
        vk = _vk;
        attr_1 = _attr_1;
        attr_2 = _attr_2;
        sk = _sk;
    }
}
