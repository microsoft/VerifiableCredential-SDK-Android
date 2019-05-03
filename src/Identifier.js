"use strict";
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
exports.__esModule = true;
var did_crypto_typescript_1 = require("@decentralized-identity/did-crypto-typescript");
var KeyStoreConstants_1 = require("src/keystores/KeyStoreConstants");
var IdentifierDocument_1 = require("src/IdentifierDocument");
var UserAgentError_1 = require("src/UserAgentError");
var Protect_1 = require("src/keystores/Protect");
var SignatureFormat_1 = require("src/keystores/SignatureFormat");
/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 */
var Identifier = /** @class */ (function () {
    /**
     * Constructs an instance of the Identifier
     * class using the provided identifier or identifier document.
     * @param identifier either the string representation of an identifier or a identifier document.
     * @param [options] for configuring how to register and resolve identifiers.
     */
    function Identifier(identifier, options) {
        this.identifier = identifier;
        // Check whether passed an identifier document
        // or an identifier string
        if (typeof identifier === 'object') {
            this.document = identifier;
            this.id = identifier.id;
        }
        else {
            this.id = identifier;
        }
        this.options = options;
    }
    /**
     * Creates a new decentralized identifier.
     * @param [options] for configuring how to register and resolve identifiers.
     */
    Identifier.create = function (options) {
        return __awaiter(this, void 0, void 0, function () {
            var id;
            return __generator(this, function (_a) {
                id = options.didPrefix;
                return [2 /*return*/, new Identifier(id, options).createLinkedIdentifier(id, true)];
            });
        });
    };
    /**
     * Creates a new decentralized identifier, using the current identifier
     * and the specified target. If the registar flag is true, the newly created
     * identifier will be registered using the
     * @param target entity for which to create the linked identifier
     * @param register flag indicating whether the new identifier should be registered
     * with a ledger.
     */
    Identifier.prototype.createLinkedIdentifier = function (target, register) {
        if (register === void 0) { register = false; }
        return __awaiter(this, void 0, void 0, function () {
            var keyStore, seed, didKey, pairwiseKey, jwk, pairwiseKeyStorageId, publicKey, document_1, identifier;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        if (!(this.options && this.options.keyStore)) return [3 /*break*/, 9];
                        keyStore = this.options.keyStore;
                        return [4 /*yield*/, keyStore.getKey(KeyStoreConstants_1["default"].masterSeed)];
                    case 1:
                        seed = _a.sent();
                        didKey = new did_crypto_typescript_1.DidKey(this.options.cryptoOptions.cryptoApi, this.options.cryptoOptions.algorithm);
                        return [4 /*yield*/, didKey.generatePairwise(seed, this.id, target)];
                    case 2:
                        pairwiseKey = _a.sent();
                        return [4 /*yield*/, pairwiseKey.getJwkKey(did_crypto_typescript_1.KeyExport.Private)];
                    case 3:
                        jwk = _a.sent();
                        pairwiseKeyStorageId = Identifier.keyStorageIdentifier(this.id, target, did_crypto_typescript_1.KeyUseFactory.create(pairwiseKey.algorithm), jwk.kty);
                        return [4 /*yield*/, keyStore.save(pairwiseKeyStorageId, jwk)];
                    case 4:
                        _a.sent();
                        publicKey = {
                            id: jwk.kid,
                            type: this.getDidDocumentKeyType(),
                            publicKeyJwk: jwk
                        };
                        if (!this.options.registrar) return [3 /*break*/, 8];
                        return [4 /*yield*/, this.createIdentifierDocument(this.id, publicKey)];
                    case 5:
                        document_1 = _a.sent();
                        if (!register) return [3 /*break*/, 7];
                        return [4 /*yield*/, this.options.registrar.register(document_1, pairwiseKeyStorageId)];
                    case 6:
                        identifier = _a.sent();
                        document_1.id = identifier.id;
                        _a.label = 7;
                    case 7: return [2 /*return*/, new Identifier(document_1, this.options)];
                    case 8: throw new UserAgentError_1["default"]("No registrar in options to register DID document");
                    case 9: throw new UserAgentError_1["default"]('No keyStore in options');
                }
            });
        });
    };
    /**
     * Gets the IdentifierDocument for the identifier
     * instance, throwing if no identifier has been
     * created.
     */
    Identifier.prototype.getDocument = function () {
        return __awaiter(this, void 0, void 0, function () {
            var _a;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        if (!!this.document) return [3 /*break*/, 2];
                        if (!this.options || !this.options.resolver) {
                            throw new UserAgentError_1["default"]('Resolver not specified in user agent options.');
                        }
                        // We need to resolve the document
                        _a = this;
                        return [4 /*yield*/, this.options.resolver.resolve(this)];
                    case 1:
                        // We need to resolve the document
                        _a.document = (_b.sent());
                        _b.label = 2;
                    case 2: return [2 /*return*/, this.document];
                }
            });
        });
    };
    /**
     * Performs a public key lookup using the
     * specified key identifier, returning the
     * key defined in document.
     * @param keyIdentifier the identifier of the public key.
     */
    Identifier.prototype.getPublicKey = function (keyIdentifier) {
        return __awaiter(this, void 0, void 0, function () {
            var index;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        if (!!this.document) return [3 /*break*/, 2];
                        return [4 /*yield*/, this.getDocument()];
                    case 1:
                        _a.sent();
                        _a.label = 2;
                    case 2:
                        // If we have been provided a key identifier use
                        // the identifier to look up a key in the document
                        if (this.document && this.document.publicKeys && keyIdentifier) {
                            index = this.document.publicKeys.findIndex(function (key) { return key.id === keyIdentifier; });
                            if (index === -1) {
                                throw new UserAgentError_1["default"]("Document does not contain a key with id '" + keyIdentifier + "'");
                            }
                            return [2 /*return*/, this.document.publicKeys[index]];
                        }
                        else if (this.document && this.document.publicKeys && this.document.publicKeys.length > 0) {
                            // If only one key has been specified in the document
                            // return that
                            return [2 /*return*/, this.document.publicKeys[0]];
                        }
                        throw new UserAgentError_1["default"]('Document does not contain any public keys');
                }
            });
        });
    };
    /**
     * Generate a storage identifier to store a key
     * @param personaId The identifier for the persona
     * @param target The identifier for the peer. Will be persona for non-pairwise keys
     * @param keyUse Key usage
     * @param keyType Key type
     */
    Identifier.keyStorageIdentifier = function (personaId, target, keyUse, keyType) {
        return personaId + "-" + target + "-" + keyUse + "-" + keyType;
    };
    // Create an identifier document. Included the public key.
    Identifier.prototype.createIdentifierDocument = function (id, publicKey) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                return [2 /*return*/, IdentifierDocument_1["default"].createAndGenerateId(id, [publicKey], this.options)];
            });
        });
    };
    // Get the did document public key type
    Identifier.prototype.getDidDocumentKeyType = function () {
        // Support other key types
        return 'Secp256k1VerificationKey2018';
    };
    /**
     * Sign payload with key specified by keyStorageIdentifier in options.keyStore
     * @param payload object to be signed
     * @param keyStorageIdentifier the identifier for the key used to sign payload.
     */
    Identifier.prototype.sign = function (payload, personaId, target) {
        return __awaiter(this, void 0, void 0, function () {
            var body, keyStorageIdentifier;
            return __generator(this, function (_a) {
                if (this.options && this.options.cryptoOptions) {
                    keyStorageIdentifier = Identifier.keyStorageIdentifier(personaId, target, did_crypto_typescript_1.KeyUse.Signature, did_crypto_typescript_1.KeyTypeFactory.create(this.options.cryptoOptions.algorithm));
                    if (this.options.keyStore) {
                        if (typeof (payload) !== 'string') {
                            body = JSON.stringify(payload);
                        }
                        else {
                            body = payload;
                        }
                        return [2 /*return*/, this.options.keyStore.sign(keyStorageIdentifier, body, SignatureFormat_1.SignatureFormat.FlatJsonJws)];
                    }
                    else {
                        throw new UserAgentError_1["default"]('No KeyStore in Options');
                    }
                }
                else {
                    throw new UserAgentError_1["default"]('No Crypto Options in User Agent Options');
                }
                return [2 /*return*/];
            });
        });
    };
    /**
     * Verify the payload with public key from the Identifier Document.
     * @param jws the signed token to be verified.
     */
    Identifier.prototype.verify = function (jws) {
        return __awaiter(this, void 0, void 0, function () {
            var _a;
            return __generator(this, function (_b) {
                switch (_b.label) {
                    case 0:
                        if (!!this.document) return [3 /*break*/, 2];
                        _a = this;
                        return [4 /*yield*/, this.getDocument()];
                    case 1:
                        _a.document = _b.sent();
                        _b.label = 2;
                    case 2: return [2 /*return*/, Protect_1["default"].verify(jws, this.document.publicKeys)];
                }
            });
        });
    };
    return Identifier;
}());
exports["default"] = Identifier;
