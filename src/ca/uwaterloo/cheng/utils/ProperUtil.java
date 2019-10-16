package ca.uwaterloo.cheng.utils;

import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ProperUtil {

    private static String FilePath = "";
    private static String FileName = "params.properties";

    public static String getConfigProperties(String key) {
        File mfile = new File(FilePath);
        if (!mfile.exists())
            mfile.mkdirs();
        File mfileName = new File(FilePath + FileName);
        if (!mfileName.exists())
            return null;
        Properties props = new Properties();
        InputStream in;
        try {
            in = new FileInputStream(FilePath + FileName);
            props.load(in);
            String value = props.getProperty(key);
            in.close();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void writeDateToLocalFile(String key, String value) {
        File mfile = new File(FilePath);
        if (!mfile.exists())
            mfile.mkdirs();
        File mfileName = new File(FilePath + FileName);
        Properties p = new Properties();
        try {
            if (!mfileName.exists())
                mfileName.createNewFile();
            InputStream in = new FileInputStream(FilePath + FileName);
            p.load(in);
            p.put(key, value);
            OutputStream fos;
            fos = new FileOutputStream(FilePath + FileName);
            p.store(fos, null);
            in.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ProperUtil.writeDateToLocalFile("test", "test");
        String tmp = ProperUtil.getConfigProperties("test");
        System.out.println(tmp);
    }

}