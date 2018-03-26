var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateLicense = function () {

    var privateKey = testInstance.load(resolve('generate_private_key.txt'));

    var license = licenseLib.generateLicense(privateKey, {
        issuedBy: 'issuedBy',
        issuedTo: 'issuedTo',
        issueTime: '2018-01-29T11:22:00Z',
        expiryTime: new Date('2058-01-29T11:22:00Z'),
        properties: {
            'nodes': 33
        }
    });

    testing.assertNotNull(license);
    var expectedLicense = testInstance.load(resolve('generate_license.txt'));

    testing.assertEquals(expectedLicense, license, JSON.stringify(license, null, 2) + ' -> ' + JSON.stringify(expectedLicense, null, 2));

};
