import { UriDescription } from '../../types';

export interface ClaimObject {
  issuerDateTime: string;
  claimDescriptions: [
      {
        header: string;
        body: string;
      }
  ];
  linksModuleData?: {
    uris: Array<UriDescription>;
  };
  imageModulesData?: [
      {
        mainImage: {
          '@type': string;
          sourceUri: UriDescription;
        }
      }
  ];
  claimObjects: Array<ClaimObject>;
  claimDetails: string;
}
