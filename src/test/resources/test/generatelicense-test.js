var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateLicense = function () {

    var privateKey = testInstance.load(resolve('generate_private_key.txt'));

    var license = licenseLib.generateLicense(privateKey, {
        name: 'name',
        organization: 'org',
        issueTime: '2018-01-29T11:22:00Z',
        expiryTime: new Date('2018-01-29T11:22:00Z'),
        properties: {
            'nodes': 33
        }
    });

    testing.assertNotNull(license);
    var expectedLicense = testInstance.load(resolve('generate_license.txt'));

    testing.assertEquals(expectedLicense, license);

};
