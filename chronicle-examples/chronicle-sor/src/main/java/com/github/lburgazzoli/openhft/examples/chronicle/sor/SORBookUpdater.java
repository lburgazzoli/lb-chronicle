/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lburgazzoli.openhft.examples.chronicle.sor;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.openhft.chronicle.map.ChronicleMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SORBookUpdater {
    public static final Logger LOGGER = LoggerFactory.getLogger(SORBookUpdater.class);


    public static void main(String[] args) throws Exception {
        OptionParser parser = new OptionParser();
        OptionSpec<String>  pid   = parser.accepts("id").withRequiredArg().ofType(String.class).required();
        OptionSpec<Integer> pidx  = parser.accepts("index").withRequiredArg().ofType(Integer.class).required();
        OptionSpec<String>  pside = parser.accepts("side").withRequiredArg().ofType(String.class).required();
        OptionSpec<Double>  ppx   = parser.accepts("px").withRequiredArg().ofType(Double.class).required();
        OptionSpec<Double>  pqty  = parser.accepts("qty").withRequiredArg().ofType(Double.class).required();

        OptionSet options = parser.parse(args);

        ChronicleMap<String,IBook> map = null;

        try {
            map = SORCommon.getMap();

            String  key   = options.valueOf(pid);
            Integer idx   = options.valueOf(pidx);
            String  side  = options.valueOf(pside);
            Double  px    = options.valueOf(ppx);
            Double  qty   = options.valueOf(pqty);

            IBook book = map.acquireUsing(key, null);

            try {
                book.busyLockBook();
                switch (side) {
                    case "bid":
                    case "BID": {
                            IBook.ILimit limit = book.getBidLimitAt(idx);
                            limit.setPrice(px);
                            limit.setQuantity(qty);
                        }
                        break;
                    case "ask":
                    case "ASK": {
                            IBook.ILimit limit = book.getAskLimitAt(idx);
                            limit.setPrice(px);
                            limit.setQuantity(qty);
                        }
                        break;
                }
            } finally {
                book.unlockBook();
            }
        } finally {
            if(map != null) {
                map.close();
            }
        }
    }
}
