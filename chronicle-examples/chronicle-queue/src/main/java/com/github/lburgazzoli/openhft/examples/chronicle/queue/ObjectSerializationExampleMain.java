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
package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.*;

import java.io.Serializable;

public class ObjectSerializationExampleMain {
    public static class MessageKey implements Serializable {
        private String arg1;
        private long arg2;

        public MessageKey(String arg1, long arg2) {
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        @Override
        public String toString() {
            return "MessageKey{" + "arg1='" + arg1 + ", arg2=" + arg2 + "}";
        }
    }

    public static void main(final String[] args) throws Exception {
        String basePath = System.getProperty("java.io.tmpdir");

        Chronicle chron = ChronicleQueueBuilder.vanilla(basePath + "/vanilla").build();
        chron.clear();

        ExcerptAppender app = chron.createAppender();
        app.startExcerpt();
        app.writeObject(new MessageKey("type", 123L));
        app.finish();
        app.close();

        ExcerptTailer vtail = chron.createTailer();
        while(vtail.nextIndex()) {
            MessageKey key = (MessageKey) vtail.readObject();
            System.out.println("key " + key);
        }

        vtail.finish();
        vtail.close();

        chron.close();
    }
}
