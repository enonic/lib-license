var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateLicense = function () {

    var privateKey = testInstance.load(resolve('generate_private_key.txt'));

    var license = licenseLib.generateLicense(privateKey, {
        name: 'name',
        organization: 'org'
    });

    testing.assertNotNull(license);
    var expectedLicense = testInstance.load(resolve('generate_license.txt'));

    testing.assertEquals(expectedLicense, license);

};
