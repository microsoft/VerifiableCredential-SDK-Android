interface ClaimClass {
  '@type': string;
  issuerName: string;
  claimLogo: {
    '@type': string;
    sourceUri: {
      '@type': string;
      uri: string;
    }
  };
  claimName: string;
  hexBackgroundColor: string;
  hexFontColor: string;
  heroImage: {
    '@type': string;
    sourceUri: {
      '@type': string;
      uri: string;
    }
  };
  moreInfo?: string;
  helpLinks?: {
    link: string
  };
  textModulesData?: [
      {
        header: string;
        body: string;
      }
  ];
  linksModuleData?: {
    uris: [
          {
            '@type': string;
            uri: string;
            description: string;
          }
      ]
  };
  imageModulesData?: [
      {
        mainImage: {
          '@type': string;
          sourceUri: {
            '@type': string;
            uri: string;
            description: string;
          }
        }
      }
  ];
}
