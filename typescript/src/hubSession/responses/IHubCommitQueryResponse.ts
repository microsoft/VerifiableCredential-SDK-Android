/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { IHubResponse } from '@decentralized-identity/hub-common-js';
import { ICryptoToken } from '../../crypto/protocols/ICryptoToken';

/**
 * Represents a Hub's response to a `CommitQueryRequest`.
 */
export default interface IHubCommitQueryResponse extends IHubResponse<'CommitQueryResponse'> {
    /** 
     * The constant type of this response. 
     **/
    '@type': 'CommitQueryResponse';

    /** 
     * Array containing the requested commits. 
     **/
    'commits': ICryptoToken[];
    /** 
     * The pagination token which can be used to fetch the next page of results. 
     **/
    'skip_token': string | null;
}
