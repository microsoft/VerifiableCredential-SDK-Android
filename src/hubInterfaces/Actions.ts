/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterfaces, { HubInterfacesOptions, HubInterface } from './HubInterfaces';

/**
* A Class that represents objects 
*/
export default class Actions extends HubInterfaces {

  constructor (hubMethodsOptions: HubInterfacesOptions) {
    hubMethodsOptions.hubInterface = HubInterface.Action;
    super(hubMethodsOptions);
  }
}
