package com.nbmediation.sdk.utils;

import com.nbmediation.sdk.mediation.MediationInfo;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static android.util.Base64.*;

/**
 * Created by jiantao.tu on 2020/5/12.
 */
public class AdapterUtilTest {

    @Test
    public void getAdapterNameTest() {
        String str = getAdapterName(MediationInfo.MEDIATION_NAME_3);
        System.out.println(str);
    }

    public String getAdapterName(String platName) {
//        try {
//            return new String(decode(platName, NO_WRAP), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return null;
//        }
        return null;
    }
}
