/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterfaces, { HubMethodsOptions, HubInterface } from './HubInterfaces';

/**
* A Class that does CRUD operations for storing items as Collections in the Hub
*/
export default class Collections extends HubInterfaces {

  constructor (hubMethodOptions: HubMethodsOptions) {
    hubMethodOptions.hubInterface = HubInterface.Collections;
    super(hubMethodOptions);
  }
}