/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson.internal;

import com.mongodb.DBObject;
import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.JacksonDBCollection;

/**
 * DBRef that can be fetched
 *
 * @author James Roper
 * @since 1.2
 */
public class FetchableDBRef<T, K> extends DBRef<T, K> {
    private final JacksonDBCollection<T, K> dbCollection;
    private T object;

    public FetchableDBRef(K id, JacksonDBCollection<T, K> dbCollection) {
        super(id, dbCollection.getName());
        this.dbCollection = dbCollection;
    }

    @Override
    public T fetch() {
        if (object == null) {
            object = dbCollection.findOneById(getId());
        }
        return object;
    }

    @Override
    public T fetch(DBObject fields) {
        // No caching, because otherwise we'd have to track which fields were passed in
        return dbCollection.findOneById(getId(), fields);
    }

    public JacksonCollectionKey getCollectionKey() {
        return dbCollection.getCollectionKey();
    }
}
