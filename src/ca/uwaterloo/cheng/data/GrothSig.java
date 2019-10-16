package ca.uwaterloo.cheng.data;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import java.io.IOException;
import java.util.ArrayList;

public class GrothSig {
    public Element R, S;
    public ArrayList<Element> T;

    public GrothSig (Element _R, Element _S, ArrayList<Element> _T)
    {
        this.R = _R;
        this.S = _S;
        this.T = _T;
    }

    public ArrayList<String> toStringList()
    {
        ArrayList<String> data = new ArrayList<>();
        String str_R = Base64.encodeBytes(R.toBytes());
        data.add(str_R);
        String str_S = Base64.encodeBytes(S.toBytes());
        data.add(str_S);
        int n = T.size();
        for (int i=0;i<n;i++)
        {
            String str_T = Base64.encodeBytes(T.get(i).toBytes());
            data.add(str_T);
        }
        return data;
    }

    public static GrothSig toGrothSig(ArrayList<String> data,int _tag)
    {
        Element R = null, S = null;
        ArrayList<Element> T = new ArrayList<>();
        Pairing pairing = PairingFactory.getPairing("f.properties");
        String str_R = data.get(0);
        String str_S = data.get(1);
        if(_tag == 1)
        {
            try {
                R = pairing.getG2().newElementFromBytes(Base64.decode(str_R));
                S = pairing.getG1().newElementFromBytes(Base64.decode(str_S));
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        else if(_tag == 2)
        {
            try {
                R = pairing.getG1().newElementFromBytes(Base64.decode(str_R));
                S = pairing.getG2().newElementFromBytes(Base64.decode(str_S));
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        int n=data.size()+2;
        try {
            for (int i = 2; i < n; i++) {
                Element T_i = null;
                String str_T = data.get(1);
                if(_tag == 1)
                {
                    T_i = pairing.getG1().newElementFromBytes(Base64.decode(str_T));
                }
                else if(_tag == 2)
                {
                    T_i = pairing.getG2().newElementFromBytes(Base64.decode(str_T));
                }
                T.add(T_i);
            }
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return new GrothSig(R, S, T);
    }
}
