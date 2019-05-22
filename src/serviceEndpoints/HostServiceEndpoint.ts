/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ServiceEndpoint from "./ServiceEndpoint";
const cloneDeep = require('lodash/fp/cloneDeep');

const type = 'HostServiceEndpoint';
/**
 * class to represent a host service endpoint.
 */
export default class HostServiceEndpoint extends ServiceEndpoint {
  
  /**
   * locations of the hubs.
   */
  public locations: string[];

  constructor (context: string, locations: string[]) {
    super(context, type);
    this.locations = locations;
  }

  /**
   * Used to control the the properties that are
   * output by JSON.stringify.
   */
  public toJSON (): any {
    // Clone the current instance. Note the use of
    // a deep clone to ensure immutability of
    // the instance being cloned for serialization
    const clonedServiceReference = cloneDeep(this);

    if (this.context) { 
      clonedServiceReference['@context'] = this.context;
      delete clonedServiceReference.context;
    }
    if (this.type) {
      clonedServiceReference['@type'] = this.type;
      delete clonedServiceReference.type;
    }

    return clonedServiceReference;
  }

  /**
   * Used to control the the properties that are
   * output by JSON.parse.
   */
  public static fromJSON (object: any): HostServiceEndpoint { 
    return new HostServiceEndpoint(object['@context'], object['locations']);
  }
}
