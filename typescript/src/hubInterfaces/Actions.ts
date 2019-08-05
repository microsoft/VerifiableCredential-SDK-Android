/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterface, { HubInterfaceOptions, HubInterfaceType } from './HubInterface';

/**
* A Class that does CRUD operations for storing objects as Actions in the Hub
*/
export default class Actions extends HubInterface {

  constructor (hubInterfaceOptions: HubInterfaceOptions) {
    hubInterfaceOptions.hubInterface = HubInterfaceType.Actions;
    super(hubInterfaceOptions);
  }
}
