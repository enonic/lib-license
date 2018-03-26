var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateLicense = function () {

    var privateKey = testInstance.load(resolve('generate_private_key.txt'));
    print(privateKey);
    var license = licenseLib.generateLicense(privateKey, {
        issuedBy: 'issuedBy',
        issuedTo: 'issuedTo',
        issueTime: '2018-01-29T11:22:00Z',
        expiryTime: new Date('2058-01-29T11:22:00Z'),
        properties: {
            'nodes': 33
        }
    });
    print(JSON.stringify(license));
    testing.assertNotNull(license);
    var expectedLicense = testInstance.load(resolve('generate_license.txt'));
    print(JSON.stringify(expectedLicense));
    testing.assertEquals(expectedLicense, license);

};
