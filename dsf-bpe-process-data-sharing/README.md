## Properties

**MeDIC**
`keytool -genseckey -alias <organization-identifier> -keyalg aes -keysize 256 -keystore organization-keystore.p12 -storetype pkcs12` 
where `<organization-identifier>` is the same as the property `org.highmed.dsf.bpe.fhir.organization.identifier.localValue` and the password for the key

* org.highmed.dsf.bpe.psn.organization.keystore=conf/organization-keystore.p12
* org.highmed.dsf.bpe.psn.organization.keystore.password=password

**TTP**
Keystore will be created if none exists

* org.highmed.dsf.bpe.psn.research.study.keystore=conf/research-study-keystore.p12
* org.highmed.dsf.bpe.psn.research.study.keystore.password=password