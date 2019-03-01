import { UriDescription } from '../../types';

/**
 * What a ClaimClass Object will look like.
 */
export interface ClaimClass {
  /**
   * set the context.
   */
  '@context': string;
  /**
   * the type of object.
   */
  '@type': string;
  /**
   * the entity that issued the claim.
   */
  issuerName: string;
  /**
   * the logo for the claim.
   */
  claimLogo: {
    /**
     * the type of the sourceUri.
     */
    '@type': string;
    /**
     * the source Uri for the logo.
     */
    sourceUri: UriDescription;
  };
  /**
   * the displayed name of the claim.
   */
  claimName: string;
  /**
   * the background color of the claim.
   */
  hexBackgroundColor: string;
  /**
   * the font color of the claim.
   */
  hexFontColor: string;
  /**
   * optional property to get more information about the claim.
   */
  moreInfo?: string;
  /**
   * optional property to link to help.
   */
  helpLinks?: {
    /**
     * the actual uri of the link.
     */
    link: string
  };
  /**
   * a generic description of what is to be displayed on the claim UI.
   */
  claimDescriptions?: [
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
   * If links module data is also defined on the object, both will be displayed.
   */
  linksModuleData?: {
    /**
     * Link URIs.
     */
    uris: Array<UriDescription>;
  };
  /**
   * Optional property: image to be displayed in the app.
   * If image module data is also defined on the object, both will be displayed.
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
}
