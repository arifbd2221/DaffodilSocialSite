package com.socialsite.daffodilvarsity.utils;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;


public class Utils {

    public static final String URL_STORAGE_REFERENCE = "gs://daffodilsocialsite.appspot.com";
    public static final String FOLDER_STORAGE_IMG = "images";


    public static int getDisplayWidth(Context context) {
        return getSize(context).x;
    }

    public static int getDisplayHeight(Context context) {
        return getSize(context).y;
    }

    private static Point getSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }


    public static void initToast(Context c, String message){
        Toast.makeText(c,message,Toast.LENGTH_SHORT).show();
    }

    public  static boolean verificaConexao(Context context) {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        conectado = conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected();
        return conectado;
    }

    public static String local(String latitudeFinal,String longitudeFinal){
        return "https://maps.googleapis.com/maps/api/staticmap?center="+latitudeFinal+","+longitudeFinal+"&zoom=18&size=280x280&markers=color:red|"+latitudeFinal+","+longitudeFinal;
    }


}
