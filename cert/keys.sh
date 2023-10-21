#!/usr/bin/env bash

keytool -importcert -alias clientKeyPair -storetype PKCS12 -keystore client_keystore.p12 -file server_certificate.cer -rfc -storepass reth465j5jyjytfg.-
