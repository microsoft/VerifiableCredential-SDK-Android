[![Build Status](https://decentralized-identity.visualstudio.com/Core/_apis/build/status/Microsoft.UserAgent?branchName=master)](https://decentralized-identity.visualstudio.com/Core/_build/latest?definitionId=19&branchName=master)

# User Agent SDK - Open Source

SDK for building decentralized identity wallets and agents.

## Identifiers

This class is the core of this sdk. An Identifier can be used to register (via registrar) and discovery (via resolver) decentralized identifiers. It can also be used to generate and store keys (via keyStore), and then use those keys to encrypt, decrypt, sign, and verify payloads. The User Agent Options allow the develop to plug in what particular registrar, resolver, and keystore instance they want to use when creating Identifiers.

Creating an instance of an Identifier Example:

```typescript
// create a new keystore instance
const keyStore = new InMemoryKeyStore('secretString');
// save a master seed in keystore for key generation.
await keyStore.save('masterSeed', Buffer.from('xxxxxxxxxxxxxxxxx'));

// set up HttpResolver with discovery service url. 
const resolver = new HttpResolver('https://beta.discover.microsoft.com');

// set up options.
const options = new UserAgentOptions();
options.resolver = resolver;
const registrar = new SidetreeRegistrar(sidetreeUrl, options);
options.registrar = registrar;

// should return a new Identifier Object.
const identifier = await Identifier.create(options);
```

## Hub Communication

Hub Interfaces can be used to make requests to [Decentralized Identity Hubs](https://github.com/decentralized-identity/identity-hub). The Hub Interfaces are Collections, Actions, Permissions, and Profile. 

### Collections

```typescript
// create options for collections.
const options: HubInterfaceOptions = new HubInterfaceOptions();
options.clientIdentifier = identifier;
options.hubOwner = identifier;
options.context = 'https://schema.org/tasks';
options.type = 'tasks';

// create a new instance as options as the parameter.
const collection = new Collections(options);

// add a new object of type 'tasks' to hub owner's hub.
await collection.addObject({task: 'write readme', done: true});

// get all objects of type 'tasks'
const tasks = await collection.getObjects();

// should print out 'write readme'
console.log(tasks[0].getPayload().task);
```

## User Agent Session

A User Agent Session is a class for creating Authentication Requests and Response in compliance with [OpenID Connect Self-Issued Tokens](https://openid.net/specs/openid-connect-core-1_0.html#SelfIssued).

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## License

Copyright (c) Microsoft Corporation. All rights reserved.
Licensed under the [MIT](LICENSE.txt) License.
