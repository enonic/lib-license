var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateKeyPair = function () {

    var publicKey = testInstance.load(resolve('validate_public_key.txt'));
    var license = testInstance.load(resolve('validate_license.txt'));

    var licenseDetails = licenseLib.validateLicense(license, publicKey);

    testing.assertNotNull(licenseDetails);
    testing.assertEquals('name', licenseDetails.name);
    testing.assertEquals('org', licenseDetails.organization);
    print(JSON.stringify(licenseDetails, null, 2));
};
