/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from "../plugin/CryptoFactory";
import { TSMap } from "typescript-map";
import { IPayloadProtectionProtocolInterface } from "./IPayloadProtectionProtocolInterface";

/**
 * Interface defining options for the selected protocol.
 */
export default interface IPayloadProtectionProtocolOptions {
  // The crypto algorithm suites used for cryptography
  cryptoFactory: CryptoFactory,

  // A dictionary for protocol specific options
  protocolOption: TSMap<string, any>,

  // The implementation of the selected protocol
  protocolInterface: IPayloadProtectionProtocolInterface,

  // Make the type indexable
  [key: string]: any;
}
