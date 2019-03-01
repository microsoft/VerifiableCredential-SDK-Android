import { UriDescription } from '../../types';

export interface ClaimClass {
  '@type': string;
  issuerName: string;
  claimLogo: {
    '@type': string;
    sourceUri: UriDescription;
  };
  claimName: string;
  hexBackgroundColor: string;
  hexFontColor: string;
  heroImage: {
    '@type': string;
    sourceUri: UriDescription;
  };
  moreInfo?: string;
  helpLinks?: {
    link: string
  };
  claimDescriptions?: [
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
}
