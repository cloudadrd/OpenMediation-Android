//package com.nbmediation.sdk.utils;
//
//import android.content.Context;
//
///**
// * Created by jiantao.tu on 2020/10/26.
// */
//public class OaidHelper {
//    private static String sOaid = "";
//
//    public static String getOaid() {
//        return sOaid;
//    }
//
//    public static void initOaidServer(Context context) {
//        try {
//            Class.forName("com.bun.miitmdid.core.MdidSdkHelper");
//        } catch (ClassNotFoundException e) {
//            return;
//        }
//        com.bun.miitmdid.core.MdidSdkHelper.InitSdk(context, true, new com.bun.miitmdid.core.IIdentifierListener() {
//            @Override
//            public void OnSupport(boolean b, com.bun.miitmdid.supplier.IdSupplier idSupplier) {
//                if (idSupplier != null && idSupplier.isSupported()) {
//                    sOaid = idSupplier.getOAID();
//                    DeveloperLog.LogD("oaid : " + sOaid);
//                }
//            }
//        });
//    }
//}