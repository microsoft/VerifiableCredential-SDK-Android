interface ClaimObject {
  issuerDateTime: string;
  textModulesData: [
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
      ];
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
  claimDetails: string;
}
