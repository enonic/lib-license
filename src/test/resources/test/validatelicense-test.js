var testing = require('/lib/xp/testing');
var licenseLib = require('/lib/license');


exports.testValidateLicense = function () {

    var publicKey = testInstance.load(resolve('validate_public_key.txt'));
    var license = testInstance.load(resolve('validate_license.txt'));

    var licenseDetails = licenseLib.validateLicense({
        license: license,
        publicKey: publicKey
    });

    testing.assertNotNull(licenseDetails);
    testing.assertEquals('issuedBy', licenseDetails.issuedBy);
    testing.assertEquals('issuedTo', licenseDetails.issuedTo);
};

exports.testValidateLicenseFromApp = function () {

    var publicKey = testInstance.load(resolve('validate_public_key.txt'));

    var licenseDetails = licenseLib.validateLicense({publicKey: publicKey});

    testing.assertNotNull(licenseDetails);
    testing.assertEquals('issuedBy', licenseDetails.issuedBy);
    testing.assertEquals('issuedTo', licenseDetails.issuedTo);
};

exports.testValidateLicenseNoParams = function () {

    var licenseDetails = licenseLib.validateLicense();

    testing.assertNotNull(licenseDetails);
    testing.assertEquals('issuedBy', licenseDetails.issuedBy);
    testing.assertEquals('issuedTo', licenseDetails.issuedTo);
};