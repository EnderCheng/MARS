package ca.uwaterloo.cheng.data;

import it.unisa.dia.gas.jpbc.Element;

public class Receipt {
    public GrothSig vcred, tcred;
    public Element vpk, attr_1, attr_2, info;

    public Receipt(GrothSig _vcred, GrothSig _tcred, Element _vpk, Element _attr_1, Element _attr_2, Element _info)
    {
        vcred = _vcred;
        tcred = _tcred;
        vpk = _vpk;
        attr_1 = _attr_1;
        attr_2 = _attr_2;
        info = _info;
    }
}
