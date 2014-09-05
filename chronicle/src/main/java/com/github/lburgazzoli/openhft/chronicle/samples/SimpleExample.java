package com.github.lburgazzoli.openhft.chronicle.samples;

import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.VanillaChronicle;

import java.io.Serializable;

public class SimpleExample {
    public static class MessageKey implements Serializable {
        private String arg1;
        private long arg2;

        public MessageKey(String arg1, long arg2) {
            this.arg1 = arg1;
            this.arg2 = arg2;
        }
    }

    public static void main(final String[] args) throws Exception {
        String basePath = System.getProperty("java.io.tmpdir");
        VanillaChronicle vcron = new VanillaChronicle(basePath + "/vanilla");
        VanillaChronicle.VanillaAppender app = vcron.createAppender();
        app.startExcerpt();
        app.writeObject(new MessageKey("type", 123L));
        app.finish();
        app.close();

        ExcerptTailer vtail = vcron.createTailer();
        while(vtail.nextIndex()) {
            MessageKey key = (MessageKey) vtail.readObject();
            System.out.println("key " + key);
        }

        vtail.finish();
        vtail.close();
    }
}
