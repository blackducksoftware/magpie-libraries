Test Certificate Data
=====================

Here is how to recreate all of this using OpenSSL.

Configure OpenSSL
-----------------

In a new directory, add the following OpenSSL configurations:

_openssl.cnf_

````
[ req ]
prompt				= no
encrypt_key			= no
default_bits		= 512
default_md			= md5
distinguished_name	= req_distinguished_name
default_keyfile		= /dev/null

[ req_distinguished_name ]
1.domainComponent	= com
2.domainComponent	= example
commonName			= example.com

[ req_x509_server_single_san ]
basicConstraints	= critical,CA:FALSE
keyUsage			= digitalSignature
extendedKeyUsage	= serverAuth
subjectAltName		= DNS:example.com

````

Generate Test Certificate Data
==============================

````
openssl req -config openssl.cnf -x509 -newkey rsa -days 24855 -extensions req_x509_server_single_san -out server_single_san.pem
````