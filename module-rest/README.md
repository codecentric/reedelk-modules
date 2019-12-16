
SSL Certificate
========================

* Create a certificate with com.reedelk.rest.utils.SelfSignedCertificateBuilder
* cacerts file (stores the certificates) is in the Java Home (e.g /Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/jre/lib/security)
* List certificates: keytool -list -keystore cacerts
* Delete certificate (by given alias): sudo keytool -delete -alias reedelktest -keystore cacerts
* Add certificate (with alias): sudo keytool -import -trustcacerts -file /Users/username/Desktop/mycert.crt -alias localhost -keystore cacerts 