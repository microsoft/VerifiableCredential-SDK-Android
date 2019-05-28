
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import JoseHelpers from "../../../../src/crypto/protocols/jose/JoseHelpers";
import { JweHeader } from "../../../../src/crypto/protocols/jwe/IJweGeneralJson";
import { TSMap } from "typescript-map";

describe('JoseHelpers', () => {
  it('should return header status from headerHasElements', () => {
    
    const header: JweHeader = new TSMap();
    expect(JoseHelpers.headerHasElements(header)).toBeFalsy();
// tslint:disable-next-line: no-backbone-get-set-outside-model
    header.set('key', 'value');
    expect(JoseHelpers.headerHasElements(header)).toBeTruthy();
  });
  it('should encode headers', () => {
    
    const header: JweHeader = new TSMap();
    let encoded = JoseHelpers.encodeHeader(header);
    expect(encoded).toEqual('e30');
    encoded = JoseHelpers.encodeHeader(header, false);
    expect(encoded).toEqual('{}');
// tslint:disable-next-line: no-backbone-get-set-outside-model
    header.set('key', 'value');
    encoded = JoseHelpers.encodeHeader(header);
    expect(encoded).toEqual('eyJrZXkiOiJ2YWx1ZSJ9');
    encoded = JoseHelpers.encodeHeader(header, false);
    expect(encoded).toEqual('{"key":"value"}');
  });

});
