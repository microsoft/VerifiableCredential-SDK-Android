import { UserAgentSession, UserAgentOptions, Identifier, HubClient, Commit, PublicKey, EcPublicKey, EcPrivateKey } from "../../src";
import TestResolver from "../resolvers/TestResolver";
import { OIDCRequestOptions } from "../../src/userAgentSession/UserAgentSession";
import OIDCAuthenticationRequest from "../../src/crypto/protocols/did/requests/OIDCAuthenticationRequest";
import OIDCAuthenticationResponse from '../../src/crypto/protocols/did/responses/OIDCAuthenticationResponse';
import IRequestPrompt, { IPermissionRequestPrompt } from "../../src/userAgentSession/oidc/requests/IRequestPrompt";
import IScopeDefinition from "../../src/userAgentSession/oidc/requests/IScopeDefinition";
import * as nodeFetch from 'node-fetch';
import IPermissionGrant from "../../src/hubSession/objects/IPermissionGrant";

describe('UserAgentSession', () => {
  let challengerSession: UserAgentSession;
  let responderSession: UserAgentSession;
  let challenger: Identifier;
  let responder: Identifier;
  let challengerResolver = new TestResolver();
  let responderResolver = new TestResolver();

  beforeAll(async () => {
    challenger = new Identifier('did:test:challenger', new UserAgentOptions());
    responder = new Identifier('did:test:responder', new UserAgentOptions());
  });

  beforeEach(async () => {
    challengerSession = new UserAgentSession(challenger, 'did:ion-did:ion-ES256K-sig', challengerResolver);
    responderSession = new UserAgentSession(responder, 'did:ion-did:ion-ES256K-sig', responderResolver);
  });
  
  describe('signRequest', () => {
    const redirectUrl = `localhost/${Math.round(Math.random() * 255).toString(16)}`;

    let defaultOIDC: OIDCAuthenticationRequest = {
      client_id: redirectUrl,
      iss: 'did:test:challenger',
      nonce: 'Will Be Set In The Test',
      response_mode: "form_post",
      response_type: "id_token",
      scope: 'openid',
    };

    let testParams: {
      should: string,
      options?: OIDCRequestOptions,
      expect: OIDCAuthenticationRequest
    }[] = [
      {
        should: 'create a simple id_token request',
        expect: defaultOIDC
      },
      {
        should: 'include state',
        options: {
          state: 'alphabet soup'
        },
        expect: Object.assign({}, defaultOIDC,
          {
            state: 'alphabet soup'
          })
      },
      {
        should: 'encode the manifest',
        options: {
          manifest: {
            client_name: 'alice',
            logo_uri: 'localhost/favicon.ico',
          }
        },
        expect: Object.assign({}, defaultOIDC, {
          registration: JSON.stringify({
            client_name: 'alice',
            logo_uri: 'localhost/favicon.ico',
          })
        })
      },
      {
        should: 'include additonal scopes',
        options: {
          scopes: [
            {
              'localhost/test.all': null,
            }
          ]
        },
        expect: Object.assign({}, defaultOIDC, {
          scope: 'openid ' + Buffer.from(JSON.stringify({
            'localhost/test.all': null,
          })).toString('base64')
        })
      },
      {
        should: 'support multiple scope definitions',
        options: {
          scopes: [
            {
              'localhost/test.all': {essential: false},
            },
            {
              'localhost/test.all': {value: 'sure'},
            }
          ]
        },
        expect: Object.assign({}, defaultOIDC, {
          scope: 'openid ' + Buffer.from(JSON.stringify({
            'localhost/test.all': {essential: false},
          })).toString('base64') + ' ' + Buffer.from(JSON.stringify({
            'localhost/test.all': {value: 'sure'},
          })).toString('base64')
        })
      },
      // DISABLED WHILE CLAIMS THROW
      // {
      //   should: 'include claim requests',
      //   options: {
      //     claimRequests: {
      //       credential: [
      //         'PassTest'
      //       ]
      //     }
      //   },
      //   expect: Object.assign({}, defaultOIDC,
      //     {
      //       claims: {
      //         id_token: {
      //           credential: [
      //             'PassTest'
      //           ]
      //         }
      //       }
      //     })
      // }
    ];

    testParams.forEach((testCase) => {
      it(`should ${testCase.should}`, async () => {
        const nonce = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(16);
        const signSpy = spyOn(challenger, 'sign').and.callFake((request: OIDCAuthenticationRequest) => {
          testCase.expect.nonce = nonce;
          expect(request).toEqual(testCase.expect);
          return Promise.resolve('');
        });
        await challengerSession.signRequest(redirectUrl, nonce, testCase.options);
        expect(signSpy).toHaveBeenCalled();
      });
    });
  });

  describe('verifyAndHydrateRequest', () => {
    const redirectUrl = `http://localhost/${Math.round(Math.random() * 255).toString(16)}`;

    const defaultPrompt: IRequestPrompt = {
      client_id: redirectUrl,
      host: 'localhost',
      iss: 'did:test:challenger',
      nonce: 'Will Be Set In The Test',
      response_mode: 'form_post',
      response_type: 'id_token',
      scope: 'openid'
    };

    const noScopePrompt: any = {
      client_id: redirectUrl,
      host: 'localhost',
      iss: 'did:test:challenger',
      nonce: 'Will Be Set In The Test',
      response_mode: 'form_post',
      response_type: 'id_token',
    }

    let testParams: {
      should: string,
      request?: OIDCRequestOptions,
      resolvedScopes?: {[scopeUrls: string]: IScopeDefinition},
      expected: Partial<IRequestPrompt>
    }[] = [{
      should: 'create a basic sign in prompt',
      expected: Object.assign({}, defaultPrompt)
    },
    {
      should: 'populate fields in the manifest',
      request: {
        manifest: {
          client_name: 'Test Client 123',
          logo_uri: 'http://localhost/icon',
          client_uri: 'http://localhost/index.html',
          policy_uri: 'http://localhost/cookies',
          tos_uri: 'http://localhost/tos'
        }
      },
      expected: Object.assign({}, defaultPrompt, {
        name: 'Test Client 123',
        logoUrl: 'http://localhost/icon',
        homepage: 'http://localhost/index.html',
        dataUsePolicy: 'http://localhost/cookies',
        termsOfService: 'http://localhost/tos'
      })
    },
    {
      should: 'ignore unfilled manifest fields',
      request: {
        manifest: {
          client_name: 'Test Client 123'
        }
      },
      expected: Object.assign({}, defaultPrompt, {
        name: 'Test Client 123',
        logoUrl: undefined,
        homepage: undefined,
        dataUsePolicy: undefined,
        termsOfService: undefined
      })
    },
    // DISABLED WHILE CLAIMS THROW
    // {
    //   should: 'include credential claims',
    //   request: {
    //     claimRequests: {
    //       credential: {
    //         TestStatus: null
    //       }
    //     }
    //   },
    //   expected: Object.assign({}, defaultPrompt, {
    //     credentialsRequested: ['TestStatus']
    //   })
    // },
    {
      should: 'retrieve permission scopes',
      request: {
        scopes: [{
          'http://localhost/testStatus.read': { essential: true },
        }]
      },
      resolvedScopes: {
        "http://localhost/testStatus.read": {
          value: "http://localhost/testStatus.read",
          resourceBundle: {
            name: "Test Status",
            description: "Read access to all test statuses",
            icon_uri: "http://localhost/icon.ico"
          },
          access: [
            {
              resource_type: 'https://schema.org/TestStatus',
              allow: '-R--'
            }
          ]
        }
      },
      expected: Object.assign({}, noScopePrompt, {
        identityHubPermissionsRequested: [
          {
            required: true,
            name: "Test Status",
            description: "Read access to all test statuses",
            iconUrl: 'http://localhost/icon.ico',
            grants: [
              {
                owner: 'did:test:responder',
                grantee: 'did:test:challenger',
                allow: '-R--',
                context: 'https://schema.org',
                type: 'TestStatus'
              }
            ]
          }
        ]
      })
    },
    {
      should: 'handle multiple different permission scopes',
      request: {
        scopes: [{
          'http://localhost/testStatus.read': { essential: true },
          'http://localhost/testStatus.write': null,
        }]
      },
      resolvedScopes: {
        "http://localhost/testStatus.read": {
          value: "http://localhost/testStatus.read",
          resourceBundle: {
            name: "Test Status",
            description: "Read access to all test statuses",
            icon_uri: "http://localhost/icon.ico"
          },
          access: [
            {
              resource_type: 'https://schema.org/TestStatus',
              allow: '-R--'
            }
          ]
        },
        "http://localhost/testStatus.write": {
          value: "http://localhost/testStatus.write",
          resourceBundle: {
            name: "Test Status",
            description: "Write access to all test statuses",
            icon_uri: "http://localhost/icon.ico"
          },
          access: [
            {
              resource_type: 'https://schema.org/TestStatus',
              allow: 'C-UD'
            }
          ]
        }
      },
      expected: Object.assign({}, noScopePrompt, {
        identityHubPermissionsRequested: [
          {
            required: true,
            name: "Test Status",
            description: "Read access to all test statuses",
            iconUrl: 'http://localhost/icon.ico',
            grants: [
              {
                owner: 'did:test:responder',
                grantee: 'did:test:challenger',
                allow: '-R--',
                context: 'https://schema.org',
                type: 'TestStatus'
              }
            ]
          },
          {
            required: false,
            name: "Test Status",
            description: "Write access to all test statuses",
            iconUrl: 'http://localhost/icon.ico',
            grants: [
              {
                owner: 'did:test:responder',
                grantee: 'did:test:challenger',
                allow: 'C-UD',
                context: 'https://schema.org',
                type: 'TestStatus'
              }
            ]
          }
        ]
      })
    }];

    testParams.forEach((testCase) => {
      it(`should ${testCase.should}`, async () => {
        const nonce = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(16);

        // utilize the sign request function, since we've got it... 
        let formedRequest: OIDCAuthenticationRequest;
        spyOn(challengerSession['sender'], 'sign').and.callFake((request: OIDCAuthenticationRequest) => {
          formedRequest = request;
          return Promise.resolve('');
        });
        await challengerSession.signRequest(redirectUrl, nonce, testCase.request);

        const encryptedStub = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(32);
        const verifySpy = spyOn(responderSession, 'verify').and.callFake(async (request: string) => {
          expect(request).toEqual(encryptedStub);
          return Promise.resolve(formedRequest);
        });
        const fetchSpy = spyOn(nodeFetch, 'default').and.callFake((url: any): any => {
          if (testCase.resolvedScopes && url in testCase.resolvedScopes) {
            return Promise.resolve({
              status: 200,
              text: () => {
                return JSON.stringify(testCase.resolvedScopes![url]);
              }
            });
          } else {
            fail(`Attempted superfluous resolve to url: ${url}`);
          }
        });
        let expected: IRequestPrompt = Object.assign({}, formedRequest!, testCase.expected, { nonce });
        let actual = await responderSession.verifyAndHydrateRequest(encryptedStub);
        expect(verifySpy).toHaveBeenCalled();
        if (testCase.resolvedScopes) {
          expect(fetchSpy).toHaveBeenCalled();
        };
        expect(actual).toEqual(expected);
      });
    });

    it('should not include host if the redirect is a deeplink', async () => {
      const nonce = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(16);

        // utilize the sign request function, since we've got it... 
        let formedRequest: OIDCAuthenticationRequest;
        spyOn(challengerSession['sender'], 'sign').and.callFake((request: OIDCAuthenticationRequest) => {
          formedRequest = request;
          return Promise.resolve('');
        });
        await challengerSession.signRequest('myapp://response', nonce);
        const verifySpy = spyOn(responderSession, 'verify').and.returnValue(Promise.resolve(formedRequest!));
        let actual = await responderSession.verifyAndHydrateRequest('doesnt Matter');
        expect(verifySpy).toHaveBeenCalled();
        expect(actual.host).toBeUndefined();
    });

    it('should throw if the scope definition failed to be retrieved', async () => {
      const nonce = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(16);

      // utilize the sign request function, since we've got it... 
      let formedRequest: OIDCAuthenticationRequest;
      spyOn(challengerSession['sender'], 'sign').and.callFake((request: OIDCAuthenticationRequest) => {
        formedRequest = request;
        return Promise.resolve('');
      });
      await challengerSession.signRequest(redirectUrl, nonce, {
        scopes: [
          {
            'https://169.254.0.0/404': null,
          }
        ]
      });
      const verifySpy = spyOn(responderSession, 'verify').and.returnValue(Promise.resolve(formedRequest!));
      const fetchSpy = spyOn(nodeFetch, 'default').and.returnValue(<any> Promise.resolve({
        status: 404,
        text: () => 'Not Found'
      }));
      try {
        await responderSession.verifyAndHydrateRequest('doesnt Matter');
        fail('expected to throw');
      } catch (error) {
        expect(error.message).toContain('https://169.254.0.0/404');
      } finally {
        expect(verifySpy).toHaveBeenCalled();
        expect(fetchSpy).toHaveBeenCalled();
      }
    });

    it('should throw if the scope definitions resource_type is incorrectly formatted', async () => {
      const nonce = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(16);

      // utilize the sign request function, since we've got it... 
      let formedRequest: OIDCAuthenticationRequest;
      spyOn(challengerSession['sender'], 'sign').and.callFake((request: OIDCAuthenticationRequest) => {
        formedRequest = request;
        return Promise.resolve('');
      });
      await challengerSession.signRequest(redirectUrl, nonce, {
        scopes: [
          {
            'https://localhost/ok': null,
          }
        ]
      });
      const verifySpy = spyOn(responderSession, 'verify').and.returnValue(Promise.resolve(formedRequest!));
      const fetchSpy = spyOn(nodeFetch, 'default').and.returnValue(<any> Promise.resolve({
        status: 200,
        text: () => JSON.stringify({
          value: "http://localhost/ok",
          resourceBundle: {
            name: "Test",
            description: "should fail"
          },
          access: [
            {
              resource_type: 'https://schema.org',
              allow: '----'
            }
          ]
        })
      }));
      try {
        await responderSession.verifyAndHydrateRequest('doesnt Matter');
        fail('expected to throw');
      } catch (error) {
        expect(error.message).toContain('resource_type');
      } finally {
        expect(verifySpy).toHaveBeenCalled();
        expect(fetchSpy).toHaveBeenCalled();
      }
    });
  });

  describe('sendResponse', () => {
    const redirectUrl = `http://localhost/${Math.round(Math.random() * 255).toString(16)}`;

    const defaultResponse: OIDCAuthenticationResponse = {
      iss: 'https://self-issued.me',
      aud: redirectUrl,
      sub: 'thumbprint',
      nonce: 'nonce',
      did: 'did:test:responder',
      sub_jwk: {},
      exp: 0,
      iat: 0,
      state: undefined
    };

    const testCases: {
      should: string,
      request?: OIDCRequestOptions,
      claims?: any,
      grants?: IPermissionRequestPrompt[],
      expected: OIDCAuthenticationResponse
    }[] = [
      {
        should: 'send a simple id_token back',
        expected: Object.assign({}, defaultResponse)
      },
      {
        should: 'send permission grants',
        grants: [
          {
            name: 'Test',
            description: 'Read test results',
            required: true,
            grants: [
              {
                allow: '-R--',
                context: 'schema.org/',
                owner: 'did:test:responder',
                grantee: 'did:test:challenger',
                type: 'test',
              }
            ]
          }
        ],
        expected: Object.assign({}, defaultResponse)
      }
    ];
    
    testCases.forEach((testCase) => {
      it(`should ${testCase.should}`, async () => {
        const nonce = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(16);
  
        // utilize the sign request function, since we've got it... 
        let formedRequest: OIDCAuthenticationRequest;
        spyOn(challengerSession['sender'], 'sign').and.callFake((request: OIDCAuthenticationRequest) => {
          formedRequest = request;
          return Promise.resolve('');
        });
        await challengerSession.signRequest(redirectUrl, nonce, testCase.request);

        // mock the entire HubInterface to avoid sending data
        let grantsSubmitted = 0;
        responderSession['permissions'] = <any> {
          addObject: (commit: any) => {
            if (testCase.grants) {
              let grant = <IPermissionRequestPrompt> commit;
              grant.grants.forEach((permissionGrant) => {
                expect(permissionGrant.owner).toEqual('did:test:responder');
                expect(permissionGrant.grantee).toEqual('did:test:challenger');
                if (!testCase.grants!.some((grantBundle) => {
                  return grantBundle.grants.some((expectedGrant) => {
                    return permissionGrant.allow === expectedGrant.allow && permissionGrant.context === expectedGrant.context && permissionGrant.type === expectedGrant.type;
                  });
                })) {
                  fail(`Unexpected Permission Grant commited: ${grant}`);
                } else {
                  grantsSubmitted++;
                }
              })
            } else {
              fail('Attempted to Commit an object when no Permission Grants were passed')
            }
            return Promise.resolve({
              getRevisions: () => { return 'permissionGrantId'; }
            });
          }
        }

        spyOn(responderSession['sender']['options']!['keyStore'], 'get').and.returnValue(Promise.resolve(new EcPrivateKey({
          d: Math.round(Math.random() * Number.MAX_SAFE_INTEGER),
          x: Math.round(Math.random() * Number.MAX_SAFE_INTEGER),
          y: Math.round(Math.random() * Number.MAX_SAFE_INTEGER)
        }).getPublicKey()));

        const signCode = Math.round(Math.random() * Number.MAX_SAFE_INTEGER).toString(16);
        const signSpy = spyOn(responderSession['sender'], 'sign').and.callFake((response: OIDCAuthenticationResponse) => {
          expect(response).toEqual(Object.assign(testCase.expected, {
            nonce,
            exp: response.exp,
            iat: response.iat,
            sub: response.sub,
            sub_jwk: response.sub_jwk
          }));
          return Promise.resolve(signCode);
        });
        const fetchSpy = spyOn(nodeFetch, 'default').and.callFake((url: nodeFetch.Request | string, options?: nodeFetch.RequestInit) => {
          expect(url).toEqual(redirectUrl);
          expect(options!.method!.toLowerCase()).toEqual('post');
          expect(options!.body).toEqual(signCode);
          return Promise.resolve(<any> {
            status: 200,
            text: () => 'Test passed'
          });
        });

        await responderSession.sendResponse(formedRequest!, testCase.grants, testCase.claims)

        expect(signSpy).toHaveBeenCalled();
        expect(fetchSpy).toHaveBeenCalled();
        if (testCase.grants) {
          const totalGrantsExpected = testCase.grants.reduce((total: number, grantPrompt: IPermissionRequestPrompt) => {
            return total + grantPrompt.grants.length;
          }, 0);
          expect(grantsSubmitted).toEqual(totalGrantsExpected);
        }
      });
    });
  });
});
