Test Certificate Authority
==========================

Here is how to recreate all of this using OpenSSL.

Configure OpenSSL
-----------------

In a new directory, add the following OpenSSL configurations:

_careq.openssl.cnf_
````
RANDFILE = $ENV::HOME/.rnd
[ req ]
output_password = mallard
default_bits = 1024
default_keyfile = ./testCA/private/cakey.pem
prompt = no
distinguished_name = req_distinguished_name
[ req_distinguished_name ]
countryName = US
stateOrProvinceName = Massachusetts
localityName = Burlington
0.organizationName = Black Duck Software, Inc.
commonName = KeyStore Test Root CA
````

_ca.openssl.cnf_
````
RANDFILE = $ENV::HOME/.rnd
[ ca ]
default_ca = CA_default
[ CA_default ]
dir = ./testCA
new_certs_dir = $dir/newcerts
certificate = $dir/cacert.pem
private_key = $dir/private/cakey.pem
RANDFILE = $dir/private/.rand
default_days = 24855
default_md = sha1
database = $dir/index.txt
serial = $dir/serial
x509_extensions	= usr_cert
preserve = no
policy = policy_match
name_opt = ca_default
cert_opt = ca_default
[ usr_cert ]
basicConstraints=CA:FALSE
nsCertType = client, email
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
subjectAltName=email:move
[ v3_ca ]
basicConstraints = CA:TRUE
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid:always,issuer:always
[ policy_match ]
countryName = match
stateOrProvinceName = match
organizationName = match
commonName = supplied
emailAddress = optional
````

_user.openssl.cnf_
````
RANDFILE = $ENV::HOME/.rnd
[ req ]
output_password = changeit
default_bits = 1024
default_keyfile = ./user/privkey.pem
distinguished_name = req_distinguished_name
[ req_distinguished_name ]
countryName = Country Name (2 letter code)
countryName_default = US
countryName_min = 2
countryName_max = 2
stateOrProvinceName = State Name
stateOrProvinceName_default = Massachusetts
localityName = Locality Name
localityName_default = Burlington
0.organizationName = Organization Name
0.organizationName_default = Black Duck Software, Inc.
commonName = User Full (common) Name
commonName_default=sysadmin
commonName_max = 64
emailAddress = Email Address
emailAddress_default=noreply@blackducksoftware.com
emailAddress_max = 64
````

Create Certificate Authority
----------------------------

From the same directory, execute the following commands to generate the CA:

````
$ mkdir -p testCA/newcerts testCA/private && chmod 700 testCA/private && touch testCA/index.txt
$ openssl req -new -out careq.pem -config careq.openssl.cnf
$ openssl ca -create_serial -batch -selfsign -extensions v3_ca -in careq.pem -key mallard -out testCA/cacert.pem -config ca.openssl.cnf && rm careq.pem
````

Create User Certificate
-----------------------

Then create a user certificate and convert it to a bunch of different formats:

````
$ mkdir -p user
$ openssl req -new -out userreq.pem -config user.openssl.cnf
$ openssl ca -batch -in userreq.pem -key mallard -out user/cert.pem -config ca.openssl.cnf && rm userreq.pem
$ openssl crl2pkcs7 -nocrl -certfile user/cert.pem -certfile testCA/cacert.pem -out user/cert.p7c.der -outform DER
$ openssl rsa -in user/privkey.pem -passin pass:changeit -out user/privkey.aes192.pem -passout pass:changeit -aes192
$ openssl rsa -in user/privkey.pem -passin pass:changeit -out user/privkey.clear.pem
$ openssl rsa -in user/privkey.pem -passin pass:changeit -out user/privkey.clear.der -outform DER
$ openssl pkcs8 -topk8 -in user/privkey.pem -passin pass:changeit -out user/privkey.pk8.pem -passout pass:changeit
$ openssl pkcs8 -topk8 -in user/privkey.pem -passin pass:changeit -out user/privkey.pk8.der -passout pass:changeit -outform DER
$ openssl pkcs8 -topk8 -in user/privkey.pem -passin pass:changeit -out user/privkey.pk8.des3.pem -v2 des3 -passout pass:changeit
$ openssl pkcs8 -topk8 -in user/privkey.pem -passin pass:changeit -out user/privkey.pk8.des3.der -v2 des3 -passout pass:changeit -outform DER
$ openssl pkcs8 -topk8 -in user/privkey.pem -passin pass:changeit -out user/privkey.pk8.clear.pem -nocrypt
$ openssl pkcs8 -topk8 -in user/privkey.pem -passin pass:changeit -out user/privkey.pk8.clear.der -nocrypt -outform DER
$ openssl pkcs12 -export -chain -name user -inkey user/privkey.pem -CAfile testCA/cacert.pem -in user/cert.pem -out user/user.p12 -passin pass:changeit -passout pass:changeit
$ keytool -importkeystore -srckeystore user/user.p12 -destkeystore user/user.jks -srcstoretype pkcs12 -srcalias user -destalias user -srcstorepass changeit -deststorepass changeit
$ keytool -importkeystore -srckeystore user/user.p12 -destkeystore user/user.jceks -srcstoretype pkcs12 -srcalias user -destalias user -srcstorepass changeit -deststorepass changeit -deststoretype jceks
$ chmod 600 user/privkey.*
````

Create Verification Data
------------------------

And finally, for verification purposes:

````
$ echo -n The Magic Words are Squeamish Ossifrage > user/plaintext.txt
$ openssl rsautl -sign -in user/plaintext.txt -inkey user/privkey.pem -passin pass:changeit | openssl enc -base64 > user/ciphertext.txt
````
