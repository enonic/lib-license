var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testValidateLicense = function () {

    var publicKey = testInstance.load(resolve('validate_public_key.txt'));
    var license = testInstance.load(resolve('validate_license.txt'));

    var licenseDetails = licenseLib.validateLicense(license, publicKey);

    testing.assertNotNull(licenseDetails);
    testing.assertEquals('name', licenseDetails.name);
    testing.assertEquals('org', licenseDetails.organization);
};

exports.testValidateLicenseFromApp = function () {

    var publicKey = testInstance.load(resolve('validate_public_key.txt'));

    var licenseDetails = licenseLib.validateLicense(null, publicKey);

    testing.assertNotNull(licenseDetails);
    testing.assertEquals('name', licenseDetails.name);
    testing.assertEquals('org', licenseDetails.organization);
};

exports.testValidateLicenseNoParams = function () {

    var licenseDetails = licenseLib.validateLicense();

    testing.assertNotNull(licenseDetails);
    testing.assertEquals('name', licenseDetails.name);
    testing.assertEquals('org', licenseDetails.organization);
};