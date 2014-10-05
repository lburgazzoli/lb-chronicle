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
package com.github.lburgazzoli.openhft.examples.chronicle.map.sor;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.io.serialization.impl.NewInstanceObjectFactory;
import net.openhft.lang.model.DataValueClasses;

import java.io.File;
import java.io.IOException;

public final class SORCommon {
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    public static ChronicleMap<String, IBook> getMap() throws IOException {
        Class<IBook> type = DataValueClasses.directClassFor(IBook.class);
        NewInstanceObjectFactory<IBook> factory = new NewInstanceObjectFactory<IBook>(type);

        final File file = new File(TMP_DIR + "/sor");
        file.deleteOnExit();

        return ChronicleMapBuilder
            .of(String.class, type)
            .valueFactory(factory)
            .entrySize(512)
            .create(file);
    }
}
