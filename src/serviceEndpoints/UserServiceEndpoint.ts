/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ServiceEndpoint from "./ServiceEndpoint";
const cloneDeep = require('lodash/fp/cloneDeep');

const type = 'UserServiceEndpoint';
/**
 * Class to represent a host service endpoint.
 */
export default class UserServiceEndpoint extends ServiceEndpoint {
  
  /**
   * locations of the hubs.
   */
  public instances: string[];

  constructor (context: string, instances: string[]) {
    super(context, type);
    this.instances = instances;
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

    clonedServiceReference['@context'] = this.context;
    delete clonedServiceReference.context;
    clonedServiceReference['@type'] = this.type;
    delete clonedServiceReference.type;

    return clonedServiceReference;
  }

  /**
   * Used to control the the properties that are
   * output by JSON.parse.
   */
  public static fromJSON (object: any): ServiceEndpoint { 
    return new UserServiceEndpoint(object['@context'], object['instances']);
  }
}
