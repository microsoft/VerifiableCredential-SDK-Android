import { UriDescription } from '../../types';

/**
 * What a claimObject will look like.
 */
export interface ClaimObject {
  /**
   * When the claim was issued.
   */
  issuerDateTime: string;
  /**
   * Descriptions of the actual claims contained in the claimObject.
   */
  claimDescriptions: [
      {
        /**
         * Header of the description.
         */
        header: string;
        /**
         * Body of the description.
         */
        body: string;
      }
  ];
  /**
   * Optional property: links to be displayed in the app.
   * If links module data is also defined on the class, both will be displayed.
   */
  linksModuleData?: {
    /**
     * Link URIs.
     */
    uris: Array<UriDescription>;
  };
  /**
   * Optional property: image to be displayed in the app.
   * If image module data is also defined on the class, both will be displayed.
   */
  imageModulesData?: [
      {
        /**
         * Main Image.
         */
        mainImage: {
          /**
           * type of sourceUri.
           */
          '@type': string;
          /**
           * the source Uri for the image.
           */
          sourceUri: UriDescription;
        }
      }
  ];
  /**
   * any claimObjects to be included in the claimObject.
   */
  claimObjects: Array<ClaimObject>;
  /**
   * the actual verifiable claims.
   */
  claimDetails: string;
}
