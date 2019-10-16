package ca.uwaterloo.cheng.data;

import it.unisa.dia.gas.jpbc.Element;
import java.math.BigInteger;

public class GrothKey {

    public BigInteger sk;
    public Element vk;

    public GrothKey (BigInteger _sk, Element _vk)
    {
        this.sk = _sk;
        this.vk = _vk;
    }

}
