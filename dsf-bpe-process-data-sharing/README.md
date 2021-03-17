## Properties

**MeDIC**
`keytool -genseckey -alias <organization-identifier> -keyalg aes -keysize 256 -keystore organization-key.jceks -storetype jceks` 
where `<organization-identifier>` is the same as the property `org.highmed.dsf.bpe.fhir.organization.identifier.localValue` and the password for the key

* org.highmed.dsf.bpe.psn.organizationKey.keystore.file=conf/organization-key.jceks
* org.highmed.dsf.bpe.psn.organizationKey.keystore.password=password

**TTP**
Keystore will be created if none exists

* org.highmed.dsf.bpe.psn.researchStudyKeys.keystore.file=conf/research-study-keystore.jceks
* org.highmed.dsf.bpe.psn.researchStudyKeys.keystore.password=password