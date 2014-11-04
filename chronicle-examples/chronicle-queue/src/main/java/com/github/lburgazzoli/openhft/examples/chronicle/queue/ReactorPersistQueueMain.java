package com.github.lburgazzoli.openhft.examples.chronicle.queue;

import net.openhft.chronicle.ChronicleConfig;

import reactor.io.Buffer;
import reactor.io.encoding.Codec;
import reactor.io.encoding.kryo.KryoCodec;
import reactor.queue.IndexedChronicleQueuePersistor;
import reactor.queue.PersistentQueue;
import reactor.queue.QueuePersistor;

import java.io.IOException;

public class ReactorPersistQueueMain {

    public static class Person implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private String name;

        private String address;

        private byte[] info;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public byte[] getInfo() {
            return info;
        }

        public void setInfo(byte[] info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return this.address + " " + this.name;
        }
    }

    public static void main(String[] args) throws Exception {
        PersistentQueue<Person> transferQueue = new PersistentQueue<Person>(getTransferMsgPersitor());

        System.out.println("before poll1 queue===" + transferQueue.isEmpty());

        long t1 = System.currentTimeMillis();

        Person person = null;
        for (int i = 0; i < 1000; i++) {
            person = getPerson(i);
            transferQueue.offer(person);
        }

        long t2 = System.currentTimeMillis();

        while (!transferQueue.isEmpty()) {
            Person _person = transferQueue.poll();

            System.out.println("1===" + _person.toString());
        }

        long t3 = System.currentTimeMillis();

        System.out.println("offer cost==={" + (t2 - t1) + "}ms,poll cost==={" + (t3 - t2) + "}ms");

        System.out.println("after poll1 queue===" + transferQueue.isEmpty());

        while (!transferQueue.isEmpty()) {
            Person _person = transferQueue.poll();

            System.out.println("2========" + _person.toString());
        }

        System.out.println("after poll2 queue===" + transferQueue.isEmpty());
    }

    private static Person getPerson(int i) {
        Person person = new Person();

        person.setAddress("address:" + i);

        person.setName("name:" + i);

        person.setInfo(String.valueOf(System.currentTimeMillis()).getBytes());

        return person;
    }

    private static QueuePersistor<Person> getTransferMsgPersitor() throws IOException {
        String basePath = System.getProperty("java.io.tmpdir") + "/" + "reactor";

        System.out.println("basePath===" + basePath);

        IndexedChronicleQueuePersistor<Person> indexChronicePersistor =
            new IndexedChronicleQueuePersistor<Person>(
                basePath,
                getReactorCodec(),
                true,
                true,
                ChronicleConfig.DEFAULT.clone());

        return indexChronicePersistor;
    }

    private static Codec<Buffer, Person, Person> getReactorCodec() {
        return new KryoCodec<Person, Person>();
    }

}