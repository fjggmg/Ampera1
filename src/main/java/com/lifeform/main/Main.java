package com.lifeform.main;

import com.lifeform.main.data.Options;

import static java.lang.Thread.sleep;

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *
 * ANYWHERE THE AFOREMENTIONED LICENSE IS NOT FOUND IN THE SOURCE CODE CREATED BY BRYAN SHARPE
 * IS STILL COVERED UNDER GPL V3 EXCLUDING ANY GUI RELATED FILES
 * SEE LICENSE.txt FOR DETAILS.
 *
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

        for (String s : args) {

            if (s.startsWith("-ur")) o.relayToUse = Integer.parseInt(s.replaceFirst("-ur", ""));
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
            if (s.equals("-tgui")) {
                o.tGUI = true;
            }
            if (s.equals("-pr")) {
                o.poolRelay = true;
                o.lite = false;
            }
            if (s.equals("-pool")) {
                o.pool = true;
                o.lite = true;
            }


        }
        return o;
    }
}
