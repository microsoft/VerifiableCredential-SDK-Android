/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterface, { HubInterfaceOptions, HubInterfaceType } from './HubInterface';

/**
* A Class that does CRUD operations for storing items as Collections in the Hub
*/
export default class Collections extends HubInterface {

  constructor (hubMethodOptions: HubInterfaceOptions) {
    hubMethodOptions.hubInterface = HubInterfaceType.Collections;
    super(hubMethodOptions);
  }
}