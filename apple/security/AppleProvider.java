/*
 * Copyright (c) 2011, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package apple.security;

import java.security.*;
import static sun.security.util.SecurityConstants.PROVIDER_VER;

/**
 * The Apple Security Provider.
 */

/**
 * Defines the Apple provider.
 *
 * This provider only exists to provide access to the Apple keychain-based KeyStore implementation
 */
@SuppressWarnings("serial") // JDK implementation class
public final class AppleProvider extends Provider {

    private static final String info = "Apple Provider";

    private static final class ProviderService extends Provider.Service {
        ProviderService(Provider p, String type, String algo, String cn) {
            super(p, type, algo, cn, null, null);
        }

        @Override
        public Object newInstance(Object ctrParamObj)
            throws NoSuchAlgorithmException {
            String type = getType();
            if (ctrParamObj != null) {
                throw new InvalidParameterException
                    ("constructorParameter not used with " + type + " engines");
            }

            String algo = getAlgorithm();
            try {
                if (type.equals("KeyStore")) {
                    if (algo.equals("KeychainStore")) {
                        return new KeychainStore();
                    }
                }
            } catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " +
                    type + " for " + algo + " using Apple", ex);
            }
            throw new ProviderException("No impl for " + algo +
                " " + type);
        }
    }


    public AppleProvider() {
        /* We are the Apple provider */
        super("Apple", PROVIDER_VER, info);

        final Provider p = this;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                putService(new ProviderService(p, "KeyStore",
                           "KeychainStore", "apple.security.KeychainStore"));
                return null;
            }
        });
    }
}
