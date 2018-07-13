package com.ampex.main;

import com.ampex.amperabase.KeyType;
import com.ampex.amperabase.Options;

/**
 * Created by Bryan on 4/7/2017.
 * Copyright (C) 2017  Ampex Technologies LLC

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see http://www.gnu.org/licenses/.

 *
 * ANYWHERE THE AFOREMENTIONED LICENSE IS NOT FOUND IN THE SOURCE CODE CREATED BY BRYAN SHARPE
 * IS STILL COVERED UNDER GPL V3 EXCLUDING ANY GUI RELATED FILES
 * SEE LICENSE.txt FOR DETAILS.
 *
 * @author Bryan
 */
public class Main {

    public static void main(String[] args)
    {
        Options o = decode(args);
        IKi main = new Ki(o);
        Ki.instance = main;
        main.start();
    }

    /**
     * Sets up Options for launching the god object
     *
     * @param args args passed into the program
     * @return Options object properly configured
     * @see Options
     */
    public static Options decode(String[] args) {
        Options o = new Options();

        for (String s : args) {

            //if (s.startsWith("-ur")) o.relayToUse = Integer.parseInt(s.replaceFirst("-ur", ""));
            if (s.equals("-testnet")) o.testNet = true;
            if (s.equals("-nogui")) o.nogui = true;
            if (s.equals("-bd")) o.bDebug = true;
            if (s.equals("-md")) o.mDebug = true;
            if (s.equals("-dump")) o.dump = true;
            if (s.equals("-rebuild")) o.rebuild = true;
            if (s.equals("-pd")) o.pDebug = true;
            if (s.equals("-full")) o.lite = false;
            if (s.equals("-td")) o.tDebug = true;
            if (s.equals("-r")) {
                o.relay = true;
                o.lite = false;
            }
            if (s.equals("-pr")) {
                o.poolRelay = true;
                o.lite = false;
            }
            if (s.equals("-pool")) {
                o.pool = true;
                o.lite = true;
            }
            if (s.equals("-benchmark")) {
                o.benchmark = true;
            }
            if (s.equals("--imps")) {
                o.useImpossible = true;
            }
            if (s.equals("--wcs")) {
                o.useWorstCase = true;
            }
            if (s.startsWith("--trans")) {
                String reg = s.replace("--trans","");

                try{
                    o.numberOfTransactions = Integer.parseInt(reg);
                }catch (NumberFormatException e)
                {
                    System.out.println("Could not parse number of transactions to use for benchmark, defaulting to 50k");
                }
            }

            if (s.equals("--kED")) {
                o.keyType = KeyType.ED25519;
            }

            if (s.equals("--kBP")) {
                o.keyType = KeyType.BRAINPOOLP512T1;
            }
            if (s.equals("--so")) {
                o.scriptOnly = true;
            }


        }
        return o;
    }
}
