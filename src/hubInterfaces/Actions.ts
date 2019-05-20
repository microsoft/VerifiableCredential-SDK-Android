/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterface, { HubInterfaceOptions, HubInterfaceType } from './HubInterface';

/**
* A Class that represents objects 
*/
export default class Actions extends HubInterface {

  constructor (hubMethodsOptions: HubInterfaceOptions) {
    hubMethodsOptions.hubInterface = HubInterfaceType.Action;
    super(hubMethodsOptions);
  }
}
