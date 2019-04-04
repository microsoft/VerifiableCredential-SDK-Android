/**
 * Interface for the issuer options for the Credential Manifest.
 */
export default interface CredentialManifestIssuerOptions {
  /**
   * Style Options for the input values of the credential form.
   */
  input: {
    /**
     * Element style of the credential form.
     */
    styles: any;
    /**
     * Input labels that correspond to data inputs.
     */
    labels: any;
  };
  /**
   * The presentation options for what should appear on the credential card in a User Agent.
   */
  presentation: {
    /**
     * The issuer name.
     */
    issuer_name: string;
    /**
     * The credential name.
     */
    credential_name: string;
    /**
     * Description of the credential
     */
    description: string;
    /**
     * Claims that can appear on the card.
     */
    claims: {
      /**
       * metadata pertaining to each claim in the claim token and how that claim should be presented.
       */
      [claim: string]: {
        /**
         * the label that should appear on the card.
         */
        label: string;
        /**
         * the type of the data.
         * e.g. number, string, date, etc.
         */
        type: string;
        /**
         * the value of the claim corresponding to a specific claim in the user-specific claim token.
         */
        value: string;
      };
    };
    /**
     * Styling Options for the presentation of the credential in the User Agent.
     */
    styles: any;
  };
}
