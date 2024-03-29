/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.extensions.appengine.subscriptions;

import com.google.api.client.googleapis.subscriptions.Subscription;
import com.google.api.client.googleapis.subscriptions.SubscriptionStore;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * Implementation of a persistent {@link SubscriptionStore} making use of native DataStore and
 * the Memcache API on AppEngine.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <p>
 * On AppEngine you should prefer this SubscriptionStore over others due to performance and quota
 * reasons.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    SubscriptionStore store = new CahcedAppEngineSubscriptionStore();
    service.setSubscriptionManager(new SubscriptionManager(store));
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public final class CachedAppEngineSubscriptionStore extends AppEngineSubscriptionStore {

  /** Cache expiration time in seconds. */
  private static final int EXPIRATION_TIME = 3600;

  /** The service instance used to access the Memcache API. */
  private MemcacheService memCache = MemcacheServiceFactory.getMemcacheService(
      CachedAppEngineSubscriptionStore.class.getCanonicalName());

  @Override
  public void removeSubscription(Subscription subscription) throws Exception {
    super.removeSubscription(subscription);
    memCache.delete(subscription.getSubscriptionID());
  }

  @Override
  public void storeSubscription(Subscription subscription) throws Exception {
    super.storeSubscription(subscription);
    memCache.put(subscription.getSubscriptionID(), subscription);
  }

  @Override
  public Subscription getSubscription(String subscriptionID) throws Exception {
    if (memCache.contains(subscriptionID)) {
      return (Subscription) memCache.get(subscriptionID);
    }

    Subscription subscription = super.getSubscription(subscriptionID);
    memCache.put(subscriptionID, subscription, Expiration.byDeltaSeconds(EXPIRATION_TIME));
    return subscription;
  }
}
