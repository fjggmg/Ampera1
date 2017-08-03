package com.lifeform.main;

import com.lifeform.main.data.Options;

/**
 * Created by Bryan on 4/7/2017.
 */
public class Main {

    public static void main(String[] args)
    {

        Options o = decode(args);

        IKi main = new Ki(o);
        main.start();
    }

    public static Options decode(String[] args)
    {
        Options o = new Options();
        for(String s:args)
        {
            if(s.equals("-r")) o.relay = true;
            if(s.equals("-enableMining")) o.mining = true;
        }
        return o;
    }
}
