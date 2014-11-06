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

import net.openhft.lang.model.constraints.MaxSize;

public interface IBook {
    public void busyLockBook() throws InterruptedException;
    public void unlockBook();

    public long getMeta();
    public void setMeta(long meta);

    public void setBidLimitAt(@MaxSize(5) int idx, ILimit limit);
    public ILimit getBidLimitAt(int idx);

    public void setAskLimitAt(@MaxSize(5) int idx, ILimit limit);
    public ILimit getAskLimitAt(int idx);

    interface ILimit {
        public double getPrice();
        public void setPrice(double price);

        public double getQuantity();
        public void setQuantity(double quantity);
    }
}
