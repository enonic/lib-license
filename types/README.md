# @enonic-types/lib-license

TypeScript type definitions for the [Enonic XP License library](https://github.com/enonic/lib-license) (`/lib/license`).

## Install

```bash
npm install --save-dev @enonic-types/lib-license
```

The library itself must be included in your XP application's `build.gradle`:

```groovy
dependencies {
    include "com.enonic.lib:lib-license:<version>"
}
```

## Setup

The package declares the ambient module `/lib/license`. Add it to the `types` array in your `tsconfig.json`:

```json
{
  "compilerOptions": {
    "types": [
      "@enonic-types/lib-license"
    ]
  }
}
```

## Usage

```ts
import {
    generateKeyPair,
    generateLicense,
    validateLicense,
    installLicense,
} from '/lib/license';

const keyPair = generateKeyPair();

const license = generateLicense(keyPair.privateKey, {
    issuedBy: 'Enonic AS',
    issuedTo: 'Customer',
    expiryTime: '2030-01-01T00:00:00Z',
    properties: {
        seats: 10,
    },
});

const details = validateLicense({
    license,
    publicKey: keyPair.publicKey,
});

if (details && !details.expired) {
    log.info(`Licensed to ${details.issuedTo}`);
}

installLicense({
    license,
    publicKey: keyPair.publicKey,
    appKey: app.name,
});
```

## API

| Function | Description |
| --- | --- |
| `generateKeyPair()` | Generates a public/private key pair. |
| `loadKeyPair(keyPairString)` | Loads a key pair from its serialized string form. Returns `null` if invalid. |
| `generateLicense(privateKey, params)` | Generates an encoded license string. |
| `validateLicense(params?)` | Validates a license and returns its details, or `null` if invalid. |
| `installLicense(params)` | Validates and stores a license in the XP repository. |
| `uninstallLicense(appKey)` | Removes an installed license from the XP repository. |

See the [library documentation](https://github.com/enonic/lib-license/blob/master/docs/index.adoc) for details.

## License

[Apache-2.0](https://github.com/enonic/lib-license/blob/master/LICENSE.txt)
