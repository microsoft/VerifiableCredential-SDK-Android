/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubMethods, { HubMethodsOptions, HubInterface } from './HubMethods';

/**
* A Class that represents objects 
*/
export default class Actions extends HubMethods {

  constructor (hubMethodsOptions: HubMethodsOptions) {
    hubMethodsOptions.hubInterface = HubInterface.Action;
    super(hubMethodsOptions);
  }
}
