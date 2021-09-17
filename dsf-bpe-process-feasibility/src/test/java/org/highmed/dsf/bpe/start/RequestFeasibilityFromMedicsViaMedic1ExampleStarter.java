package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.MEDIC_1_FHIR_BASE_URL;

public class RequestFeasibilityFromMedicsViaMedic1ExampleStarter
		extends AbstractRequestFeasibilityFromMedicsViaMedic1ExampleStarter
{
	private static boolean NEEDS_CONSENT_CHECK = true;
	private static boolean NEEDS_RECORD_LINKAGE = true;

	private static final String QUERY = "SELECT COUNT(e) FROM EHR e;";

	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	// highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	// password
	public static void main(String[] args) throws Exception
	{
		new RequestFeasibilityFromMedicsViaMedic1ExampleStarter().main(args, MEDIC_1_FHIR_BASE_URL);
	}
}
